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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    // Sử dụng SharedFlow để đảm bảo deep link được xử lý ngay cả khi app khởi động
    private val deepLinkEvents = MutableSharedFlow<Intent>(replay = 1, extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Gửi intent khởi động vào flow để xử lý
        dispatchDeepLinkIntent(intent)
        
        ZaloPaySDKHelper.init()

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
        // Gửi intent mới vào flow khi app đang chạy
        dispatchDeepLinkIntent(intent)
    }

    private fun dispatchDeepLinkIntent(intent: Intent?) {
        intent?.let {
            Log.d("MainActivity", "Dispatching deep link intent: ${it.data}")
            deepLinkEvents.tryEmit(it)
        }
    }
}
