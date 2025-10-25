package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentVehicleId: String? = null
)

class VehicleDetailViewModel : ViewModel() {

    private val repository = ProductRepository(RetrofitClient.productApi)

    private val _state = MutableStateFlow(VehicleDetailState())
    val state: StateFlow<VehicleDetailState> = _state.asStateFlow()

    fun loadVehicle(id: String, force: Boolean = false) {
        if (!force && _state.value.currentVehicleId == id && _state.value.vehicle != null) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                currentVehicleId = id
            )

            val result = repository.getVehicleDetail(id)

            result.onSuccess { vehicle ->
                _state.value = _state.value.copy(
                    vehicle = vehicle,
                    isLoading = false
                )
            }.onFailure { exception ->
                val errorMessage = when {
                    exception.message?.contains("Unable to resolve host") == true ->
                        "Khong the ket noi den server. Vui long kiem tra ket noi Internet."
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "Ket noi bi timeout. Vui long thu lai."
                    exception.message?.contains("JSON", ignoreCase = true) == true ||
                        exception.message?.contains("Serialization", ignoreCase = true) == true ->
                        "Loi xu ly du lieu tu server: ${exception.message}"
                    else ->
                        "Loi: ${exception.message ?: "Unknown error"}"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun retry() {
        _state.value.currentVehicleId?.let {
            loadVehicle(it, force = true)
        }
    }
}
