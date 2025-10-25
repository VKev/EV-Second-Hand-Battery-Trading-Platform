package com.example.evsecondhand.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.ui.screen.chatbot.ChatbotWidget
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.ChatbotViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.NumberFormat
import java.util.Locale

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
    
    // Load more detection
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
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryGreen.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "EV Market",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EV Market",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                    Text(
                        text = "Khám phá pin & xe điện cũ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Batteries Section
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = "Batteries",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pin EV",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    val batteryError = state.batteryError
                    if (batteryError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "❌ Lỗi tải pin",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = batteryError,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { homeViewModel.loadBatteries(1) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    } else if (state.batteries.isEmpty() && !state.isLoadingBatteries) {
                        Text(
                            text = "Không có pin nào khả dụng",
                            modifier = Modifier.padding(16.dp),
                            color = TextSecondary
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.batteries) { battery ->
                                BatteryCard(
                                    battery = battery,
                                    onClick = { onBatteryClick(battery.id) }
                                )
                            }
                            
                            if (state.isLoadingBatteries) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Vehicles Section
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = "Vehicles",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Xe điện",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    val vehicleError = state.vehicleError
                    if (vehicleError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "❌ Lỗi tải xe",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = vehicleError,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { homeViewModel.loadVehicles(1) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    } else if (state.vehicles.isEmpty() && !state.isLoadingVehicles) {
                        Text(
                            text = "Không có xe nào khả dụng",
                            modifier = Modifier.padding(16.dp),
                            color = TextSecondary
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.vehicles) { vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    onClick = { onVehicleClick(vehicle.id) }
                                )
                            }
                            
                            if (state.isLoadingVehicles) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        
        // Chatbot Widget - FAB ở góc dưới phải
        ChatbotWidget(
            viewModel = chatbotViewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BatteryCard(
    battery: Battery,
    onClick: () -> Unit = {}
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Image
            Box(modifier = Modifier.height(140.dp)) {
                AsyncImage(
                    model = battery.images.firstOrNull(),
                    contentDescription = battery.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                if (battery.isVerified) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreen
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Verified",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = battery.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${battery.capacity} kWh",
                            fontSize = 14.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        battery.health?.let {
                            Text(
                                text = "Health: $it%",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Text(
                        text = formatter.format(battery.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${battery.brand} • ${battery.year}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit = {}
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Image
            Box(modifier = Modifier.height(140.dp)) {
                AsyncImage(
                    model = vehicle.images.firstOrNull(),
                    contentDescription = vehicle.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                if (vehicle.isVerified) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreen
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Verified",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = vehicle.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${vehicle.brand} ${vehicle.model}",
                            fontSize = 14.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${NumberFormat.getInstance(Locale.US).format(vehicle.mileage)} km",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    
                    Text(
                        text = formatter.format(vehicle.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Năm ${vehicle.year}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
