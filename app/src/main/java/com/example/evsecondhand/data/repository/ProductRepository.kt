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
            
            // Fetch multiple pages to ensure we get AVAILABLE items
            val allBatteries = mutableListOf<Battery>()
            var currentPage = page
            var hasMore = true
            var pagesChecked = 0
            val maxPagesToCheck = 10 // Check up to 5 pages to find AVAILABLE items
            
            while (hasMore && pagesChecked < maxPagesToCheck && allBatteries.size < limit) {
                val response = productApi.getBatteries(currentPage, limit)
                Log.d(TAG, "Page $currentPage - received: ${response.data.batteries.size} items")
                
                // Filter AVAILABLE batteries from this page
                val availableFromPage = response.data.batteries.filter { 
                    it.status.equals("AVAILABLE", ignoreCase = true)
                }
                
                allBatteries.addAll(availableFromPage)
                Log.d(TAG, "Page $currentPage - AVAILABLE: ${availableFromPage.size}, Total collected: ${allBatteries.size}")
                
                // Stop if no more pages or we got enough items
                hasMore = response.data.batteries.size == limit && currentPage < response.data.totalPages
                currentPage++
                pagesChecked++
            }
            
            Log.d(TAG, "Final result: ${allBatteries.size} AVAILABLE batteries from $pagesChecked pages")
            Result.success(allBatteries.take(limit))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching batteries", e)
            Result.failure(e)
        }
    }
    
    suspend fun getVehicles(page: Int, limit: Int = 10): Result<List<Vehicle>> {
        return try {
            Log.d(TAG, "Fetching vehicles - page: $page, limit: $limit")
            
            // Fetch multiple pages to ensure we get AVAILABLE items
            val allVehicles = mutableListOf<Vehicle>()
            var currentPage = page
            var hasMore = true
            var pagesChecked = 0
            val maxPagesToCheck = 5 // Check up to 5 pages to find AVAILABLE items
            
            while (hasMore && pagesChecked < maxPagesToCheck && allVehicles.size < limit) {
                val response = productApi.getVehicles(currentPage, limit)
                Log.d(TAG, "Page $currentPage - received: ${response.data.vehicles.size} items")
                
                // Filter AVAILABLE vehicles from this page
                val availableFromPage = response.data.vehicles.filter { 
                    it.status.equals("AVAILABLE", ignoreCase = true)
                }
                
                allVehicles.addAll(availableFromPage)
                Log.d(TAG, "Page $currentPage - AVAILABLE: ${availableFromPage.size}, Total collected: ${allVehicles.size}")
                
                // Stop if no more pages or we got enough items
                hasMore = response.data.vehicles.size == limit && currentPage < response.data.totalPages
                currentPage++
                pagesChecked++
            }
            
            Log.d(TAG, "Final result: ${allVehicles.size} AVAILABLE vehicles from $pagesChecked pages")
            Result.success(allVehicles.take(limit))
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
