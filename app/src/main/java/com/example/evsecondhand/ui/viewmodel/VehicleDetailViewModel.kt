package com.example.evsecondhand.ui.viewmodel

import com.example.evsecondhand.data.model.AuctionStatusData
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.AuctionRepository
import com.example.evsecondhand.data.repository.ProductRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.net.UnknownHostException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

private const val VEHICLE_LISTING_TYPE = "VEHICLE"

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentVehicleId: String? = null,
    val isCheckingAuctionStatus: Boolean = false,
    val isProcessingDeposit: Boolean = false,
    val canBid: Boolean = false,
    val hasDeposit: Boolean = false,
    val depositMessage: String? = null,
    val depositError: String? = null
)

class VehicleDetailViewModel : ViewModel() {

    private val productRepository = ProductRepository(RetrofitClient.productApi)
    private val auctionRepository = AuctionRepository(RetrofitClient.auctionApi)

    private val _state = MutableStateFlow(VehicleDetailState())
    val state: StateFlow<VehicleDetailState> = _state.asStateFlow()

    fun loadVehicle(id: String, force: Boolean = false) {
        if (!force && _state.value.currentVehicleId == id && _state.value.vehicle != null) {
            refreshAuctionStatusIfNeeded()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                currentVehicleId = id,
                isProcessingDeposit = false,
                depositMessage = null,
                depositError = null,
                canBid = false,
                hasDeposit = false
            )

            val result = productRepository.getVehicleDetail(id)

            result.onSuccess { vehicle ->
                _state.value = _state.value.copy(
                    vehicle = vehicle,
                    isLoading = false,
                    error = null
                )
                if (vehicle.isAuction == true) {
                    refreshAuctionStatus()
                } else {
                    _state.value = _state.value.copy(
                        canBid = false,
                        hasDeposit = false,
                        depositError = null
                    )
                }
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = mapToErrorMessage(exception)
                )
            }
        }
    }

    fun retry() {
        _state.value.currentVehicleId?.let {
            loadVehicle(it, force = true)
        }
    }

    fun refreshAuctionStatus(force: Boolean = false) {
        val vehicle = _state.value.vehicle ?: return
        if (vehicle.isAuction != true) return
        if (!force && _state.value.isCheckingAuctionStatus) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isCheckingAuctionStatus = true,
                depositError = null
            )

            val result = auctionRepository.getAuctionStatus(
                listingType = VEHICLE_LISTING_TYPE,
                listingId = vehicle.id
            )

            result.onSuccess { data ->
                applyAuctionStatus(data)
                _state.value = _state.value.copy(isCheckingAuctionStatus = false)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isCheckingAuctionStatus = false,
                    depositError = mapToErrorMessage(exception)
                )
            }
        }
    }

    private fun refreshAuctionStatusIfNeeded() {
        val vehicle = _state.value.vehicle ?: return
        if (vehicle.isAuction == true && !_state.value.isCheckingAuctionStatus) {
            refreshAuctionStatus(force = true)
        }
    }

    fun placeDeposit() {
        val vehicle = _state.value.vehicle ?: return
        if (vehicle.isAuction != true) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isProcessingDeposit = true,
                depositError = null,
                depositMessage = null
            )

            val result = auctionRepository.placeDeposit(
                listingType = VEHICLE_LISTING_TYPE,
                listingId = vehicle.id
            )

            result.onSuccess { depositResult ->
                applyAuctionStatus(depositResult.status)
                _state.value = _state.value.copy(
                    isProcessingDeposit = false,
                    canBid = true,
                    depositMessage = depositResult.message ?: "Đặt cọc thành công"
                )
                if (depositResult.status == null) {
                    refreshAuctionStatus(force = true)
                }
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isProcessingDeposit = false,
                    depositError = mapToErrorMessage(exception)
                )
            }
        }
    }

    fun clearDepositMessage() {
        if (_state.value.depositMessage != null) {
            _state.value = _state.value.copy(depositMessage = null)
        }
    }

    private fun applyAuctionStatus(status: AuctionStatusData?) {
        val canBid = status?.isBidder == true || status?.hasDeposit == true
        _state.value = _state.value.copy(
            canBid = canBid,
            hasDeposit = status?.hasDeposit == true,
            depositError = null
        )
    }

    private fun mapToErrorMessage(exception: Throwable): String {
        if (exception is HttpException) {
            extractServerMessage(exception)?.let { detailed ->
                if (detailed.isNotBlank()) return detailed
            }

            return when (exception.code()) {
                401 -> "Bạn cần đăng nhập để tiếp tục thao tác này."
                403 -> "Bạn không có quyền thực hiện thao tác này."
                404 -> "Không tìm thấy thông tin đấu giá. Vui lòng thử lại."
                422 -> "Dữ liệu gửi lên không hợp lệ. Vui lòng kiểm tra và thử lại."
                in 500..599 -> "Máy chủ đang gặp sự cố (HTTP ${exception.code()}). Vui lòng thử lại sau."
                else -> "Lỗi máy chủ (HTTP ${exception.code()}): ${exception.message()}"
            }
        }

        val message = exception.message.orEmpty()
        return when {
            exception is UnknownHostException ||
                message.contains("Unable to resolve host", ignoreCase = true) ->
                "Không thể kết nối đến server. Vui lòng kiểm tra kết nối Internet."
            message.contains("timeout", ignoreCase = true) ->
                "Kết nối bị timeout. Vui lòng thử lại."
            message.contains("JSON", ignoreCase = true) ||
                message.contains("Serialization", ignoreCase = true) ->
                "Lỗi xử lý dữ liệu từ server: $message"
            else ->
                "Lỗi: ${if (message.isBlank()) "Không xác định" else message}"
        }
    }
    private fun extractServerMessage(exception: HttpException): String? {
        val rawBody = try {
            exception.response()?.errorBody()?.string()
        } catch (ignored: Exception) {
            null
        }?.trim().orEmpty()

        if (rawBody.isBlank()) return null

        return try {
            val json = JSONObject(rawBody)

            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: json.optJSONArray("errors")?.firstNonBlank()
                ?: json.optJSONObject("data")?.let { data ->
                    data.optString("message").takeIf { it.isNotBlank() }
                        ?: data.optJSONArray("errors")?.firstNonBlank()
                }
                ?: rawBody
        } catch (ignored: Exception) {
            rawBody
        }
    }

    private fun JSONArray.firstNonBlank(): String? {
        for (index in 0 until length()) {
            val value = optString(index)
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
