package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.LogoutResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/logout")
    suspend fun logout(): LogoutResponse
    
    @GET("auth/google")
    suspend fun getGoogleAuthUrl(@Query("client_type") clientType: String = "mobile"): GoogleAuthUrlResponse
    
    @POST("auth/exchange-code")
    suspend fun exchangeCode(@Body request: ExchangeCodeRequest): AuthResponse
}

@Serializable
data class GoogleAuthUrlResponse(
    val url: String
)

@Serializable
data class ExchangeCodeRequest(
    val code: String
)
