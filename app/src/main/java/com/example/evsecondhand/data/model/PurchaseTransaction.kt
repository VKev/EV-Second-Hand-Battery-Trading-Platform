package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseHistoryResponse(
    val message: String,
    val data: PurchaseHistoryData
)

@Serializable
data class PurchaseHistoryData(
    val transactions: List<PurchaseTransaction>,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val totalResults: Int
)

@Serializable
data class PurchaseTransaction(
    val id: String,
    val buyerId: String,
    val status: String, // COMPLETED, PENDING, CANCELLED
    val vehicleId: String? = null,
    val batteryId: String? = null,
    val finalPrice: Double,
    val paymentGateway: String, // WALLET, MOMO, etc.
    val paymentDetail: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val vehicle: PurchaseVehicle? = null,
    val battery: PurchaseBattery? = null,
    val review: PurchaseReview? = null
)

@Serializable
data class PurchaseVehicle(
    val id: String,
    val title: String,
    val images: List<String>
)

@Serializable
data class PurchaseBattery(
    val id: String,
    val title: String,
    val images: List<String>
)

@Serializable
data class PurchaseReview(
    val id: String,
    val rating: Int,
    val comment: String?
)

enum class PurchaseStatus {
    COMPLETED,
    PENDING,
    CANCELLED,
    PROCESSING
}