package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.BatteryResponse
import com.example.evsecondhand.data.model.VehicleResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {
    
    @GET("batteries/")
    suspend fun getBatteries(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): BatteryResponse
    
    @GET("vehicles/")
    suspend fun getVehicles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): VehicleResponse
}
