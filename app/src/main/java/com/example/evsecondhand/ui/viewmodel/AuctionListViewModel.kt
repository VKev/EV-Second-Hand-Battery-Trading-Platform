package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.AuctionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuctionListState(
    val presentAuctions: List<AuctionSummary> = emptyList(),
    val futureAuctions: List<AuctionSummary> = emptyList(),
    val pastAuctions: List<AuctionSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuctionListViewModel : ViewModel() {

    private val repository = AuctionRepository(RetrofitClient.auctionApi)

    private val _state = MutableStateFlow(AuctionListState(isLoading = true))
    val state: StateFlow<AuctionListState> = _state.asStateFlow()

    init {
        loadAuctions()
    }

    fun loadAuctions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val presentResult = repository.getLiveAuctions("present")
            val futureResult = repository.getLiveAuctions("future")
            val pastResult = repository.getLiveAuctions("past")

            val allFailed = presentResult.isFailure && futureResult.isFailure && pastResult.isFailure
            val errorMessage = if (allFailed) {
                presentResult.exceptionOrNull()?.let(::mapToErrorMessage)
                    ?: futureResult.exceptionOrNull()?.let(::mapToErrorMessage)
                    ?: pastResult.exceptionOrNull()?.let(::mapToErrorMessage)
            } else {
                null
            }

            _state.value = AuctionListState(
                presentAuctions = presentResult.getOrElse { emptyList() },
                futureAuctions = futureResult.getOrElse { emptyList() },
                pastAuctions = pastResult.getOrElse { emptyList() },
                isLoading = false,
                error = errorMessage
            )
        }
    }

    fun retry() {
        loadAuctions()
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
