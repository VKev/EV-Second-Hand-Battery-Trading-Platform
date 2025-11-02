package com.example.evsecondhand

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.ui.navigation.AppNavigation
import com.example.evsecondhand.ui.theme.EVSecondHandTheme
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val deepLinkEvents = MutableSharedFlow<Intent>(replay = 1, extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dispatchDeepLinkIntent(intent)
        
        setContent {
            EVSecondHandTheme {
                val homeViewModel: HomeViewModel = viewModel()

                AppNavigation(
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel,
                    deepLinkFlow = deepLinkEvents.asSharedFlow()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchDeepLinkIntent(intent)
    }

    private fun dispatchDeepLinkIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            android.util.Log.d(
                "MainActivity",
                "Received deep link: ${intent.dataString}"
            )
            deepLinkEvents.tryEmit(intent)
        }
    }
}
