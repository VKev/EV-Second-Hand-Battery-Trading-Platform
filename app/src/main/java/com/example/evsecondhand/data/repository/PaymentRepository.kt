package com.example.evsecondhand.data.repository

import com.example.evsecondhand.data.model.CheckoutRequest
import com.example.evsecondhand.data.model.CheckoutResponse
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.model.WithdrawRequest
import com.example.evsecondhand.data.remote.CheckoutApiService

class PaymentRepository(
    private val api: CheckoutApiService,
    private val accessToken: String
) {

    private fun authHeader(): String = "Bearer $accessToken"

    suspend fun fetchWalletBalance(): Result<WalletBalance> = runCatching {
        api.getWalletBalance(authHeader()).data
    }

    suspend fun fetchWalletHistory(): Result<List<Transaction>> = runCatching {
        api.getWalletHistory(authHeader()).data.transactions
    }

    suspend fun submitWithdraw(amount: Long): Result<String?> = runCatching {
    api.requestWithdraw(authHeader(), WithdrawRequest(amount)).message
    }

    suspend fun initiateCheckout(
        listingId: String,
        listingType: String,
        paymentMethod: String
    ): Result<CheckoutResponse> = runCatching {
        api.checkout(
            authHeader(),
            CheckoutRequest(
                listingId = listingId,
                listingType = listingType,
                paymentMethod = paymentMethod
            )
        )
    }

    suspend fun confirmCheckoutPayment(transactionId: String): Result<String?> = runCatching {
        api.payWithWallet(authHeader(), transactionId).message
    }
}
