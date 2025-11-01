package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.evsecondhand.data.model.DepositData
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.remote.WalletApiService
import com.example.evsecondhand.data.zalopay.CreateOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    suspend fun depositFunds(amount: Int): Result<DepositData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating ZaloPay order - amount: $amount")
                
                // Use ZaloPay instead of backend API
                val createOrder = CreateOrder()
                val response = createOrder.createOrder(amount.toString())
                
                if (response == null) {
                    Log.e(TAG, "ZaloPay order creation failed - null response")
                    return@withContext Result.failure(Exception("Failed to create ZaloPay order"))
                }
                
                val returnCode = response.optInt("return_code", -1)
                val returnMessage = response.optString("return_message", "Unknown error")
                
                if (returnCode == 1) {
                    // Success
                    val zpTransToken = response.optString("zp_trans_token", "")
                    val orderUrl = response.optString("order_url", "")
                    val orderId = response.optString("app_trans_id", "")
                    
                    Log.d(TAG, "ZaloPay order created successfully - orderId: $orderId")
                    
                    // Convert ZaloPay response to DepositData format
                    val depositData = DepositData(
                        partnerCode = "ZALOPAY",
                        orderId = orderId,
                        requestId = zpTransToken,
                        amount = amount,
                        responseTime = System.currentTimeMillis(),
                        message = returnMessage,
                        resultCode = returnCode,
                        payUrl = orderUrl,
                        deeplink = orderUrl,
                        qrCodeUrl = orderUrl,
                        deeplinkMiniApp = orderUrl
                    )
                    
                    Result.success(depositData)
                } else {
                    Log.e(TAG, "ZaloPay order creation failed - code: $returnCode, message: $returnMessage")
                    Result.failure(Exception("ZaloPay error: $returnMessage"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating ZaloPay order", e)
                Result.failure(e)
            }
        }
    }
}