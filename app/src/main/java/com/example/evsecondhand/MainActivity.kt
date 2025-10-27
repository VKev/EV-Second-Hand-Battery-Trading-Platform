package com.example.evsecondhand

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.ui.navigation.AppNavigation
import com.example.evsecondhand.ui.theme.EVSecondHandTheme
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    private var pendingOAuthCode: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check for OAuth callback in intent
        handleIntent(intent)
        
        setContent {
            EVSecondHandTheme {
                val homeViewModel: HomeViewModel = viewModel()
                
                // Process pending OAuth code if any
                pendingOAuthCode?.let { code ->
                    Log.d("MainActivity", "Processing pending OAuth code: $code")
                    authViewModel.handleGoogleCallback(code)
                    pendingOAuthCode = null
                }
                
                AppNavigation(
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        Log.d("MainActivity", "handleIntent called with URI: $uri")
        
        uri?.let {
            // Check if this is an OAuth callback
            // Support both evmarket:// and https:// schemes
            val isOAuthCallback = (it.scheme == "evmarket" && it.host == "oauth") ||
                                  (it.scheme == "https" && it.host == "evmarket.app" && it.path == "/oauth/callback")
            
            if (isOAuthCallback) {
                val code = it.getQueryParameter("code")
                val error = it.getQueryParameter("error")
                
                Log.d("MainActivity", "OAuth callback detected - code: $code, error: $error")
                
                when {
                    code != null -> {
                        // Store code to process after Compose is ready
                        pendingOAuthCode = code
                        // If already initialized, process immediately
                        authViewModel.handleGoogleCallback(code)
                    }
                    error != null -> {
                        Log.e("MainActivity", "OAuth error: $error")
                        // Handle OAuth error (user cancelled, etc.)
                        authViewModel.handleOAuthError(error)
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // ViewModels will be automatically cleared
    }
}