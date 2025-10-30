package com.example.evsecondhand.data.repository

import com.example.evsecondhand.data.model.CheckoutRequestBody
import com.example.evsecondhand.data.model.CheckoutResponse
import com.example.evsecondhand.data.model.TransactionItem
import com.example.evsecondhand.data.model.WalletBalance
import com.example.evsecondhand.data.model.WithdrawRequestBody
import com.example.evsecondhand.data.remote.PaymentApiService

class PaymentRepository(
    private val api: PaymentApiService,
    private val accessToken: String
) {

    private fun authHeader(): String = "Bearer $accessToken"

    suspend fun fetchWalletBalance(): Result<WalletBalance> = runCatching {
        api.getWalletBalance(authHeader()).data
    }

    suspend fun fetchWalletHistory(): Result<List<TransactionItem>> = runCatching {
        api.getWalletHistory(authHeader()).data
    }

    suspend fun submitWithdraw(amount: Long): Result<String?> = runCatching {
        api.requestWithdraw(authHeader(), WithdrawRequestBody(amount)).message
    }

    suspend fun initiateCheckout(
        listingId: String,
        listingType: String,
        paymentMethod: String
    ): Result<CheckoutResponse> = runCatching {
        api.checkout(
            authHeader(),
            CheckoutRequestBody(
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
