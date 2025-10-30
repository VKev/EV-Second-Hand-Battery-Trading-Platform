package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.CheckoutRequest
import com.example.evsecondhand.data.model.CheckoutResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CheckoutApiService {
    
    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): CheckoutResponse
    
    @POST("checkout/{transactionId}/pay-with-wallet")
    suspend fun payWithWallet(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): com.example.evsecondhand.data.model.TransactionStatusResponse
}
