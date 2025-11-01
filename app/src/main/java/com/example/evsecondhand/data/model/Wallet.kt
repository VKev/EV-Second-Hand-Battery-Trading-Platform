package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

// Wallet Balance Models
@Serializable
data class WalletBalanceResponse(
    val message: String,
    val data: WalletBalance
)

@Serializable
data class WalletBalance(
    val id: String,
    val userId: String,
    val availableBalance: Double,
    val lockedBalance: Double,
    val createdAt: String,
    val updatedAt: String
)

// Transaction History Models
@Serializable
data class TransactionHistoryResponse(
    val message: String,
    val data: TransactionHistoryData
)

@Serializable
data class TransactionHistoryData(
    val transactions: List<Transaction>,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val totalResults: Int
)

@Serializable
data class Transaction(
    val id: String,
    val walletId: String,
    val type: String, // DEPOSIT, WITHDRAWAL, AUCTION_DEPOSIT, etc.
    val amount: Double,
    val status: String, // COMPLETED, PENDING, CANCELLED
    val gateway: String, // MOMO, INTERNAL, etc.
    val gatewayTransId: String? = null,
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    PURCHASE,
    AUCTION_DEPOSIT,
    AUCTION_BID
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    CANCELLED
}

// Deposit Models
@Serializable
data class DepositRequest(
    val amount: Int
)

@Serializable
data class DepositResponse(
    val message: String,
    val data: DepositData
)

@Serializable
data class DepositData(
    val partnerCode: String,
    val orderId: String,
    val requestId: String,
    val amount: Int,
    val responseTime: Long,
    val message: String,
    val resultCode: Int,
    val payUrl: String,
    val deeplink: String,
    val qrCodeUrl: String,
    val deeplinkMiniApp: String
)

// Withdraw request (used by wallet/checkout endpoints)
@Serializable
data class WithdrawRequest(
    val amount: Long
)

// Generic server message response used across multiple APIs
@Serializable
data class GenericServerMessageResponse(
    val message: String? = null
)