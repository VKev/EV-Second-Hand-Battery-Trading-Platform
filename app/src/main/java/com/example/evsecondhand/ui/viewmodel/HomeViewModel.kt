package com.example.evsecondhand.ui.viewmodel

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
    
    private val repository = ProductRepository(RetrofitClient.productApi)
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        loadBatteries()
        loadVehicles()
    }
    
    fun loadBatteries(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingBatteries = true, batteryError = null)
            
            val result = repository.getBatteries(page)
            
            result.onSuccess { batteries ->
                val currentBatteries = if (page == 1) emptyList() else _state.value.batteries
                _state.value = _state.value.copy(
                    batteries = currentBatteries + batteries,
                    isLoadingBatteries = false,
                    currentBatteryPage = page,
                    hasMoreBatteries = batteries.isNotEmpty()
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoadingBatteries = false,
                    batteryError = exception.message ?: "Failed to load batteries"
                )
            }
        }
    }
    
    fun loadVehicles(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingVehicles = true, vehicleError = null)
            
            val result = repository.getVehicles(page)
            
            result.onSuccess { vehicles ->
                val currentVehicles = if (page == 1) emptyList() else _state.value.vehicles
                _state.value = _state.value.copy(
                    vehicles = currentVehicles + vehicles,
                    isLoadingVehicles = false,
                    currentVehiclePage = page,
                    hasMoreVehicles = vehicles.isNotEmpty()
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoadingVehicles = false,
                    vehicleError = exception.message ?: "Failed to load vehicles"
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
}
