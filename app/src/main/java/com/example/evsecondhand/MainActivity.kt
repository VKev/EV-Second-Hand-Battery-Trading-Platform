package com.example.evsecondhand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.ui.navigation.AppNavigation
import com.example.evsecondhand.ui.theme.EVSecondHandTheme
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EVSecondHandTheme {
                val authViewModel: AuthViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()
                
                AppNavigation(
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}