package com.example.evsecondhand.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val batteries: List<Battery> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val isLoadingBatteries: Boolean = false,
    val isLoadingVehicles: Boolean = false,
    val batteryError: String? = null,
    val vehicleError: String? = null,
    val currentBatteryPage: Int = 1,
    val currentVehiclePage: Int = 1,
    val hasMoreBatteries: Boolean = true,
    val hasMoreVehicles: Boolean = true
)

class HomeViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    private val repository = ProductRepository(RetrofitClient.productApi)
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadBatteries()
        loadVehicles()
    }
    
    fun loadBatteries(page: Int = 1) {
        viewModelScope.launch {
            Log.d(TAG, "Loading batteries page: $page")
            _state.value = _state.value.copy(isLoadingBatteries = true, batteryError = null)
            
            val result = repository.getBatteries(page)
            
            result.onSuccess { batteries ->
                Log.d(TAG, "Batteries loaded successfully: ${batteries.size} items")
                val currentBatteries = if (page == 1) emptyList() else _state.value.batteries
                _state.value = _state.value.copy(
                    batteries = currentBatteries + batteries,
                    isLoadingBatteries = false,
                    currentBatteryPage = page,
                    hasMoreBatteries = batteries.isNotEmpty()
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load batteries", exception)
                val errorMessage = when {
                    exception.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server. Vui lòng kiểm tra kết nối Internet."
                    exception.message?.contains("timeout") == true -> 
                        "Kết nối bị timeout. Vui lòng thử lại."
                    exception.message?.contains("JSON") == true || 
                    exception.message?.contains("Serialization") == true -> 
                        "Lỗi xử lý dữ liệu từ server: ${exception.message}"
                    else -> 
                        "Lỗi: ${exception.message ?: "Unknown error"}"
                }
                _state.value = _state.value.copy(
                    isLoadingBatteries = false,
                    batteryError = errorMessage
                )
            }
        }
    }
    
    fun loadVehicles(page: Int = 1) {
        viewModelScope.launch {
            Log.d(TAG, "Loading vehicles page: $page")
            _state.value = _state.value.copy(isLoadingVehicles = true, vehicleError = null)
            
            val result = repository.getVehicles(page)
            
            result.onSuccess { vehicles ->
                Log.d(TAG, "Vehicles loaded successfully: ${vehicles.size} items")
                val currentVehicles = if (page == 1) emptyList() else _state.value.vehicles
                _state.value = _state.value.copy(
                    vehicles = currentVehicles + vehicles,
                    isLoadingVehicles = false,
                    currentVehiclePage = page,
                    hasMoreVehicles = vehicles.isNotEmpty()
                )
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load vehicles", exception)
                val errorMessage = when {
                    exception.message?.contains("Unable to resolve host") == true -> 
                        "Không thể kết nối đến server. Vui lòng kiểm tra kết nối Internet."
                    exception.message?.contains("timeout") == true -> 
                        "Kết nối bị timeout. Vui lòng thử lại."
                    exception.message?.contains("JSON") == true || 
                    exception.message?.contains("Serialization") == true -> 
                        "Lỗi xử lý dữ liệu từ server: ${exception.message}"
                    else -> 
                        "Lỗi: ${exception.message ?: "Unknown error"}"
                }
                _state.value = _state.value.copy(
                    isLoadingVehicles = false,
                    vehicleError = errorMessage
                )
            }
        }
    }
    
    fun loadMoreBatteries() {
        if (!_state.value.isLoadingBatteries && _state.value.hasMoreBatteries) {
            loadBatteries(_state.value.currentBatteryPage + 1)
        }
    }
    
    fun loadMoreVehicles() {
        if (!_state.value.isLoadingVehicles && _state.value.hasMoreVehicles) {
            loadVehicles(_state.value.currentVehiclePage + 1)
        }
    }
    
    fun refresh() {
        loadBatteries(1)
        loadVehicles(1)
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "HomeViewModel cleared - cancelling all coroutines")
    }
}
