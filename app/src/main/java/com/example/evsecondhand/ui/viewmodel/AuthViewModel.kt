package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.R
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object ExchangingCode : AuthState() // <-- Trạng thái mới
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(RetrofitClient.authApi, application)
    private val googleSignInClient: GoogleSignInClient

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, options)

        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val user = repository.getCurrentUser()
        if (user != null) {
            _isLoggedIn.value = true
            _authState.value = AuthState.Success(user)
        } else {
            _isLoggedIn.value = false
            _authState.value = AuthState.LoggedOut
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.login(email, password)

            result.onSuccess { response ->
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.data.user)
            }.onFailure { exception ->
                Log.e(TAG, "Email login failed", exception)
                _authState.value = AuthState.Error(
                    exception.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.register(name, email, password)

            result.onSuccess { response ->
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.data.user)
            }.onFailure { exception ->
                Log.e(TAG, "Registration failed", exception)
                _authState.value = AuthState.Error(
                    exception.message ?: "Registration failed. Please try again."
                )
            }
        }
    }

    fun onGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.signInWithGoogleIdToken(idToken)

            result.onSuccess { response ->
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.data.user)
            }.onFailure { exception ->
                Log.e(TAG, "Google sign-in failed", exception)
                _authState.value = AuthState.Error(
                    exception.message ?: "Google authentication failed. Please try again."
                )
            }
        }
    }

    fun exchangeAuthCodeForToken(code: String) {
        // Tránh gọi lại nếu đang xử lý
        if (_authState.value is AuthState.ExchangingCode || _authState.value is AuthState.Loading) return

        viewModelScope.launch {
            _authState.value = AuthState.ExchangingCode // <-- Set trạng thái đang trao đổi
            val result = repository.exchangeAuthCodeForToken(code)
            result.onSuccess { response ->
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.data.user)
            }.onFailure { exception ->
                Log.e(TAG, "Auth code exchange failed", exception)
                _authState.value = AuthState.Error(
                    exception.message ?: "Authentication failed. Please try again."
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                Log.w(TAG, "Repository logout failed, clearing local state", e)
                repository.clearLocalData()
            } finally {
                try {
                    googleSignInClient.signOut().await()
                } catch (signOutError: Exception) {
                    Log.w(TAG, "Google sign-out failed", signOutError)
                }
                _isLoggedIn.value = false
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun getAccessToken(): String? = repository.getAccessToken()

    fun getCurrentUser(): User? = repository.getCurrentUser()

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    companion object {
        private const val TAG = "AuthViewModel"
    }
}

