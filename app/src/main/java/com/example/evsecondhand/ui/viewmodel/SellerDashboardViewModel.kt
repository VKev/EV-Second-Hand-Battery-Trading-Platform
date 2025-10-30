package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.seller.BatteryItem
import com.example.evsecondhand.data.model.seller.BatteryItemFull
import com.example.evsecondhand.data.model.seller.UpdateBatteryRequest
import com.example.evsecondhand.data.model.seller.VehicleItem
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.SellerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class SellerDashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val vehicles: List<VehicleItem> = emptyList(),
    val batteries: List<BatteryItem> = emptyList()
)

class SellerDashboardViewModel(
    private val repository: SellerRepository
) : ViewModel() {

    companion object {
        fun provideFactory(
            accessToken: String
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SellerDashboardViewModel::class.java)) {
                        val repository = SellerRepository(
                            RetrofitClient.sellerApi,
                            accessToken
                        )
                        return SellerDashboardViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(SellerDashboardUiState(isLoading = true))
    val uiState: StateFlow<SellerDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadDashboardData()
        }
    }

    suspend fun fetchBatteryDetail(id: String): Result<BatteryItemFull> {
        return repository.fetchBatteryDetail(id)
    }

    suspend fun updateBattery(id: String, request: UpdateBatteryRequest) =
        repository.editBattery(id, request)

    private suspend fun loadDashboardData() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        var latestVehicles = currentState.vehicles
        var latestBatteries = currentState.batteries
        val errors = mutableListOf<String>()

        supervisorScope {
            val vehiclesDeferred = async { repository.fetchVehicles() }
            val batteriesDeferred = async { repository.fetchBatteries() }

            vehiclesDeferred.await()
                .onSuccess { latestVehicles = it }
                .onFailure { errors += it.toReadableError("Unable to load vehicles.") }

            batteriesDeferred.await()
                .onSuccess { latestBatteries = it }
                .onFailure { errors += it.toReadableError("Unable to load batteries.") }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            vehicles = latestVehicles,
            batteries = latestBatteries,
            errorMessage = errors.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n")
        )
    }

    private fun Throwable.toReadableError(
        fallback: String = "Something went wrong."
    ): String {
        val rootMessage = message ?: fallback
        return when {
            rootMessage.contains("timeout", ignoreCase = true) -> "Request timed out. Please try again."
            rootMessage.contains("Unable to resolve host", ignoreCase = true) -> "Cannot connect to server. Check your connection."
            else -> rootMessage
        }
    }
}
