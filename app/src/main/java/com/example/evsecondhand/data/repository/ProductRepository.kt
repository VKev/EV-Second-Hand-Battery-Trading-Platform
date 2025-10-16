package com.example.evsecondhand.data.repository

import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.ProductApiService

class ProductRepository(
    private val productApi: ProductApiService
) {
    
    suspend fun getBatteries(page: Int, limit: Int = 10): Result<List<Battery>> {
        return try {
            val response = productApi.getBatteries(page, limit)
            // Filter only AVAILABLE batteries
            val availableBatteries = response.data.batteries.filter { 
                it.status.equals("AVAILABLE", ignoreCase = true)
            }
            Result.success(availableBatteries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVehicles(page: Int, limit: Int = 10): Result<List<Vehicle>> {
        return try {
            val response = productApi.getVehicles(page, limit)
            // Filter only AVAILABLE vehicles
            val availableVehicles = response.data.vehicles.filter { 
                it.status.equals("AVAILABLE", ignoreCase = true)
            }
            Result.success(availableVehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
