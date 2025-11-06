package com.example.evsecondhand.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.evsecondhand.data.model.AuthResponse
import com.example.evsecondhand.data.model.LoginRequest
import com.example.evsecondhand.data.model.RegisterRequest
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.AuthApiService
import com.example.evsecondhand.data.model.ExchangeCodeRequest
import com.example.evsecondhand.data.remote.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import retrofit2.HttpException
import java.time.Instant

class AuthRepository(
    private val authApi: AuthApiService,
    private val context: Context
) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val FIREBASE_DATABASE_URL =
            "https://prm-fire-base-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase =
        FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
    private val usersRef = firebaseDatabase.getReference("users")

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
            Log.e(TAG, "Login failed", e)
            val errorMessage = extractErrorMessage(e)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, password))
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            val errorMessage = extractErrorMessage(e)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun exchangeAuthCodeForToken(code: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Exchanging auth code for token")
            val request = ExchangeCodeRequest(code = code)
            val response = authApi.exchangeCodeForToken(request)
            saveAuthData(response.data.accessToken, response.data.user)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exchange code for token", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Signing in with Google ID token via Firebase")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: error("Firebase user is null after Google sign-in")

            ensureUserProfile(firebaseUser)

            val backendResponse = obtainBackendSession(firebaseUser)
            saveAuthData(backendResponse.data.accessToken, backendResponse.data.user)
            Log.d(TAG, "Google sign-in successful - User: ${backendResponse.data.user.email}")

            Result.success(backendResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in with Firebase failed", e)
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
        } catch (_: Exception) {
            // ignore to ensure local cleanup still runs
        } finally {
            firebaseAuth.signOut()
            clearLocalData()
        }
    }

    fun clearLocalData() {
        prefs.edit().clear().apply()
        RetrofitClient.updateAccessToken(null)
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    private suspend fun ensureUserProfile(firebaseUser: FirebaseUser) {
        try {
            val userNode = usersRef.child(firebaseUser.uid)
            val snapshot = userNode.get().await()
            if (!snapshot.exists()) {
                val username = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                    ?: firebaseUser.email.orEmpty()
                val payload = mapOf(
                    "uid" to firebaseUser.uid,
                    "username" to username,
                    "email" to firebaseUser.email,
                    "createdAt" to ServerValue.TIMESTAMP
                )
                userNode.setValue(payload).await()
                Log.d(TAG, "Firebase user profile created for ${firebaseUser.email}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to ensure Firebase profile", e)
        }
    }

    private suspend fun obtainBackendSession(firebaseUser: FirebaseUser): AuthResponse {
        val email = firebaseUser.email ?: error("Missing email from Google account")
        val displayName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore('@')

        val derivedPassword = "firebase-${firebaseUser.uid}"

        val loginAttempt = runCatching {
            authApi.login(LoginRequest(email, derivedPassword))
        }

        return loginAttempt.getOrElse { loginError ->
            runCatching {
                authApi.register(RegisterRequest(displayName, email, derivedPassword))
            }.getOrElse { registerError ->
                Log.e(TAG, "Backend sync failed: login=${loginError.message}, register=${registerError.message}")
                throw loginError
            }
        }
    }

    private fun FirebaseUser.toDomainUser(): User {
        val createdAt = metadata?.creationTimestamp?.let { Instant.ofEpochMilli(it).toString() } ?: ""
        val updatedAt = metadata?.lastSignInTimestamp?.let { Instant.ofEpochMilli(it).toString() } ?: createdAt

        return User(
            id = uid,
            email = email.orEmpty(),
            name = displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty(),
            avatar = photoUrl?.toString(),
            role = "customer",
            isVerified = isEmailVerified,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun extractErrorMessage(exception: Throwable): String {
        if (exception is HttpException) {
            val rawBody = try {
                exception.response()?.errorBody()?.string()
            } catch (ignored: Exception) {
                null
            }?.trim().orEmpty()

            Log.d(TAG, "Raw error body: $rawBody")

            if (rawBody.isNotBlank()) {
                try {
                    val json = JSONObject(rawBody)
                    val message = json.optString("message").takeIf { it.isNotBlank() }
                        ?: json.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: json.optJSONArray("errors")?.let { errors ->
                            (0 until errors.length()).asSequence()
                                .mapNotNull { errors.optString(it).takeIf { s -> s.isNotBlank() } }
                                .firstOrNull()
                        }

                    if (!message.isNullOrBlank()) {
                        Log.d(TAG, "Extracted message: $message")
                        return message
                    }
                } catch (ignored: Exception) {
                    Log.e(TAG, "Error parsing JSON", ignored)
                }
            }

            // Fallback to HTTP error messages
            return when (exception.code()) {
                400 -> "Thông tin đăng nhập không hợp lệ. Vui lòng kiểm tra lại."
                401 -> "Email hoặc mật khẩu không đúng."
                403 -> "Bạn không có quyền truy cập."
                404 -> "Không tìm thấy tài khoản."
                409 -> "Email đã được sử dụng."
                422 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
                in 500..599 -> "Lỗi máy chủ. Vui lòng thử lại sau."
                else -> "Lỗi kết nối (HTTP ${exception.code()}). Vui lòng thử lại."
            }
        }

        val message = exception.message.orEmpty()
        return when {
            message.contains("Unable to resolve host", ignoreCase = true) ->
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối Internet."
            message.contains("timeout", ignoreCase = true) ->
                "Kết nối bị timeout. Vui lòng thử lại."
            else -> exception.message ?: "Đã xảy ra lỗi. Vui lòng thử lại."
        }
    }
}
