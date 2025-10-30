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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class SellerRepository(
    private val api: SellerApiService,
    private val accessToken: String
) {

    private fun authHeader(): String = "Bearer $accessToken"

    suspend fun fetchVehicles(): Result<List<VehicleItem>> = runCatching {
        api.getMyVehicles(authHeader()).data.vehicles
    }

    suspend fun fetchBatteries(): Result<List<BatteryItem>> = runCatching {
        api.getMyBatteries(authHeader()).data.results
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
            specs.warranty?.basic?.let { map["specifications[warranty][basic]"] = it.toTextBody() }
            specs.warranty?.battery?.let { map["specifications[warranty][battery]"] = it.toTextBody() }
            specs.warranty?.drivetrain?.let { map["specifications[warranty][drivetrain]"] = it.toTextBody() }
            specs.dimensions?.width?.let { map["specifications[dimensions][width]"] = it.toTextBody() }
            specs.dimensions?.height?.let { map["specifications[dimensions][height]"] = it.toTextBody() }
            specs.dimensions?.length?.let { map["specifications[dimensions][length]"] = it.toTextBody() }
            specs.dimensions?.curbWeight?.let { map["specifications[dimensions][curbWeight]"] = it.toTextBody() }
            specs.performance?.topSpeed?.let { map["specifications[performance][topSpeed]"] = it.toTextBody() }
            specs.performance?.motorType?.let { map["specifications[performance][motorType]"] = it.toTextBody() }
            specs.performance?.horsepower?.let { map["specifications[performance][horsepower]"] = it.toTextBody() }
            specs.performance?.acceleration?.let { map["specifications[performance][acceleration]"] = it.toTextBody() }
            specs.batteryAndCharging?.range?.let { map["specifications[batteryAndCharging][range]"] = it.toTextBody() }
            specs.batteryAndCharging?.chargeTime?.let { map["specifications[batteryAndCharging][chargeTime]"] = it.toTextBody() }
            specs.batteryAndCharging?.chargingSpeed?.let { map["specifications[batteryAndCharging][chargingSpeed]"] = it.toTextBody() }
            specs.batteryAndCharging?.batteryCapacity?.let { map["specifications[batteryAndCharging][batteryCapacity]"] = it.toTextBody() }
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
            specs.weight?.let { map["specifications[weight]"] = it.toTextBody() }
            specs.voltage?.let { map["specifications[voltage]"] = it.toTextBody() }
            specs.chemistry?.let { map["specifications[chemistry]"] = it.toTextBody() }
            specs.degradation?.let { map["specifications[degradation]"] = it.toTextBody() }
            specs.chargingTime?.let { map["specifications[chargingTime]"] = it.toTextBody() }
            specs.installation?.let { map["specifications[installation]"] = it.toTextBody() }
            specs.warrantyPeriod?.let { map["specifications[warrantyPeriod]"] = it.toTextBody() }
            specs.temperatureRange?.let { map["specifications[temperatureRange]"] = it.toTextBody() }
        }
        return map
    }
}
