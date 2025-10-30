package com.example.evsecondhand.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.User
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AuthRepository(RetrofitClient.authApi, application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
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
                _authState.value = AuthState.Error(
                    exception.message ?: "Registration failed. Please try again."
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _isLoggedIn.value = false
                _authState.value = AuthState.LoggedOut
            } catch (e: Exception) {
                // Even if API call fails, still logout locally
                repository.clearLocalData()
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
}
