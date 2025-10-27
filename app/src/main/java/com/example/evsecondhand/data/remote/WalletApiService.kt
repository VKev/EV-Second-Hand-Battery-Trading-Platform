package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.TransactionHistoryResponse
import com.example.evsecondhand.data.model.WalletBalanceResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WalletApiService {
    
    @GET("wallet/")
    suspend fun getWalletBalance(
        @Header("Authorization") token: String
    ): WalletBalanceResponse
    
    @GET("wallet/history")
    suspend fun getTransactionHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): TransactionHistoryResponse
}