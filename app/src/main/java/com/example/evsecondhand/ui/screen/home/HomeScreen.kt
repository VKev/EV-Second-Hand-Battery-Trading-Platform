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
 * Home Screen - Màn hình chính của ứng dụng
 * 
 * Components được tách ra thành các file riêng:
 * - HeroSection.kt: Hero banner với parallax effect
 * - WelcomeSection.kt: Welcome card và trust badges
 * - ProductCards.kt: Battery & Vehicle cards với animations
 * - StateCards.kt: Loading, Error, Empty state cards
 * - FooterSection.kt: Footer với contact info, services, social links
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
                        title = "Pin EV Chất Lượng",
                        subtitle = "Được kiểm định kỹ lưỡng, bảo hành rõ ràng"
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
                            EmptyStateCard(message = "Không có pin nào khả dụng")
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
                        title = "Xe Điện Đa Dạng",
                        subtitle = "Nhiều thương hiệu, mẫu mã từ phổ thông đến cao cấp"
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
                            EmptyStateCard(message = "Không có xe nào khả dụng")
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
        
        // Chatbot Widget - Floating action button ở góc dưới phải
        ChatbotWidget(
            viewModel = chatbotViewModel,
            modifier = Modifier.fillMaxSize(),
            onNavigateToVehicle = { vehicleId -> onVehicleClick(vehicleId) },
            onNavigateToBattery = { batteryId -> onBatteryClick(batteryId) }
        )
    }
}
