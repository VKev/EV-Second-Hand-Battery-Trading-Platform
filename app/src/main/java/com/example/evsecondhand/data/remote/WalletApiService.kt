package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.DepositRequest
import com.example.evsecondhand.data.model.DepositResponse
import com.example.evsecondhand.data.model.TransactionHistoryResponse
import com.example.evsecondhand.data.model.WalletBalanceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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
    
    @POST("wallet/deposit")
    suspend fun depositFunds(
        @Header("Authorization") token: String,
        @Body request: DepositRequest
    ): DepositResponse
}