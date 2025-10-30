package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.CheckoutPaymentInfo
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.PaymentRepository
import com.example.evsecondhand.data.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Locale

enum class PaymentItemType {
    BATTERY,
    VEHICLE;

    companion object {
        fun from(value: String?): PaymentItemType? {
            return when (value?.trim()?.lowercase()) {
                "battery" -> BATTERY
                "vehicle" -> VEHICLE
                else -> null
            }
        }
    }
}

enum class CheckoutPaymentMethod(val apiValue: String) {
    WALLET("WALLET"),
    MOMO("MOMO")
}

data class PendingMomoPayment(
    val transactionId: String,
    val paymentInfo: CheckoutPaymentInfo
)

data class CheckoutProductSummary(
    val id: String,
    val type: PaymentItemType,
    val brand: String,
    val name: String,
    val variant: String,
    val specs: List<String>,
    val price: Long
)

data class PaymentUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val balance: WalletBalance? = null,
    val history: List<Transaction> = emptyList(),
    val checkoutProduct: CheckoutProductSummary? = null,
    val isProductLoading: Boolean = false,
    val productError: String? = null,
    val isCheckoutProcessing: Boolean = false,
    val checkoutSuccessMessage: String? = null,
    val pendingMomoPayment: PendingMomoPayment? = null,
    val navigateToHome: Boolean = false
)

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    companion object {
        fun provideFactory(accessToken: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                        val paymentRepository = PaymentRepository(
                            RetrofitClient.checkoutApi,
                            accessToken
                        )
                        val productRepository = ProductRepository(RetrofitClient.productApi)
                        return PaymentViewModel(paymentRepository, productRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(PaymentUiState(isLoading = true))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private var lastLoadedProductKey: Pair<PaymentItemType, String>? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            fetchWalletData()
        }
    }

    fun loadCheckoutProduct(itemTypeRaw: String?, itemId: String?) {
        val type = PaymentItemType.from(itemTypeRaw)
        val normalizedId = itemId?.trim()?.takeIf { it.isNotEmpty() }

        if (type == null || normalizedId == null) {
            lastLoadedProductKey = null
            _uiState.value = _uiState.value.copy(
                checkoutProduct = null,
                isProductLoading = false,
                productError = null
            )
            return
        }

        val targetKey = type to normalizedId
        if (lastLoadedProductKey == targetKey && _uiState.value.checkoutProduct != null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProductLoading = true,
                productError = null
            )

            val result = when (type) {
                PaymentItemType.BATTERY -> productRepository
                    .getBatteryDetail(normalizedId)
                    .map { it.toCheckoutSummary() }
                PaymentItemType.VEHICLE -> productRepository
                    .getVehicleDetail(normalizedId)
                    .map { it.toCheckoutSummary() }
            }

            result
                .onSuccess { summary ->
                    lastLoadedProductKey = targetKey
                    _uiState.value = _uiState.value.copy(
                        checkoutProduct = summary,
                        isProductLoading = false,
                        productError = null
                    )
                }
                .onFailure { throwable ->
                    lastLoadedProductKey = null
                    _uiState.value = _uiState.value.copy(
                        checkoutProduct = null,
                        isProductLoading = false,
                        productError = throwable.toReadableError("Không thể tải thông tin sản phẩm.")
                    )
                }
        }
    }

    fun withdraw(amount: Long) {
        if (amount <= 0L) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Amount must be greater than zero."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            paymentRepository.submitWithdraw(amount)
                .onSuccess {
                    fetchWalletData()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.toReadableError()
                    )
                }
        }
    }

    fun initiateCheckout(method: CheckoutPaymentMethod) {
        val product = _uiState.value.checkoutProduct
        val listingId = product?.id?.takeIf { it.isNotBlank() }

        if (product == null || listingId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Không thể xác định sản phẩm để thanh toán."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckoutProcessing = true,
                errorMessage = null,
                checkoutSuccessMessage = null,
                pendingMomoPayment = null,
                navigateToHome = false
            )

            paymentRepository.initiateCheckout(
                listingId = listingId,
                listingType = product.type.name,
                paymentMethod = method.apiValue
            ).onSuccess { response ->
                val checkoutData = response.data
                val paymentInfo = checkoutData?.paymentInfo

                when (method) {
                    CheckoutPaymentMethod.MOMO -> {
                        if (checkoutData != null && paymentInfo != null) {
                            _uiState.value = _uiState.value.copy(
                                isCheckoutProcessing = false,
                                pendingMomoPayment = PendingMomoPayment(
                                    transactionId = checkoutData.transactionId,
                                    paymentInfo = paymentInfo
                                ),
                                navigateToHome = false
                            )
                        } else {
                            fetchWalletData()
                            _uiState.value = _uiState.value.copy(
                                isCheckoutProcessing = false,
                                checkoutSuccessMessage = response.message?.ifBlank { "Thanh toán thành công." },
                                pendingMomoPayment = null,
                                navigateToHome = false
                            )
                        }
                    }
                    CheckoutPaymentMethod.WALLET -> {
                        val transactionId = checkoutData?.transactionId
                        if (transactionId.isNullOrBlank()) {
                            _uiState.value = _uiState.value.copy(
                                isCheckoutProcessing = false,
                                errorMessage = "Không nhận được mã giao dịch từ hệ thống. Vui lòng thử lại.",
                                navigateToHome = false
                            )
                        } else {
                            paymentRepository.confirmCheckoutPayment(transactionId)
                                .onSuccess { message ->
                                    fetchWalletData()
                                    _uiState.value = _uiState.value.copy(
                                        isCheckoutProcessing = false,
                                        checkoutSuccessMessage = message?.ifBlank { null } ?: "Thanh toán thành công.",
                                        pendingMomoPayment = null,
                                        navigateToHome = true
                                    )
                                }
                                .onFailure { throwable ->
                                    _uiState.value = _uiState.value.copy(
                                        isCheckoutProcessing = false,
                                        errorMessage = "Thanh toán thất bại: ${throwable.toReadableError()}",
                                        navigateToHome = false
                                    )
                                }
                        }
                    }
                }
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isCheckoutProcessing = false,
                    errorMessage = throwable.toReadableError(),
                    navigateToHome = false
                )
            }
        }
    }

    fun confirmMomoPayment(transactionId: String) {
        if (transactionId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckoutProcessing = true,
                errorMessage = null,
                navigateToHome = false
            )

            paymentRepository.confirmCheckoutPayment(transactionId)
                .onSuccess { message ->
                    fetchWalletData()
                    _uiState.value = _uiState.value.copy(
                        isCheckoutProcessing = false,
                        checkoutSuccessMessage = message?.ifBlank { null } ?: "Thanh toán thành công.",
                        pendingMomoPayment = null,
                        navigateToHome = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isCheckoutProcessing = false,
                        errorMessage = "Thanh toán thất bại: ${throwable.toReadableError()}",
                        navigateToHome = false
                    )
                }
        }
    }

    fun clearCheckoutSuccessMessage() {
        if (_uiState.value.checkoutSuccessMessage != null || _uiState.value.navigateToHome) {
            _uiState.value = _uiState.value.copy(
                checkoutSuccessMessage = null,
                navigateToHome = false
            )
        }
    }

    fun clearPendingMomoPayment() {
        if (_uiState.value.pendingMomoPayment != null) {
            _uiState.value = _uiState.value.copy(pendingMomoPayment = null)
        }
    }

    private suspend fun fetchWalletData() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        var latestBalance: WalletBalance? = currentState.balance
    var latestHistory: List<Transaction> = currentState.history
        val errors = mutableListOf<String>()

        supervisorScope {
            val balanceDeferred = async { paymentRepository.fetchWalletBalance() }


            balanceDeferred.await()
                .onSuccess { latestBalance = it }
                .onFailure { errors += it.toReadableError("Unable to load wallet balance.") }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            balance = latestBalance,
            history = latestHistory,
            errorMessage = errors.takeIf { it.isNotEmpty() }?.joinToString("\n")
        )
    }

    private fun Battery.toCheckoutSummary(): CheckoutProductSummary {
        val specItems = mutableListOf<String>()
        specItems += "Dung lượng: ${capacity} kWh"
        health?.takeIf { it > 0 }?.let { specItems += "Sức khỏe pin: $it%" }
        specItems += "Năm sản xuất: $year"
        specifications?.voltage?.takeIf { it.isNotBlank() }?.let { specItems += "Điện áp: $it" }
        specifications?.chargingTime?.takeIf { it.isNotBlank() }?.let { specItems += "Thời gian sạc: $it" }
        specifications?.warrantyPeriod?.takeIf { it.isNotBlank() }?.let { specItems += "Bảo hành: $it" }

        val variantPieces = listOfNotNull(
            capacity.takeIf { it > 0 }?.let { "$it kWh" },
            health?.takeIf { it > 0 }?.let { "$it% sức khỏe" },
            status.takeIf { it.isNotBlank() }?.let { it.toReadableStatus() }
        ).filter { it.isNotBlank() }
        val variant = variantPieces.joinToString(" • ").ifBlank { "Pin EV" }

        return CheckoutProductSummary(
            id = id,
            type = PaymentItemType.BATTERY,
            brand = brand.ifBlank { "Pin EV" },
            name = title.ifBlank { brand.ifBlank { "Pin điện" } },
            variant = variant,
            specs = specItems.filter { it.isNotBlank() }.take(4),
            price = price.toLong().coerceAtLeast(0L)
        )
    }

    private fun Vehicle.toCheckoutSummary(): CheckoutProductSummary {
        val specItems = mutableListOf<String>()
        specItems += "Năm sản xuất: $year"
        specItems += "Quãng đường: ${mileage} km"
        specifications?.performance?.motorType?.takeIf { it.isNotBlank() }?.let { specItems += "Động cơ: $it" }
        specifications?.performance?.horsepower?.takeIf { it.isNotBlank() }?.let { specItems += "Công suất: $it" }
        specifications?.batteryAndCharging?.batteryCapacity?.takeIf { it.isNotBlank() }?.let { specItems += "Pin: $it" }
        specifications?.batteryAndCharging?.range?.takeIf { it.isNotBlank() }?.let { specItems += "Tầm hoạt động: $it" }

        val variantPieces = listOfNotNull(
            model.takeIf { it.isNotBlank() },
            "Năm $year",
            status.takeIf { it.isNotBlank() }?.let { it.toReadableStatus() }
        ).filter { it.isNotBlank() }
        val variant = variantPieces.joinToString(" • ").ifBlank { "Xe EV" }

        val resolvedName = title.ifBlank {
            listOf(brand, model).filter { it.isNotBlank() }.distinct().joinToString(" ").ifBlank { "Xe điện" }
        }

        return CheckoutProductSummary(
            id = id,
            type = PaymentItemType.VEHICLE,
            brand = brand.ifBlank { "Xe EV" },
            name = resolvedName,
            variant = variant,
            specs = specItems.filter { it.isNotBlank() }.take(4),
            price = price.toLong().coerceAtLeast(0L)
        )
    }

    private fun String.toReadableStatus(): String {
        val normalized = replace('_', ' ').lowercase(Locale.getDefault())
        return normalized.split(' ').joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.getDefault())
                } else {
                    char.toString()
                }
            }
        }
    }

    private fun Throwable.toReadableError(defaultMessage: String = "Something went wrong."): String {
        val rootMessage = message ?: defaultMessage
        return when {
            rootMessage.contains("timeout", ignoreCase = true) -> "Request timed out. Please try again."
            rootMessage.contains("Unable to resolve host", ignoreCase = true) -> "Cannot connect to server. Check your internet connection."
            else -> rootMessage
        }
    }
}
