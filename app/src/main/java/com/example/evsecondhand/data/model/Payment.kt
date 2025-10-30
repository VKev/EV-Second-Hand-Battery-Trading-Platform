package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceResponse(
    val message: String,
    val data: WalletBalance
)

@Serializable
data class WalletBalance(
    val id: String,
    val userId: String,
    val availableBalance: Long,
    val lockedBalance: Long,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class TransactionHistoryResponse(
    val message: String,
    val data: List<TransactionItem>
)

@Serializable
data class TransactionItem(
    val id: String,
    val type: String,
    val amount: Long,
    val status: String,
    val description: String,
    val createdAt: String
)

@Serializable
data class WithdrawRequestBody(
    val amount: Long
)

@Serializable
data class GenericServerMessageResponse(
    val message: String? = null
)

@Serializable
data class CheckoutRequestBody(
    val listingId: String,
    val listingType: String,
    val paymentMethod: String
)

@Serializable
data class CheckoutResponse(
    val message: String,
    val data: CheckoutData? = null
)

@Serializable
data class CheckoutData(
    val transactionId: String,
    val paymentInfo: CheckoutPaymentInfo? = null
)

@Serializable
data class CheckoutPaymentInfo(
    val partnerCode: String? = null,
    val orderId: String? = null,
    val requestId: String? = null,
    val amount: Long? = null,
    val responseTime: Long? = null,
    val message: String? = null,
    val resultCode: Int? = null,
    val payUrl: String? = null,
    val deeplink: String? = null,
    val qrCodeUrl: String? = null,
    val deeplinkMiniApp: String? = null
)
