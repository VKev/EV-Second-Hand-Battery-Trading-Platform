package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.evsecondhand.data.model.PurchaseTransaction
import com.example.evsecondhand.data.remote.PurchaseApiService

class PurchaseRepository(
    private val purchaseApi: PurchaseApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "PurchaseRepository"
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }
    
    private fun getBearerToken(): String {
        val token = getAccessToken()
        return "Bearer $token"
    }
    
    suspend fun getMyPurchases(page: Int = 1, limit: Int = 10): Result<Pair<List<PurchaseTransaction>, Int>> {
        return try {
            val token = getBearerToken()
            Log.d(TAG, "Fetching my purchases - page: $page, limit: $limit")
            
            val response = purchaseApi.getMyPurchases(token, page, limit)
            Log.d(TAG, "Purchases fetched: ${response.data.transactions.size} items")
            
            Result.success(Pair(response.data.transactions, response.data.totalPages))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching purchases", e)
            Result.failure(e)
        }
    }
}