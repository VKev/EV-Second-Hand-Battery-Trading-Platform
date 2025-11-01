package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.CheckoutResponse
import com.example.evsecondhand.data.model.PaymentMethod
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.CheckoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class ProcessingTransaction(val transactionId: String, val attempt: Int = 0) : CheckoutState()
    data class Success(val checkoutResponse: CheckoutResponse) : CheckoutState()
    data class Error(val message: String, val isInsufficientBalance: Boolean = false) : CheckoutState()
}

data class CheckoutUiState(
    val listingId: String = "",
    val listingType: String = "",
    val listingName: String = "",
    val listingPrice: Int = 0,
    val listingImage: String? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.WALLET,
    val walletBalance: Int = 0,
    val isLoadingWallet: Boolean = false,
    val checkoutState: CheckoutState = CheckoutState.Idle
)

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "CheckoutViewModel"
    }
    
    private val repository = CheckoutRepository(
        RetrofitClient.checkoutApi,
        RetrofitClient.walletApi,
        RetrofitClient.purchaseApi,
        application.applicationContext
    )
    
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    fun initCheckout(
        listingId: String,
        listingType: String,
        listingName: String,
        listingPrice: Int,
        listingImage: String?
    ) {
        Log.d(TAG, "Init checkout - ID: $listingId, Type: $listingType, Price: $listingPrice")
        _uiState.value = _uiState.value.copy(
            listingId = listingId,
            listingType = listingType,
            listingName = listingName,
            listingPrice = listingPrice,
            listingImage = listingImage
        )
        loadWalletBalance()
    }
    
    fun initCheckoutFromBattery(battery: Battery) {
        initCheckout(
            listingId = battery.id,
            listingType = "BATTERY",
            listingName = "${battery.brand} ${battery.title}",
            listingPrice = battery.price,
            listingImage = battery.images.firstOrNull()
        )
    }
    
    fun initCheckoutFromVehicle(vehicle: Vehicle) {
        initCheckout(
            listingId = vehicle.id,
            listingType = "VEHICLE",
            listingName = "${vehicle.brand} ${vehicle.model}",
            listingPrice = vehicle.price,
            listingImage = vehicle.images.firstOrNull()
        )
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        Log.d(TAG, "Payment method selected: ${method.displayName}")
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }
    
    private fun loadWalletBalance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingWallet = true)
            
            val result = repository.getWalletBalance()
            
            result.onSuccess { walletData ->
                Log.d(TAG, "Wallet balance loaded: ${walletData.availableBalance}")
                _uiState.value = _uiState.value.copy(
                    walletBalance = walletData.availableBalance.toInt(),
                    isLoadingWallet = false
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load wallet balance", exception)
                _uiState.value = _uiState.value.copy(
                    walletBalance = 0,
                    isLoadingWallet = false
                )
            }
        }
    }
    
    fun processCheckout() {
        val currentState = _uiState.value
        
        // Validate wallet balance if using WALLET payment
        if (currentState.selectedPaymentMethod == PaymentMethod.WALLET) {
            if (currentState.walletBalance < currentState.listingPrice) {
                Log.w(TAG, "Insufficient balance: ${currentState.walletBalance} < ${currentState.listingPrice}")
                _uiState.value = currentState.copy(
                    checkoutState = CheckoutState.Error(
                        message = "Số dư ví không đủ. Vui lòng nạp thêm tiền.",
                        isInsufficientBalance = true
                    )
                )
                return
            }
        }
        
        viewModelScope.launch {
            Log.d(TAG, "Processing checkout...")
            _uiState.value = currentState.copy(checkoutState = CheckoutState.Loading)
            
            val result = repository.checkout(
                listingId = currentState.listingId,
                listingType = currentState.listingType,
                paymentMethod = currentState.selectedPaymentMethod.value
            )
            
            result.onSuccess { response ->
                Log.d(TAG, "Checkout successful: ${response.message}")
                
                // Both WALLET and ZaloPay - immediate success
                // Backend will process transaction asynchronously
                _uiState.value = currentState.copy(
                    checkoutState = CheckoutState.Success(response)
                )
                
                // Reload wallet balance for WALLET payment
                if (currentState.selectedPaymentMethod == PaymentMethod.WALLET) {
                    Log.d(TAG, "WALLET payment - reloading balance")
                    loadWalletBalance()
                }
            }.onFailure { exception ->
                Log.e(TAG, "Checkout failed", exception)
                val errorMessage = when {
                    exception.message?.contains("401") == true -> 
                        "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    exception.message?.contains("403") == true -> 
                        "Bạn không có quyền thực hiện giao dịch này."
                    exception.message?.contains("404") == true -> 
                        "Sản phẩm không tồn tại hoặc đã được bán."
                    exception.message?.contains("insufficient") == true -> 
                        "Số dư ví không đủ. Vui lòng nạp thêm tiền."
                    exception.message?.contains("timeout") == true -> 
                        "Kết nối timeout. Vui lòng thử lại."
                    else -> 
                        "Thanh toán thất bại. Vui lòng thử lại."
                }
                
                _uiState.value = currentState.copy(
                    checkoutState = CheckoutState.Error(
                        message = errorMessage,
                        isInsufficientBalance = errorMessage.contains("Số dư ví không đủ")
                    )
                )
            }
        }
    }
    
    fun resetCheckoutState() {
        _uiState.value = _uiState.value.copy(checkoutState = CheckoutState.Idle)
    }
    
    fun canProceedCheckout(): Boolean {
        val currentState = _uiState.value
        return when (currentState.selectedPaymentMethod) {
            PaymentMethod.WALLET -> currentState.walletBalance >= currentState.listingPrice
            PaymentMethod.ZALOPAY -> true // ZaloPay luôn cho phép proceed
        }
    }
}
