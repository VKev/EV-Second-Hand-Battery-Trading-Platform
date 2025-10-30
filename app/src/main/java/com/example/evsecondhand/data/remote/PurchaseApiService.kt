package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.PurchaseHistoryResponse
import com.example.evsecondhand.data.model.TransactionStatusResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PurchaseApiService {
    
    @GET("transactions/me")
    suspend fun getMyPurchases(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): PurchaseHistoryResponse
    
    @GET("transactions/{transactionId}")
    suspend fun getTransactionStatus(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): TransactionStatusResponse
}