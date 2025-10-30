package com.example.evsecondhand.data.model.seller

import com.example.evsecondhand.data.model.VehicleSpecifications
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleListResponse(
    val message: String,
    val data: VehicleListWrapper
)

@Serializable
data class VehicleListWrapper(
    val vehicles: List<VehicleItem> = emptyList()
)

@Serializable
data class VehicleItem(
    val id: String,
    val title: String,
    val price: Long,
    val images: List<String> = emptyList(),
    val status: String,
    val brand: String,
    val model: String,
    val year: Int,
    val mileage: Long,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BatteryListResponse(
    val message: String,
    val data: BatteryListWrapper
)

@Serializable
data class BatteryListWrapper(
    val results: List<BatteryItem> = emptyList()
)

@Serializable
data class BatteryItem(
    val id: String,
    val title: String,
    val price: Long,
    val images: List<String> = emptyList(),
    val status: String,
    val brand: String,
    val capacity: Int,
    val year: Int,
    val health: Int = 0,
    val isAuction: Boolean = false,
    val auctionStartsAt: String? = null,
    val auctionEndsAt: String? = null,
    val startingPrice: Long? = null,
    val bidIncrement: Long? = null,
    val depositAmount: Long? = null,
    val isVerified: Boolean,
    val createdAt: String,
    val auctionRejectionReason: String? = null
)

@Serializable
data class BatteryDetailResponse(
    val message: String,
    val data: BatteryDetailWrapper
)

@Serializable
data class BatteryDetailWrapper(
    val battery: BatteryItemFull
)

@Serializable
data class BatteryItemFull(
    val id: String,
    val title: String,
    val description: String,
    val price: Long,
    val images: List<String> = emptyList(),
    val status: String,
    val brand: String,
    val capacity: Int,
    val year: Int,
    val health: Int = 0,
    val specifications: BatterySpecifications? = null,
    val isAuction: Boolean = false,
    val auctionStartsAt: String? = null,
    val auctionEndsAt: String? = null,
    val startingPrice: Long? = null,
    val bidIncrement: Long? = null,
    val depositAmount: Long? = null,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String? = null,
    val sellerId: String,
    val auctionRejectionReason: String? = null
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

/**
 * Simple request body for PATCH updates.
 *
 * NOTE: The production API expects multipart/form-data when uploading new images.
 * For brevity we keep the contract as JSON and delegate actual multipart assembly
 * to higher layers when needed.
 */
@Serializable
data class UpdateBatteryRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val brand: String? = null,
    val capacity: Int? = null,
    val year: Int? = null,
    val health: Int? = null,
    val specifications: BatterySpecifications? = null,
    @SerialName("images")
    val imageUrls: List<String>? = null
)

data class CreateVehicleRequest(
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val brand: String,
    val model: String,
    val year: Int,
    val mileage: Long,
    val specifications: VehicleSpecifications? = null,
    val isAuction: Boolean? = null,
    val startingPrice: Long? = null,
    val bidIncrement: Long? = null,
    val depositAmount: Long? = null
)

data class CreateBatteryRequest(
    val title: String,
    val description: String,
    val price: Long,
    val status: String,
    val brand: String,
    val capacity: Int,
    val year: Int,
    val health: Int,
    val specifications: BatterySpecifications? = null,
    val isAuction: Boolean? = null,
    val startingPrice: Long? = null,
    val bidIncrement: Long? = null,
    val depositAmount: Long? = null
)
