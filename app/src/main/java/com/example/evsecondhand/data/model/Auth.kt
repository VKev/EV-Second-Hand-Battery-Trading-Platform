package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val message: String,
    val data: AuthData
)

@Serializable
data class AuthData(
    val user: User,
    val accessToken: String
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val role: String? = null,
    val isVerified: Boolean? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class LogoutResponse(
    val message: String
)
