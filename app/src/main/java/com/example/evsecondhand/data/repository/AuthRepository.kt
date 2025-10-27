package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.AuthApiService
import com.example.evsecondhand.data.remote.ExchangeCodeRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepository(
    private val authApi: AuthApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
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
    
    suspend fun loginWithGoogle(): Result<String> {
        return try {
            Log.d(TAG, "Starting Google OAuth - Getting auth URL from backend")
            // Get Google OAuth URL from backend
            val response = authApi.getGoogleAuthUrl(clientType = "mobile")
            Log.d(TAG, "Received Google OAuth URL: ${response.url}")
            Result.success(response.url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Google OAuth URL", e)
            Result.failure(e)
        }
    }
    
    suspend fun exchangeCodeForToken(code: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Exchanging OAuth code for token - code length: ${code.length}")
            val response = authApi.exchangeCode(ExchangeCodeRequest(code))
            Log.d(TAG, "Successfully exchanged code - User: ${response.data.user.email}")
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exchange OAuth code", e)
            Result.failure(e)
        }
    }
    
    fun openGoogleOAuth(url: String) {
        try {
            Log.d(TAG, "Opening Google OAuth URL in Custom Tab")
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            Log.w(TAG, "Custom Tabs not available, falling back to browser", e)
            // Fallback to browser if Custom Tabs not available
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
    
    private fun saveAuthData(token: String, user: User) {
        Log.d(TAG, "Saving auth data - User: ${user.email}")
        prefs.edit().apply {
            putString("access_token", token)
            putString("user", json.encodeToString(user))
            apply()
        }
        Log.d(TAG, "Auth data saved successfully")
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
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
