package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.CheckoutRequestBody
import com.example.evsecondhand.data.model.CheckoutResponse
import com.example.evsecondhand.data.model.GenericServerMessageResponse
import com.example.evsecondhand.data.model.TransactionHistoryResponse
import com.example.evsecondhand.data.model.WalletBalanceResponse
import com.example.evsecondhand.data.model.WithdrawRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApiService {

    @GET("wallet/")
    suspend fun getWalletBalance(
        @Header("Authorization") authHeader: String
    ): WalletBalanceResponse

    @GET("wallet/history")
    suspend fun getWalletHistory(
        @Header("Authorization") authHeader: String
    ): TransactionHistoryResponse

    @POST("wallet/withdraw")
    suspend fun requestWithdraw(
        @Header("Authorization") authHeader: String,
        @Body body: WithdrawRequestBody
    ): GenericServerMessageResponse

    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") authHeader: String,
        @Body body: CheckoutRequestBody
    ): CheckoutResponse

    @POST("checkout/{transactionId}/pay-with-wallet")
    suspend fun payWithWallet(
        @Header("Authorization") authHeader: String,
        @Path("transactionId") transactionId: String
    ): GenericServerMessageResponse
}
