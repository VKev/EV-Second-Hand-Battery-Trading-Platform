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
    private var pendingWalletRequest: CheckoutRequest? = null

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
        val request = CheckoutRequest(
            listingId = listingId,
            listingType = listingType,
            paymentMethod = paymentMethod
        )

        if (paymentMethod == "WALLET") {
            pendingWalletRequest = request
        } else {
            pendingWalletRequest = null
        }

        api.checkout(authHeader(), request)
    }

    suspend fun confirmCheckoutPayment(transactionId: String): Result<String?> = runCatching {
        val walletRequest = pendingWalletRequest
            ?: throw IllegalStateException("No pending wallet checkout request available to confirm.")
        api.payWithWallet(authHeader(), transactionId, walletRequest).also {
            pendingWalletRequest = null
        }.message
    }
}
