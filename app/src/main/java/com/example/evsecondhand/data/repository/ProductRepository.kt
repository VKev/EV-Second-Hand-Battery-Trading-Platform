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
    
    suspend fun getBatteries(page: Int, limit: Int = 100): Result<List<Battery>> {
        return try {
            Log.d(TAG, "Fetching batteries - page: $page, limit: $limit")
            val response = productApi.getBatteries(page, limit)
            val batteries = response.data.batteries.filter { battery ->
                val normalizedStatus = battery.status.uppercase()
                // Include AUCTION_LIVE, AVAILABLE, and other auction statuses
                normalizedStatus == "AVAILABLE" || 
                normalizedStatus == "AUCTION_LIVE" ||
                normalizedStatus.contains("AUCTION")
            }
            Log.d(TAG, "Batteries fetched: ${batteries.size} items after filtering by status")
            Result.success(batteries)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching batteries", e)
            Result.failure(e)
        }
    }
    
    suspend fun getVehicles(page: Int, limit: Int = 100): Result<List<Vehicle>> {
        return try {
            Log.d(TAG, "Fetching vehicles - page: $page, limit: $limit")
            val response = productApi.getVehicles(page, limit)
            val vehicles = response.data.vehicles.filter { vehicle ->
                val normalizedStatus = vehicle.status.uppercase()
                // Include AUCTION_LIVE, AVAILABLE, and other auction statuses
                normalizedStatus == "AVAILABLE" || 
                normalizedStatus == "AUCTION_LIVE" ||
                normalizedStatus.contains("AUCTION")
            }
            Log.d(TAG, "Vehicles fetched: ${vehicles.size} items after filtering by status")
            Result.success(vehicles)
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
