package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.AuthApiService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepository(
    private val authApi: AuthApiService,
    private val context: Context
) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, password))
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveAuthData(token: String, user: User) {
        prefs.edit().apply {
            putString("access_token", token)
            putString("user", json.encodeToString(user))
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }
    
    fun getCurrentUser(): User? {
        val userJson = prefs.getString("user", null) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
