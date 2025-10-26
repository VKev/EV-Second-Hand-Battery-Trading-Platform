package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.remote.WalletApiService

class WalletRepository(
    private val walletApi: WalletApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "WalletRepository"
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
    
    suspend fun getWalletBalance(): Result<WalletBalance> {
        return try {
            val token = getBearerToken()
            Log.d(TAG, "Fetching wallet balance with token")
            
            val response = walletApi.getWalletBalance(token)
            Log.d(TAG, "Wallet balance fetched successfully: ${response.data.availableBalance}")
            
            Result.success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching wallet balance", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTransactionHistory(page: Int = 1, limit: Int = 10): Result<Pair<List<Transaction>, Int>> {
        return try {
            val token = getBearerToken()
            Log.d(TAG, "Fetching transaction history - page: $page, limit: $limit")
            
            val response = walletApi.getTransactionHistory(token, page, limit)
            Log.d(TAG, "Transaction history fetched: ${response.data.transactions.size} items")
            
            Result.success(Pair(response.data.transactions, response.data.totalPages))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transaction history", e)
            Result.failure(e)
        }
    }
}