package com.example.evsecondhand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatteryDetailState(
    val battery: Battery? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentBatteryId: String? = null
)

class BatteryDetailViewModel : ViewModel() {

    private val repository = ProductRepository(RetrofitClient.productApi)

    private val _state = MutableStateFlow(BatteryDetailState())
    val state: StateFlow<BatteryDetailState> = _state.asStateFlow()

    fun loadBattery(id: String, force: Boolean = false) {
        if (!force && _state.value.currentBatteryId == id && _state.value.battery != null) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                currentBatteryId = id
            )

            val result = repository.getBatteryDetail(id)

            result.onSuccess { battery ->
                _state.value = _state.value.copy(
                    battery = battery,
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
        _state.value.currentBatteryId?.let {
            loadBattery(it, force = true)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // viewModelScope will automatically cancel all coroutines
    }
}
