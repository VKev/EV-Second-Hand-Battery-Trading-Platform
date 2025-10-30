package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.evsecondhand.data.model.CheckoutData
import com.example.evsecondhand.data.model.CheckoutRequest
import com.example.evsecondhand.data.model.CheckoutResponse
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.remote.CheckoutApiService
import com.example.evsecondhand.data.remote.WalletApiService

class CheckoutRepository(
    private val checkoutApi: CheckoutApiService,
    private val walletApi: WalletApiService,
    private val purchaseApi: com.example.evsecondhand.data.remote.PurchaseApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "CheckoutRepository"
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
    
    suspend fun checkout(
        listingId: String,
        listingType: String,
        paymentMethod: String
    ): Result<CheckoutResponse> {
        return try {
            val token = getBearerToken()
            Log.d(TAG, "Checkout - listingId: $listingId, type: $listingType, payment: $paymentMethod")
            val request = CheckoutRequest(
                listingId = listingId,
                listingType = listingType,
                paymentMethod = paymentMethod
            )
            val response = checkoutApi.checkout(token, request)
            Log.d(TAG, "Checkout successful - transactionId: ${response.data.transactionId}")
            
            // For WALLET payment, need to confirm payment
            if (paymentMethod == "WALLET") {
                Log.d(TAG, "WALLET payment - confirming with pay-with-wallet API")
                val walletResponse = checkoutApi.payWithWallet(token, response.data.transactionId)
                Log.d(TAG, "Payment confirmed - status: ${walletResponse.data.status}")
                
                // Convert TransactionStatusResponse back to CheckoutResponse format
                val finalResponse = CheckoutResponse(
                    message = walletResponse.message,
                    data = CheckoutData(
                        transactionId = walletResponse.data.id,
                        paymentInfo = null,
                        paymentDetail = null
                    )
                )
                Result.success(finalResponse)
            } else {
                Result.success(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Checkout failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun getWalletBalance(): Result<WalletBalance> {
        return try {
            val token = getBearerToken()
            Log.d(TAG, "Fetching wallet balance")
            val response = walletApi.getWalletBalance(token)
            Log.d(TAG, "Wallet balance: ${response.data.availableBalance}")
            Result.success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get wallet balance", e)
            Result.failure(e)
        }
    }
    
    suspend fun pollTransactionStatus(
        transactionId: String,
        maxAttempts: Int = 10, // 10 attempts x 3 seconds = 30 seconds max
        delayMillis: Long = 3000L
    ): Result<String> {
        return try {
            val token = getBearerToken()
            
            repeat(maxAttempts) { attempt ->
                Log.d(TAG, "Polling transaction status - attempt ${attempt + 1}/$maxAttempts")
                
                // Get purchase history and find our transaction
                val historyResponse = purchaseApi.getMyPurchases(token, page = 1, limit = 20)
                val transaction = historyResponse.data.transactions.find { it.id == transactionId }
                
                if (transaction != null) {
                    val status = transaction.status
                    Log.d(TAG, "Transaction $transactionId status: $status")
                    
                    when (status) {
                        "COMPLETED" -> {
                            Log.d(TAG, "Transaction completed successfully!")
                            return Result.success(status)
                        }
                        "CANCELLED", "FAILED" -> {
                            Log.e(TAG, "Transaction failed with status: $status")
                            return Result.failure(Exception("Transaction $status"))
                        }
                        "PENDING", "PROCESSING" -> {
                            // Continue polling
                            if (attempt < maxAttempts - 1) {
                                kotlinx.coroutines.delay(delayMillis)
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Transaction not found yet, waiting...")
                    if (attempt < maxAttempts - 1) {
                        kotlinx.coroutines.delay(delayMillis)
                    }
                }
            }
            
            // Timeout after max attempts - but transaction was created successfully
            Log.w(TAG, "Transaction still PENDING after ${maxAttempts * delayMillis / 1000} seconds")
            // Return success with PENDING status - let user check purchase history
            Result.success("PENDING_TIMEOUT")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to poll transaction status", e)
            Result.failure(e)
        }
    }
}
