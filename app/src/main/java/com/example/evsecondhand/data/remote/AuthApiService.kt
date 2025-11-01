package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.LogoutResponse
import com.example.evsecondhand.data.model.ExchangeCodeRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/logout")
    suspend fun logout(): LogoutResponse

    @POST("auth/exchange-code")
    suspend fun exchangeCodeForToken(@Body request: ExchangeCodeRequest): AuthResponse
}
