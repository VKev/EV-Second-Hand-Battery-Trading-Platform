package com.example.evsecondhand.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.ui.screen.chatbot.ChatbotWidget
import com.example.evsecondhand.ui.screen.home.components.*
import com.example.evsecondhand.ui.viewmodel.ChatbotViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlin.math.min

/**
 * Home Screen - M\u00E0n h\u00ECnh ch\u00EDnh c\u1EE7a \u1EE9ng d\u1EE5ng
 * 
 * Components Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃƒÂ¡ch ra thÃƒÂ nh cÃƒÂ¡c file riÃƒÂªng:
 * - HeroSection.kt: Hero banner v\u1EDBi parallax effect
 * - WelcomeSection.kt: Welcome card v\u00E0 trust badges
 * - ProductCards.kt: Battery & Vehicle cards v\u1EDBi animations
 * - StateCards.kt: Loading, Error, Empty state cards
 * - FooterSection.kt: Footer v\u1EDBi contact info, services, social links
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onBatteryClick: (String) -> Unit,
    onVehicleClick: (String) -> Unit,
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val state by homeViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    
    // Parallax effect for hero section
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val parallaxOffset = min(scrollOffset.value.toFloat() * 0.5f, 200f)
    
    // Load more detection - auto load when scrolled near bottom
    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisibleItemIndex >= totalItemsNumber - 2
        }
    }
    
    LaunchedEffect(isScrolledToEnd) {
        if (isScrolledToEnd && !state.isLoadingBatteries && !state.isLoadingVehicles) {
            homeViewModel.loadMoreBatteries()
            homeViewModel.loadMoreVehicles()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(
                isRefreshing = state.isLoadingBatteries && state.currentBatteryPage == 1
            ),
            onRefresh = { homeViewModel.refresh() }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8F9FA),
                                Color(0xFFFFFFFF)
                            )
                        )
                    ),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 1. Hero Section with Parallax
                item {
                    HeroSection(parallaxOffset = parallaxOffset)
                }
                
           
                
                // 3. Section Divider
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 4. Batteries Section Header
                item {
                    SectionHeader(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Pin EV ch\u1EA5t l\u01B0\u1EE3ng",
                        subtitle = "\u0110\u01B0\u1EE3c ki\u1EC3m \u0111\u1ECBnh k\u1EF9 l\u01B0\u1EE1ng, b\u1EA3o h\u00E0nh r\u00F5 r\u00E0ng"
                    )
                }
                
                // 5. Batteries List
                item {
                    val batteryError = state.batteryError
                    when {
                        batteryError != null -> {
                            ErrorCard(
                                message = batteryError,
                                onRetry = { homeViewModel.loadBatteries(1) }
                            )
                        }
                        state.batteries.isEmpty() && !state.isLoadingBatteries -> {
                            EmptyStateCard(message = "Kh\u00F4ng c\u00F3 pin n\u00E0o kh\u1EA3 d\u1EE5ng")
                        }
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                items(state.batteries) { battery ->
                                    ModernBatteryCard(
                                        battery = battery,
                                        onClick = { onBatteryClick(battery.id) }
                                    )
                                }
                                
                                if (state.isLoadingBatteries) {
                                    item {
                                        LoadingCard()
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 6. Section Divider
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 1.dp,
                        color = Color.Black.copy(alpha = 0.06f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 7. Vehicles Section Header
                item {
                    SectionHeader(
                        icon = Icons.Default.DirectionsCar,
                        title = "Xe \u0111i\u1EC7n \u0111a d\u1EA1ng",
                        subtitle = "Nhi\u1EC1u th\u01B0\u01A1ng hi\u1EC7u, m\u1EABu m\u00E3 t\u1EEB ph\u1ED5 th\u00F4ng \u0111\u1EBFn cao c\u1EA5p"
                    )
                }
                
                // 8. Vehicles List
                item {
                    val vehicleError = state.vehicleError
                    when {
                        vehicleError != null -> {
                            ErrorCard(
                                message = vehicleError,
                                onRetry = { homeViewModel.loadVehicles(1) }
                            )
                        }
                        state.vehicles.isEmpty() && !state.isLoadingVehicles -> {
                            EmptyStateCard(message = "Kh\u00F4ng c\u00F3 xe n\u00E0o kh\u1EA3 d\u1EE5ng")
                        }
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                items(state.vehicles) { vehicle ->
                                    ModernVehicleCard(
                                        vehicle = vehicle,
                                        onClick = { onVehicleClick(vehicle.id) }
                                    )
                                }
                                
                                if (state.isLoadingVehicles) {
                                    item {
                                        LoadingCard()
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 9. Final Divider
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
            
            }
        }
        
        // Chatbot Widget - Floating action button \u1EDF g\u00F3c d\u01B0\u1EDBi ph\u1EA3i
        ChatbotWidget(
            viewModel = chatbotViewModel,
            modifier = Modifier.fillMaxSize(),
            onNavigateToVehicle = { vehicleId -> onVehicleClick(vehicleId) },
            onNavigateToBattery = { batteryId -> onBatteryClick(batteryId) }
        )
    }
}


