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
    val specifications: VehicleSpecifications,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val sellerId: String
)

@Serializable
data class VehicleSpecifications(
    val warranty: Warranty,
    val dimensions: Dimensions,
    val performance: Performance,
    val batteryAndCharging: BatteryAndCharging
)

@Serializable
data class Warranty(
    val basic: String,
    val battery: String,
    val drivetrain: String
)

@Serializable
data class Dimensions(
    val width: String,
    val height: String,
    val length: String,
    val curbWeight: String
)

@Serializable
data class Performance(
    val topSpeed: String,
    val motorType: String,
    val horsepower: String,
    val acceleration: String
)

@Serializable
data class BatteryAndCharging(
    val range: String,
    val chargeTime: String,
    val chargingSpeed: String,
    val batteryCapacity: String
)
