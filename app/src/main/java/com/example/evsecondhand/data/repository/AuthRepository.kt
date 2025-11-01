package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.AuthApiService
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.remote.ExchangeCodeRequest
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

    init {
        RetrofitClient.setTokenProvider { getAccessToken() }
        RetrofitClient.updateAccessToken(getAccessToken())
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
        RetrofitClient.updateAccessToken(token)
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
    
    suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Log error but still clear local data
        } finally {
            clearLocalData()
        }
    }
    
    fun clearLocalData() {
        prefs.edit().clear().apply()
        RetrofitClient.updateAccessToken(null)
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    suspend fun loginWithGoogle(): Result<String> = runCatching {
        authApi.getGoogleAuthUrl().url
    }

    fun openGoogleOAuth(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    suspend fun exchangeCodeForToken(code: String): Result<AuthResponse> {
        return try {
            val response = authApi.exchangeCode(ExchangeCodeRequest(code))
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
