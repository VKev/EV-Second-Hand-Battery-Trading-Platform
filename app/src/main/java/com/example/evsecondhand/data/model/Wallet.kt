package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalance(
    val balance: Double,
    val currency: String = "VND"
)

@Serializable
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val lastFourDigits: String,
    val isActive: Boolean,
    val displayName: String
)

enum class PaymentMethodType {
    BANK_ACCOUNT,
    CREDIT_CARD,
    PAYPAL
}

@Serializable
data class AuctionFundsHold(
    val amount: Double,
    val description: String,
    val currency: String = "VND"
)

@Serializable
data class Transaction(
    val id: String,
    val date: String,
    val type: TransactionType,
    val description: String,
    val amount: Double,
    val currency: String = "VND"
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    PURCHASE,
    AUCTION_BID
}