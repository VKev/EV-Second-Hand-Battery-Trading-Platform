package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletState(
    val balance: Double = 2848.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalTransactions: Int = 8
)

class WalletViewModel : ViewModel() {
    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        loadWalletData()
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Mock transactions theo hình của bạn
            val mockTransactions = listOf(
                Transaction("1", "2025-10-25", TransactionType.AUCTION_BID, "AUCTION_DEPOSIT", -2000.0),
                Transaction("2", "2025-10-25", TransactionType.DEPOSIT, "User deposits 20000...", 2000.0),
                Transaction("3", "2025-10-25", TransactionType.DEPOSIT, "User deposits 20000...", 2000.0),
                Transaction("4", "2025-10-25", TransactionType.DEPOSIT, "User deposits 20000...", 2000.0),
                Transaction("5", "2025-10-25", TransactionType.DEPOSIT, "User deposits 50000...", 5000.0),
                Transaction("6", "2025-10-25", TransactionType.DEPOSIT, "User deposits 50000 ...", 50.0),
                Transaction("7", "2025-10-25", TransactionType.DEPOSIT, "User deposits 50000 ...", 50.0),
                Transaction("8", "2025-10-25", TransactionType.DEPOSIT, "User deposits 50000 ...", 50.0)
            )

            _state.value = _state.value.copy(
                transactions = mockTransactions,
                isLoading = false,
                totalTransactions = mockTransactions.size
            )
        }
    }

    fun loadTransactions(page: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(currentPage = page)
            // TODO: Load transactions for specific page from API
        }
    }

    fun depositFunds() {
        // TODO: Implement deposit logic
    }

    fun withdrawFunds() {
        // TODO: Implement withdraw logic
    }
}