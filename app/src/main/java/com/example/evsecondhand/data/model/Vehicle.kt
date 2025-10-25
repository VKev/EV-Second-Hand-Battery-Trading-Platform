package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponse(
    val message: String,
    val data: VehicleData
)

@Serializable
data class VehicleData(
    val vehicles: List<Vehicle>,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val totalResults: Int
)

@Serializable
data class Vehicle(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val images: List<String>,
    val status: String,
    val brand: String,
    val model: String,
    val year: Int,
    val mileage: Int,
    val specifications: VehicleSpecifications? = null,
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
data class VehicleSpecifications(
    val warranty: Warranty? = null,
    val dimensions: Dimensions? = null,
    val performance: Performance? = null,
    val batteryAndCharging: BatteryAndCharging? = null
)

@Serializable
data class Warranty(
    val basic: String? = null,
    val battery: String? = null,
    val drivetrain: String? = null
)

@Serializable
data class Dimensions(
    val width: String? = null,
    val height: String? = null,
    val length: String? = null,
    val curbWeight: String? = null
)

@Serializable
data class Performance(
    val topSpeed: String? = null,
    val motorType: String? = null,
    val horsepower: String? = null,
    val acceleration: String? = null
)

@Serializable
data class BatteryAndCharging(
    val range: String? = null,
    val chargeTime: String? = null,
    val chargingSpeed: String? = null,
    val batteryCapacity: String? = null
)

@Serializable
data class VehicleDetailResponse(
    val message: String,
    val data: VehicleDetailData
)

@Serializable
data class VehicleDetailData(
    val vehicle: Vehicle
)
