package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.AuctionDetailData
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.AuctionRepository
import com.example.evsecondhand.data.repository.BidResult
import com.example.evsecondhand.data.repository.DepositResult
import com.example.evsecondhand.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class AuctionDetailUiState(
    val detail: AuctionDetailData? = null,
    val summary: AuctionSummary? = null,
    val listingType: String? = null,
    val listingId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isProcessingDeposit: Boolean = false,
    val isPlacingBid: Boolean = false,
    val message: String? = null,
    val vehicle: Vehicle? = null,
    val battery: Battery? = null,
    val isProductLoading: Boolean = false,
    val productError: String? = null
)

class AuctionDetailViewModel : ViewModel() {

    private val repository = AuctionRepository(RetrofitClient.auctionApi)
    private val productRepository = ProductRepository(RetrofitClient.productApi)

    private val _uiState = MutableStateFlow(AuctionDetailUiState(isLoading = true))
    val uiState: StateFlow<AuctionDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(listingType: String, listingId: String, force: Boolean = false) {
        val currentType = _uiState.value.listingType
        if (!force &&
            currentType != null &&
            listingType.equals(currentType, ignoreCase = true) &&
            listingId == _uiState.value.listingId &&
            _uiState.value.detail != null
        ) {
            return
        }

        _uiState.value = _uiState.value.copy(
            listingType = listingType,
            listingId = listingId,
            isLoading = true,
            error = null,
            productError = null,
            vehicle = null,
            battery = null,
            isProductLoading = true
        )

        viewModelScope.launch {
            repository.getAuctionDetail(listingType, listingId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        detail = detail,
                        summary = detail?.let { repository.toAuctionSummary(it) },
                        isLoading = false,
                        error = null
                    )
                    detail?.let {
                        fetchListingDetail(listingType, listingId)
                    } ?: run {
                        _uiState.value = _uiState.value.copy(isProductLoading = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isProductLoading = false,
                        isLoading = false,
                        error = mapToErrorMessage(throwable)
                    )
                }
        }
    }

    fun retry() {
        val listingType = _uiState.value.listingType ?: return
        val listingId = _uiState.value.listingId ?: return
        loadDetail(listingType, listingId, force = true)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun placeDeposit() {
        val listingType = _uiState.value.listingType ?: return
        val listingId = _uiState.value.listingId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingDeposit = true)

            repository.placeDeposit(listingType, listingId)
                .onSuccess { result ->
                    handleDepositSuccess(listingType, listingId, result)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isProcessingDeposit = false,
                        message = mapToErrorMessage(throwable)
                    )
                }
        }
    }

    fun placeBid(amount: Int) {
        val listingType = _uiState.value.listingType ?: return
        val listingId = _uiState.value.listingId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPlacingBid = true)

            repository.placeBid(listingType, listingId, amount)
                .onSuccess { result ->
                    handleBidSuccess(listingType, listingId, result)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isPlacingBid = false,
                        message = mapToErrorMessage(throwable)
                    )
                }
        }
    }

    private suspend fun fetchListingDetail(listingType: String, listingId: String) {
        val normalizedType = listingType.uppercase(Locale.ROOT)
        _uiState.value = _uiState.value.copy(
            isProductLoading = true,
            productError = null,
            vehicle = null,
            battery = null
        )

        when (normalizedType) {
            "VEHICLE" -> {
                val result = productRepository.getVehicleDetail(listingId)
                _uiState.value = _uiState.value.copy(
                    isProductLoading = false,
                    vehicle = result.getOrNull(),
                    productError = result.exceptionOrNull()?.let { mapToErrorMessage(it) }
                )
            }
            "BATTERY" -> {
                val result = productRepository.getBatteryDetail(listingId)
                _uiState.value = _uiState.value.copy(
                    isProductLoading = false,
                    battery = result.getOrNull(),
                    productError = result.exceptionOrNull()?.let { mapToErrorMessage(it) }
                )
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    isProductLoading = false
                )
            }
        }
    }

    private suspend fun handleDepositSuccess(
        listingType: String,
        listingId: String,
        result: DepositResult
    ) {
        val refreshed = repository.getAuctionDetail(listingType, listingId).getOrNull()
        _uiState.value = _uiState.value.copy(
            detail = refreshed ?: _uiState.value.detail,
            summary = refreshed?.let { repository.toAuctionSummary(it) } ?: _uiState.value.summary,
            isProcessingDeposit = false,
            error = null,
            message = result.message ?: "Dat coc thanh cong"
        )
    }

    private suspend fun handleBidSuccess(
        listingType: String,
        listingId: String,
        result: BidResult
    ) {
        val updatedDetail = result.detail ?: repository.getAuctionDetail(listingType, listingId).getOrNull()
        _uiState.value = _uiState.value.copy(
            detail = updatedDetail ?: _uiState.value.detail,
            summary = updatedDetail?.let { repository.toAuctionSummary(it) } ?: _uiState.value.summary,
            isPlacingBid = false,
            error = null,
            message = result.message ?: "Dau gia thanh cong"
        )
    }

    private fun mapToErrorMessage(exception: Throwable): String {
        val message = exception.message.orEmpty()
        return when {
            message.contains("Unable to resolve host", ignoreCase = true) ->
                "Khong the ket noi den server. Vui long kiem tra ket noi Internet."
            message.contains("timeout", ignoreCase = true) ->
                "Ket noi bi timeout. Vui long thu lai."
            message.contains("JSON", ignoreCase = true) ||
                message.contains("Serialization", ignoreCase = true) ->
                "Loi xu ly du lieu tu server: $message"
            else ->
                "Loi: ${message.ifBlank { "Unknown error" }}"
        }
    }
}
