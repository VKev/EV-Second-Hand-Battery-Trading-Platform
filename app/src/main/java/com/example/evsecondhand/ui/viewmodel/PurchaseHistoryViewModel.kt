package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.PurchaseTransaction
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PurchaseHistoryState(
    val purchases: List<PurchaseTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalResults: Int = 0
)

class PurchaseHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "PurchaseHistoryViewModel"
    }
    
    private val repository = PurchaseRepository(RetrofitClient.purchaseApi, application)
    
    private val _state = MutableStateFlow(PurchaseHistoryState())
    val state: StateFlow<PurchaseHistoryState> = _state.asStateFlow()

    init {
        loadPurchases(1)
    }

    fun loadPurchases(page: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = repository.getMyPurchases(page, 10)
            
            result.onSuccess { (purchases, totalPages) ->
                Log.d(TAG, "Purchases loaded: ${purchases.size} items, page $page/$totalPages")
                _state.value = _state.value.copy(
                    purchases = purchases,
                    currentPage = page,
                    totalPages = totalPages,
                    totalResults = purchases.size,
                    isLoading = false
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load purchases", exception)
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
    
    fun refresh() {
        loadPurchases(1)
    }
}