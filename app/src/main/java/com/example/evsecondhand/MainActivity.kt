package com.example.evsecondhand

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.data.zalopay.ZaloPaySDKHelper
import com.example.evsecondhand.ui.navigation.AppNavigation
import com.example.evsecondhand.ui.theme.EVSecondHandTheme
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize ZaloPay SDK
        ZaloPaySDKHelper.init()
        
        // Handle deep link if the app is launched from it
        handleAuthDeepLink(intent)
        handleZaloPayDeepLink(intent)

        setContent {
            EVSecondHandTheme {
                val homeViewModel: HomeViewModel = viewModel()

                AppNavigation(
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link if the app is already running
        handleAuthDeepLink(intent)
        handleZaloPayDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        val action = intent?.action
        val data: Uri? = intent?.data

        if (Intent.ACTION_VIEW == action && data != null) {
            if (data.scheme == "evmarket" && data.host == "auth-callback") {
                val code = data.getQueryParameter("code")
                if (!code.isNullOrBlank()) {
                    Log.d(TAG, "Received auth code from backend: $code")
                    authViewModel.exchangeAuthCodeForToken(code)
                } else {
                    Log.w(TAG, "Auth callback received but code is missing.")
                }
            }
        }
    }
    
    private fun handleZaloPayDeepLink(intent: Intent?) {
        val action = intent?.action
        val data: Uri? = intent?.data
        
        if (Intent.ACTION_VIEW == action && data != null) {
            Log.d(TAG, "Deep link received: $data")
            
            // Check if this is from ZaloPay (evmarket://app)
            if (data.scheme == "evmarket" && data.host == "app") {
                Log.d(TAG, "ZaloPay payment callback received - User returned from ZaloPay app")
                // Let ZaloPay SDK handle the deep link
                ZaloPaySDKHelper.handleDeepLink(data)
                // The wallet will automatically refresh when the user navigates back to it
                // ZaloPay will have sent the payment result to the backend via their callback
            }
        }
    }
}

