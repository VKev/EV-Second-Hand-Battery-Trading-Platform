package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.BatteryDetailResponse
import com.example.evsecondhand.data.model.BatteryResponse
import com.example.evsecondhand.data.model.VehicleDetailResponse
import com.example.evsecondhand.data.model.VehicleResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface ProductApiService {
    
    @GET("batteries/")
    suspend fun getBatteries(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): BatteryResponse
    
    @GET("batteries/{id}")
    suspend fun getBatteryDetail(
        @Path("id") id: String
    ): BatteryDetailResponse
    
    @GET("vehicles/")
    suspend fun getVehicles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): VehicleResponse

    @GET("vehicles/{id}")
    suspend fun getVehicleDetail(
        @Path("id") id: String
    ): VehicleDetailResponse
}
