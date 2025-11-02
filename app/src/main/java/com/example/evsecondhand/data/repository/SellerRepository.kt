package com.example.evsecondhand.data.repository

import com.example.evsecondhand.data.model.GenericServerMessageResponse
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.data.model.seller.BatteryItem
import com.example.evsecondhand.data.model.seller.BatteryItemFull
import com.example.evsecondhand.data.model.seller.CreateBatteryRequest
import com.example.evsecondhand.data.model.seller.CreateVehicleRequest
import com.example.evsecondhand.data.model.seller.UpdateBatteryRequest
import com.example.evsecondhand.data.model.seller.VehicleItem
import com.example.evsecondhand.data.remote.SellerApiService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class SellerRepository(
    private val api: SellerApiService,
    private val accessToken: String
) {

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private fun authHeader(): String = "Bearer $accessToken"

    suspend fun fetchVehicles(): Result<List<VehicleItem>> = runCatching {
        api.getMyVehicles(authHeader()).data.vehicles
    }

    suspend fun fetchBatteries(): Result<List<BatteryItem>> = runCatching {
        api.getMyBatteries(authHeader()).data.batteries
    }

    suspend fun fetchBatteryDetail(id: String): Result<BatteryItemFull> = runCatching {
        api.getBatteryDetail(authHeader(), id).data.battery
    }

    suspend fun editBattery(
        id: String,
        request: UpdateBatteryRequest
    ): Result<GenericServerMessageResponse> = runCatching {
        api.updateBattery(authHeader(), id, request)
    }

    suspend fun createVehicle(
        request: CreateVehicleRequest,
        imageParts: List<MultipartBody.Part>
    ): Result<Unit> = runCatching {
        require(imageParts.isNotEmpty()) { "At least one vehicle image is required." }
        val isAuctionListing = request.isAuction == true
        val payload = if (isAuctionListing) {
            request
        } else {
            request.copy(
                isAuction = null,
                startingPrice = null,
                bidIncrement = null,
                depositAmount = null
            )
        }
        val partMap = payload.toPartMap()
        if (isAuctionListing) {
            api.createVehicleAuction(
                authHeader(),
                partMap,
                imageParts
            )
        } else {
            api.createVehicle(
                authHeader(),
                partMap,
                imageParts
            )
        }
        Unit
    }

    suspend fun createBattery(
        request: CreateBatteryRequest,
        imageParts: List<MultipartBody.Part>
    ): Result<Unit> = runCatching {
        require(imageParts.isNotEmpty()) { "At least one battery image is required." }
        val isAuctionListing = request.isAuction == true
        val payload = if (isAuctionListing) {
            request
        } else {
            request.copy(
                isAuction = null,
                startingPrice = null,
                bidIncrement = null,
                depositAmount = null
            )
        }
        val partMap = payload.toPartMap()
        if (isAuctionListing) {
            api.createBatteryAuction(
                authHeader(),
                partMap,
                imageParts
            )
        } else {
            api.createBattery(
                authHeader(),
                partMap,
                imageParts
            )
        }
        Unit
    }

    private fun String.toTextBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun Long.toTextBody(): RequestBody = toString().toTextBody()
    private fun Int.toTextBody(): RequestBody = toString().toTextBody()

    private fun CreateVehicleRequest.toPartMap(): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()
        map["title"] = title.toTextBody()
        map["description"] = description.toTextBody()
        map["price"] = price.toTextBody()
        map["status"] = status.toTextBody()
        map["brand"] = brand.toTextBody()
        map["model"] = model.toTextBody()
        map["year"] = year.toTextBody()
        map["mileage"] = mileage.toTextBody()
        isAuction?.let { map["isAuction"] = it.toString().toTextBody() }
        startingPrice?.let { map["startingPrice"] = it.toTextBody() }
        bidIncrement?.let { map["bidIncrement"] = it.toTextBody() }
        depositAmount?.let { map["depositAmount"] = it.toTextBody() }
        specifications?.let { specs ->
            val specJson = buildJsonObject {
                putJsonObject("warranty") {
                    specs.warranty?.basic?.let { put("basic", it) }
                    specs.warranty?.battery?.let { put("battery", it) }
                    specs.warranty?.drivetrain?.let { put("drivetrain", it) }
                }
                putJsonObject("dimensions") {
                    specs.dimensions?.width?.let { put("width", it) }
                    specs.dimensions?.height?.let { put("height", it) }
                    specs.dimensions?.length?.let { put("length", it) }
                    specs.dimensions?.curbWeight?.let { put("curbWeight", it) }
                }
                putJsonObject("performance") {
                    specs.performance?.topSpeed?.let { put("topSpeed", it) }
                    specs.performance?.motorType?.let { put("motorType", it) }
                    specs.performance?.horsepower?.let { put("horsepower", it) }
                    specs.performance?.acceleration?.let { put("acceleration", it) }
                }
                putJsonObject("batteryAndCharging") {
                    specs.batteryAndCharging?.range?.let { put("range", it) }
                    specs.batteryAndCharging?.chargeTime?.let { put("chargeTime", it) }
                    specs.batteryAndCharging?.chargingSpeed?.let { put("chargingSpeed", it) }
                    specs.batteryAndCharging?.batteryCapacity?.let { put("batteryCapacity", it) }
                }
            }
            map["specifications"] = specJson.toString().toTextBody()
        }
        return map
    }

    private fun CreateBatteryRequest.toPartMap(): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()
        map["title"] = title.toTextBody()
        map["description"] = description.toTextBody()
        map["price"] = price.toTextBody()
        map["status"] = status.toTextBody()
        map["brand"] = brand.toTextBody()
        map["capacity"] = capacity.toTextBody()
        map["year"] = year.toTextBody()
        map["health"] = health.toTextBody()
        isAuction?.let { map["isAuction"] = it.toString().toTextBody() }
        startingPrice?.let { map["startingPrice"] = it.toTextBody() }
        bidIncrement?.let { map["bidIncrement"] = it.toTextBody() }
        depositAmount?.let { map["depositAmount"] = it.toTextBody() }
        specifications?.let { specs ->
            map["specifications"] = json.encodeToString(specs).toTextBody()
        }
        return map
    }
}
