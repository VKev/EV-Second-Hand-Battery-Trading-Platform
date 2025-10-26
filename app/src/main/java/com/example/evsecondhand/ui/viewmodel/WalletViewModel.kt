package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletState(
    val availableBalance: Double = 0.0,
    val lockedBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingTransactions: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalTransactions: Int = 0
)

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "WalletViewModel"
    }
    
    private val repository = WalletRepository(RetrofitClient.walletApi, application)
    
    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        loadWalletData()
    }

    private fun loadWalletData() {
        loadWalletBalance()
        loadTransactions(1)
    }
    
    private fun loadWalletBalance() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = repository.getWalletBalance()
            
            result.onSuccess { wallet ->
                Log.d(TAG, "Balance loaded: available=${wallet.availableBalance}, locked=${wallet.lockedBalance}")
                _state.value = _state.value.copy(
                    availableBalance = wallet.availableBalance,
                    lockedBalance = wallet.lockedBalance,
                    isLoading = false
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load balance", exception)
                val errorMessage = when {
                    exception.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server"
                    exception.message?.contains("timeout") == true -> 
                        "Kết nối bị timeout"
                    exception.message?.contains("401") == true || 
                    exception.message?.contains("Unauthorized") == true -> 
                        "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                    else -> 
                        "Lỗi: ${exception.message}"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun loadTransactions(page: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
            
            val result = repository.getTransactionHistory(page, 10)
            
            result.onSuccess { (transactions, totalPages) ->
                Log.d(TAG, "Transactions loaded: ${transactions.size} items, page $page/$totalPages")
                _state.value = _state.value.copy(
                    transactions = transactions,
                    currentPage = page,
                    totalPages = totalPages,
                    totalTransactions = transactions.size,
                    isLoadingTransactions = false
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load transactions", exception)
                val errorMessage = when {
                    exception.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server"
                    exception.message?.contains("timeout") == true -> 
                        "Kết nối bị timeout"
                    exception.message?.contains("401") == true || 
                    exception.message?.contains("Unauthorized") == true -> 
                        "Phiên đăng nhập hết hạn"
                    else -> 
                        "Lỗi: ${exception.message}"
                }
                _state.value = _state.value.copy(
                    isLoadingTransactions = false,
                    error = errorMessage
                )
            }
        }
    }

    fun depositFunds() {
        // TODO: Implement deposit logic
        Log.d(TAG, "Deposit funds clicked")
    }

    fun withdrawFunds() {
        // TODO: Implement withdraw logic
        Log.d(TAG, "Withdraw funds clicked")
    }
    
    fun refresh() {
        loadWalletData()
    }
}