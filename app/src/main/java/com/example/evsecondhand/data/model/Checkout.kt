package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequest(
    val listingId: String,
    val listingType: String, // "VEHICLE" or "BATTERY"
    val paymentMethod: String // "MOMO" or "WALLET"
)

@Serializable
data class CheckoutResponse(
    val message: String,
    val data: CheckoutData
)

@Serializable
data class CheckoutData(
    val transactionId: String,
    val paymentInfo: PaymentInfo? = null, // For MoMo payment
    val paymentDetail: PaymentDetail? = null // For WALLET payment
)

@Serializable
data class PaymentInfo(
    val partnerCode: String? = null,
    val orderId: String? = null,
    val requestId: String? = null,
    val amount: Int? = null,
    val responseTime: Long? = null,
    val message: String? = null,
    val resultCode: Int? = null,
    val payUrl: String? = null,
    val deeplink: String? = null,
    val qrCodeUrl: String? = null,
    val deeplinkMiniApp: String? = null
)

@Serializable
data class PaymentDetail(
    val gateway: String? = null, // "MOMO" or "WALLET"
    val paymentDetail: String? = null, // JSON string or payment info
    val amount: Int? = null,
    val payUrl: String? = null
)

enum class ListingType(val value: String) {
    VEHICLE("VEHICLE"),
    BATTERY("BATTERY")
}

enum class PaymentMethod(val value: String, val displayName: String) {
    MOMO("MOMO", "Ví MoMo"),
    WALLET("WALLET", "Ví EV Market")
}
