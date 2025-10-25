package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BatteryResponse(
    val message: String,
    val data: BatteryData
)

@Serializable
data class BatteryData(
    val batteries: List<Battery>,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val totalResults: Int
)

@Serializable
data class Battery(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val images: List<String>,
    val status: String,
    val brand: String,
    val capacity: Int,
    val year: Int,
    val health: Int?,
    val specifications: BatterySpecifications?,
    val isVerified: Boolean,
    val isAuction: Boolean? = null,
    val auctionStartsAt: String? = null,
    val auctionEndsAt: String? = null,
    val startingPrice: Int? = null,
    val bidIncrement: Int? = null,
    val depositAmount: Int? = null,
    val auctionRejectionReason: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val sellerId: String,
    val seller: Seller? = null
)

@Serializable
data class BatterySpecifications(
    val weight: String? = null,
    val voltage: String? = null,
    val chemistry: String? = null,
    val degradation: String? = null,
    val chargingTime: String? = null,
    val installation: String? = null,
    val warrantyPeriod: String? = null,
    val temperatureRange: String? = null
)

@Serializable
data class Seller(
    val id: String,
    val name: String,
    val avatar: String? = null
)

@Serializable
data class BatteryDetailResponse(
    val message: String,
    val data: BatteryDetailData
)

@Serializable
data class BatteryDetailData(
    val battery: Battery
)
