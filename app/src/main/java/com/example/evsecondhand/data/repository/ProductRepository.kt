package com.example.evsecondhand.data.repository

import android.util.Log
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.remote.ProductApiService

class ProductRepository(
    private val productApi: ProductApiService
) {
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    suspend fun getBatteries(page: Int, limit: Int = 10): Result<List<Battery>> {
        return try {
            Log.d(TAG, "Fetching batteries - page: $page, limit: $limit")
            val response = productApi.getBatteries(page, limit)
            Log.d(TAG, "Battery response received: ${response.data.batteries.size} items")
            Log.d(TAG, "Battery statuses: ${response.data.batteries.map { it.status }}")
            
            // Filter only AVAILABLE batteries
            val availableBatteries = response.data.batteries.filter { 
                it.status.equals("AVAILABLE", ignoreCase = true)
            }
            Log.d(TAG, "Available batteries after filter: ${availableBatteries.size}")
            
            // If no AVAILABLE batteries found, show warning and return all
            if (availableBatteries.isEmpty() && response.data.batteries.isNotEmpty()) {
                Log.w(TAG, "No AVAILABLE batteries found, returning all batteries for debugging")
                Result.success(response.data.batteries)
            } else {
                Result.success(availableBatteries)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching batteries", e)
            Result.failure(e)
        }
    }
    
    suspend fun getVehicles(page: Int, limit: Int = 10): Result<List<Vehicle>> {
        return try {
            Log.d(TAG, "Fetching vehicles - page: $page, limit: $limit")
            val response = productApi.getVehicles(page, limit)
            Log.d(TAG, "Vehicle response received: ${response.data.vehicles.size} items")
            Log.d(TAG, "Vehicle statuses: ${response.data.vehicles.map { it.status }}")
            
            // Filter only AVAILABLE vehicles
            val availableVehicles = response.data.vehicles.filter { 
                it.status.equals("AVAILABLE", ignoreCase = true)
            }
            Log.d(TAG, "Available vehicles after filter: ${availableVehicles.size}")
            
            // If no AVAILABLE vehicles found, show warning and return all
            if (availableVehicles.isEmpty() && response.data.vehicles.isNotEmpty()) {
                Log.w(TAG, "No AVAILABLE vehicles found, returning all vehicles for debugging")
                Result.success(response.data.vehicles)
            } else {
                Result.success(availableVehicles)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicles", e)
            Result.failure(e)
        }
    }

    suspend fun getBatteryDetail(id: String): Result<Battery> {
        return try {
            Log.d(TAG, "Fetching battery detail - id: $id")
            val response = productApi.getBatteryDetail(id)
            Result.success(response.data.battery)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching battery detail", e)
            Result.failure(e)
        }
    }

    suspend fun getVehicleDetail(id: String): Result<Vehicle> {
        return try {
            Log.d(TAG, "Fetching vehicle detail - id: $id")
            val response = productApi.getVehicleDetail(id)
            Result.success(response.data.vehicle)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle detail", e)
            Result.failure(e)
        }
    }
}
