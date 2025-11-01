package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.GenericServerMessageResponse
import com.example.evsecondhand.data.model.VehicleDetailResponse
import com.example.evsecondhand.data.model.seller.BatteryDetailResponse
import com.example.evsecondhand.data.model.seller.BatteryListResponse
import com.example.evsecondhand.data.model.seller.UpdateBatteryRequest
import com.example.evsecondhand.data.model.seller.VehicleListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface SellerApiService {

    @GET("users/me/vehicles")
    suspend fun getMyVehicles(
        @Header("Authorization") authHeader: String
    ): VehicleListResponse

    @GET("users/me/batteries")
    suspend fun getMyBatteries(
        @Header("Authorization") authHeader: String
    ): BatteryListResponse

    @GET("batteries/{id}")
    suspend fun getBatteryDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): BatteryDetailResponse

    @PATCH("batteries/{id}")
    suspend fun updateBattery(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Body request: UpdateBatteryRequest
    ): GenericServerMessageResponse

    @Multipart
    @POST("vehicles/")
    suspend fun createVehicle(
        @Header("Authorization") authHeader: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>
    ): VehicleDetailResponse

    @Multipart
    @POST("auctions/vehicles/")
    suspend fun createVehicleAuction(
        @Header("Authorization") authHeader: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>
    ): VehicleDetailResponse

    @Multipart
    @POST("batteries/")
    suspend fun createBattery(
        @Header("Authorization") authHeader: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>
    ): BatteryDetailResponse

    @Multipart
    @POST("auctions/batteries/")
    suspend fun createBatteryAuction(
        @Header("Authorization") authHeader: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part images: List<MultipartBody.Part>
    ): BatteryDetailResponse
}
