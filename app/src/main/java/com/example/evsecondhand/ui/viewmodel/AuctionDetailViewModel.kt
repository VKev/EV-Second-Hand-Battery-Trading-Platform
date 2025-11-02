package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.AuctionDetailData
import com.example.evsecondhand.data.model.AuctionSeller
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Seller
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
            isProductLoading = true
        )

        viewModelScope.launch {
            repository.getAuctionDetail(listingType, listingId)
                .onSuccess { detail ->
                    val products = mapProductFromDetail(detail, listingType)
                    val shouldFetchProduct = when (listingType.uppercase(Locale.ROOT)) {
                        "VEHICLE" -> products.vehicle == null
                        "BATTERY" -> products.battery == null
                        else -> false
                    }

                    _uiState.value = _uiState.value.copy(
                        detail = detail,
                        summary = detail?.let { repository.toAuctionSummary(it) },
                        isLoading = false,
                        error = null,
                        vehicle = products.vehicle ?: _uiState.value.vehicle,
                        battery = products.battery ?: _uiState.value.battery,
                        isProductLoading = shouldFetchProduct,
                        productError = null
                    )

                    if (shouldFetchProduct) {
                        fetchListingDetail(listingType, listingId)
                    } else {
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
            productError = null
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
        val refreshedProducts = mapProductFromDetail(refreshed, listingType)
        _uiState.value = _uiState.value.copy(
            detail = refreshed ?: _uiState.value.detail,
            summary = refreshed?.let { repository.toAuctionSummary(it) } ?: _uiState.value.summary,
            isProcessingDeposit = false,
            error = null,
            message = result.message ?: "Đặt cọc thành công",
            vehicle = refreshedProducts.vehicle ?: _uiState.value.vehicle,
            battery = refreshedProducts.battery ?: _uiState.value.battery
        )
    }

    private suspend fun handleBidSuccess(
        listingType: String,
        listingId: String,
        result: BidResult
    ) {
        val updatedDetail = result.detail ?: repository.getAuctionDetail(listingType, listingId).getOrNull()
        val products = mapProductFromDetail(updatedDetail, listingType)
        _uiState.value = _uiState.value.copy(
            detail = updatedDetail ?: _uiState.value.detail,
            summary = updatedDetail?.let { repository.toAuctionSummary(it) } ?: _uiState.value.summary,
            isPlacingBid = false,
            error = null,
            message = result.message ?: "Đấu giá thành công",
            vehicle = products.vehicle ?: _uiState.value.vehicle,
            battery = products.battery ?: _uiState.value.battery
        )
    }

    private fun mapToErrorMessage(exception: Throwable): String {
        val message = exception.message.orEmpty()
        return when {
            message.contains("Unable to resolve host", ignoreCase = true) ->
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối Internet."
            message.contains("timeout", ignoreCase = true) ->
                "Kết nối bị timeout. Vui lòng thử lại."
            message.contains("JSON", ignoreCase = true) ||
                message.contains("Serialization", ignoreCase = true) ->
                "Lỗi xử lý dữ liệu từ máy chủ: $message"
            else ->
                "Lỗi: ${message.ifBlank { "Lỗi không xác định" }}"
        }
    }
}

private data class ProductMapping(
    val vehicle: Vehicle? = null,
    val battery: Battery? = null
)

private fun mapProductFromDetail(detail: AuctionDetailData?, listingType: String?): ProductMapping {
    if (detail == null || listingType == null) return ProductMapping()
    return when (listingType.uppercase(Locale.ROOT)) {
        "VEHICLE" -> ProductMapping(vehicle = detail.toVehicle())
        "BATTERY" -> ProductMapping(battery = detail.toBattery())
        else -> ProductMapping()
    }
}

private fun AuctionDetailData.toVehicle(): Vehicle? {
    val resolvedId = id ?: listingId ?: return null
    val resolvedTitle = title ?: return null
    val resolvedBrand = brand ?: return null
    val resolvedModel = model ?: return null
    val resolvedYear = year ?: return null
    val resolvedMileage = mileage ?: 0
    val resolvedPrice = price ?: startingPrice ?: return null
    val resolvedImages = when {
        !images.isNullOrEmpty() -> images
        !image.isNullOrBlank() -> listOf(image)
        else -> emptyList()
    }
    val resolvedSellerId = sellerId ?: seller?.id ?: resolvedId

    return Vehicle(
        id = resolvedId,
        title = resolvedTitle,
        description = description.orEmpty(),
        price = resolvedPrice,
        images = resolvedImages,
        status = status ?: "UNKNOWN",
        brand = resolvedBrand,
        model = resolvedModel,
        year = resolvedYear,
        mileage = resolvedMileage,
        specifications = specifications,
        isVerified = isVerified ?: seller?.isVerified ?: false,
        isAuction = isAuction,
        auctionStartsAt = auctionStartsAt,
        auctionEndsAt = auctionEndsAt,
        startingPrice = startingPrice ?: resolvedPrice,
        bidIncrement = bidIncrement,
        depositAmount = depositAmount,
        auctionRejectionReason = auctionRejectionReason,
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt ?: createdAt.orEmpty(),
        sellerId = resolvedSellerId,
        seller = seller?.toSeller(resolvedSellerId)
    )
}

private fun AuctionDetailData.toBattery(): Battery? {
    val resolvedId = id ?: listingId ?: return null
    val resolvedTitle = title ?: return null
    val resolvedBrand = brand ?: return null
    val resolvedYear = year ?: return null
    val resolvedCapacity = capacity ?: return null
    val resolvedPrice = price ?: startingPrice ?: return null
    val resolvedImages = when {
        !images.isNullOrEmpty() -> images
        !image.isNullOrBlank() -> listOf(image)
        else -> emptyList()
    }
    val resolvedSellerId = sellerId ?: seller?.id ?: resolvedId

    return Battery(
        id = resolvedId,
        title = resolvedTitle,
        description = description.orEmpty(),
        price = resolvedPrice,
        images = resolvedImages,
        status = status ?: "UNKNOWN",
        brand = resolvedBrand,
        capacity = resolvedCapacity,
        year = resolvedYear,
        health = health,
        specifications = batterySpecifications,
        isVerified = isVerified ?: seller?.isVerified ?: false,
        isAuction = isAuction,
        auctionStartsAt = auctionStartsAt,
        auctionEndsAt = auctionEndsAt,
        startingPrice = startingPrice ?: resolvedPrice,
        bidIncrement = bidIncrement,
        depositAmount = depositAmount,
        auctionRejectionReason = auctionRejectionReason,
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt ?: createdAt.orEmpty(),
        sellerId = resolvedSellerId,
        seller = seller?.toSeller(resolvedSellerId)
    )
}

private fun AuctionSeller.toSeller(fallbackId: String): Seller {
    val resolvedId = id ?: fallbackId
    val resolvedName = name.orEmpty()
    return Seller(
        id = resolvedId,
        name = resolvedName,
        avatar = avatar,
        isVerified = isVerified ?: false
    )
}
