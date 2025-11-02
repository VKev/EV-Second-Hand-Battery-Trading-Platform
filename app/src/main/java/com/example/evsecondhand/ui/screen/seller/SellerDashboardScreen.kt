package com.example.evsecondhand.ui.screen.seller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.seller.BatteryItem
import com.example.evsecondhand.data.model.seller.VehicleItem
import com.example.evsecondhand.ui.theme.*
import com.example.evsecondhand.ui.components.ResponsiveText
import com.example.evsecondhand.ui.components.ModernCard
import com.example.evsecondhand.ui.viewmodel.SellerDashboardUiState
import com.example.evsecondhand.ui.viewmodel.SellerDashboardViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.max

@Composable
fun SellerDashboardScreen(
    viewModel: SellerDashboardViewModel,
    onBatteryClick: (batteryId: String) -> Unit,
    onBackClick: () -> Unit,
    onAddListingClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SellerDashboardContent(
        state = state,
        onRefresh = viewModel::refresh,
        onBatteryClick = onBatteryClick,
        onBackClick = onBackClick,
        onAddListingClick = onAddListingClick
    )
}

@Composable
private fun SellerDashboardContent(
    state: SellerDashboardUiState,
    onRefresh: () -> Unit,
    onBatteryClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddListingClick: () -> Unit
) {
    val primaryTab = rememberSaveable { mutableStateOf(PrimarySectionTab.Overview) }
    val listingsTab = rememberSaveable { mutableStateOf(ListingCategory.Vehicles) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isLoading)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FBF8),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh,
            indicator = { refreshState, trigger ->
                SwipeRefreshIndicator(
                    state = refreshState,
                    refreshTriggerDistance = trigger,
                    backgroundColor = Color.White,
                    contentColor = PrimaryGreen,
                    scale = true
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryGreen.copy(alpha = 0.05f),
                                    AccentBlue.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    DashboardHeader(
                        onBackClick = onBackClick,
                        onAddListingClick = onAddListingClick
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.errorMessage?.let { error ->
                            ErrorBanner(message = error, onRetry = onRefresh)
                        }
                    }

                    DashboardTabRow(
                        selectedTab = primaryTab.value,
                        onTabSelected = { primaryTab.value = it }
                    )

                    Crossfade(
                        targetState = primaryTab.value,
                        animationSpec = tween(300)
                    ) { tab ->
                        when (tab) {
                            PrimarySectionTab.Overview -> OverviewSection(state)
                            PrimarySectionTab.Listings -> ListingsSection(
                                state = state,
                                listingsTab = listingsTab.value,
                                onListingTabChange = { listingsTab.value = it },
                                onBatteryClick = onBatteryClick
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    onBackClick: () -> Unit,
    onAddListingClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = TextPrimary
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            ResponsiveText(
                text = "Tin của tôi",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1
            )
            ResponsiveText(
                text = "Theo dõi và quản lý tin đăng của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2
            )
        }

        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryGreen,
            shadowElevation = 8.dp
        ) {
            IconButton(onClick = onAddListingClick, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo tin mới",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardTabRow(
    selectedTab: PrimarySectionTab,
    onTabSelected: (PrimarySectionTab) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = PrimaryGreen,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                        .height(4.dp)
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(PrimaryGreen)
                )
            }
        ) {
            PrimarySectionTab.values().forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(state: SellerDashboardUiState) {
    val totalListings = state.vehicles.size + state.batteries.size
    val activeListings = state.vehicles.count { it.status.equals("AVAILABLE", true) } +
            state.batteries.count { it.status.equals("AVAILABLE", true) }
    val totalViews = max(120, totalListings * 35)
    val totalMessages = max(5, totalListings * 3)
    val totalEarnings = state.vehicles.sumOf { it.price } + state.batteries.sumOf { it.price }

    val metrics = listOf(
        MetricCardData(
            title = "Hoạt động",
            value = activeListings.toString(),
            subtitle = "$totalListings tin đã đăng",
            icon = Icons.Default.CheckCircle,
            accent = PrimaryGreen,
            gradient = listOf(Color(0xFF4CAF50), Color(0xFF45A049))
        ),
        MetricCardData(
            title = "Lượt xem",
            value = totalViews.toString(),
            subtitle = "+${max(12, totalListings * 5)} lượt/tuần",
            icon = Icons.Default.RemoveRedEye,
            accent = AccentBlue,
            gradient = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
        ),
        MetricCardData(
            title = "Tin nhắn",
            value = totalMessages.toString(),
            subtitle = "+${max(2, totalListings)} hội thoại mới",
            icon = Icons.Default.Message,
            accent = Color(0xFF7C4DFF),
            gradient = listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2))
        ),
        MetricCardData(
            title = "Doanh thu",
            value = formatCurrency(totalEarnings),
            subtitle = "Tổng giá trị",
            icon = Icons.Default.AttachMoney,
            accent = Color(0xFF00BFA5),
            gradient = listOf(Color(0xFF00BFA5), Color(0xFF00897B))
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Enhanced Header with gradient and icon
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF0F9FF),
                                Color(0xFFE8F4FF),
                                Color(0xFFF8FCFF)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(8.dp, RoundedCornerShape(18.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGreen,
                                            AccentBlue
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📊",
                                fontSize = 32.sp
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tổng quan hoạt động",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Theo dõi hiệu suất kinh doanh của bạn",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Performance indicator
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PrimaryGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tốt",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Enhanced Metrics with staggered animation
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            items(metrics.size) { index ->
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(index * 100L)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ) + slideInHorizontally(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialOffsetX = { it / 2 }
                    ) + scaleIn(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialScale = 0.8f
                    )
                ) {
                    EnhancedMetricCard(
                        data = metrics[index],
                        modifier = Modifier.width(260.dp)
                    )
                }
            }
        }

        // Quick Stats Summary
        QuickStatsSummary(
            totalListings = totalListings,
            activeListings = activeListings,
            totalViews = totalViews
        )

        RecentActivityCard(state)
        TipsCard()
    }
}

@Composable
private fun QuickStatsSummary(
    totalListings: Int,
    activeListings: Int,
    totalViews: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickStatItem(
                icon = Icons.Default.Inventory,
                value = totalListings.toString(),
                label = "Tổng tin",
                color = Color(0xFF2196F3)
            )

            VerticalDivider(
                modifier = Modifier.height(60.dp),
                thickness = 1.dp,
                color = Color(0xFFE8E8E8)
            )

            QuickStatItem(
                icon = Icons.Default.Visibility,
                value = totalViews.toString(),
                label = "Lượt xem",
                color = Color(0xFF9C27B0)
            )

            VerticalDivider(
                modifier = Modifier.height(60.dp),
                thickness = 1.dp,
                color = Color(0xFFE8E8E8)
            )

            QuickStatItem(
                icon = Icons.Default.TrendingUp,
                value = "${(activeListings * 100 / max(1, totalListings))}%",
                label = "Tỷ lệ hoạt động",
                color = PrimaryGreen
            )
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color.copy(alpha = 0.12f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        ResponsiveText(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = TextPrimary,
            maxLines = 1
        )
        ResponsiveText(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondary,
            maxLines = 2
        )
    }
}

@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Color.Gray
) {
    Box(
        modifier = modifier
            .width(thickness)
            .background(color)
    )
}
@Composable
private fun EnhancedMetricCard(
    data: MetricCardData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = data.accent.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = data.gradient.map { it.copy(alpha = 0.05f) }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        ResponsiveText(
                            text = data.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ResponsiveText(
                            text = data.value,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = data.accent.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = data.accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = data.accent.copy(alpha = 0.15f),
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = data.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(state: SellerDashboardUiState) {
    val recentVehicle = state.vehicles.firstOrNull()
    val recentBattery = state.batteries.firstOrNull()
    val activities = listOfNotNull(
        ActivityItem(
            title = "Tin nhắn mới",
            description = "Người mua đang hỏi về ${recentVehicle?.title ?: "tin đăng của bạn"}.",
            timestamp = "2 giờ trước",
            accent = AccentBlue,
            icon = Icons.Default.Message
        ),
        recentBattery?.let {
            ActivityItem(
                title = "Lượt xem tăng",
                description = "${it.title} có thêm ${max(5, it.capacity / 2)} lượt xem hôm nay.",
                timestamp = "8 giờ trước",
                accent = Color(0xFF00C853),
                icon = Icons.Default.TrendingUp
            )
        },
        recentVehicle?.let {
            ActivityItem(
                title = "Xem hồ sơ",
                description = "Một người mua tiềm năng đã xem hồ sơ bán của bạn.",
                timestamp = "Hôm qua",
                accent = Color(0xFF7C4DFF),
                icon = Icons.Default.Person
            )
        },
        ActivityItem(
            title = "Đánh giá mới",
            description = "Bạn vừa nhận được đánh giá 5 sao cho giao dịch gần nhất.",
            timestamp = "2 ngày trước",
            accent = Color(0xFFFFC107),
            icon = Icons.Default.Star
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕐 Hoạt động gần đây",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            activities.forEach { item ->
                ActivityRow(item)
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = item.accent.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = item.timestamp,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TipsCard() {
    val tips = listOf(
        TipItem("💬", "Mô tả chi tiết", "Tin có mô tả rõ ràng tăng 35% lượt xem", Color(0xFF2196F3)),
        TipItem("📸", "Ảnh chất lượng cao", "Ảnh sắc nét giúp tăng 70% lượt quan tâm", Color(0xFF9C27B0)),
        TipItem("⚡", "Phản hồi nhanh", "Trả lời nhanh giúp tăng 50% cơ hội chốt đơn", Color(0xFFFF9800)),
        TipItem("🔋", "Báo cáo pin", "Báo cáo sức khỏe pin tạo niềm tin với người mua", Color(0xFF00BFA5))
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "💡 Mẹo dành cho người bán",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                tips.forEachIndexed { index, tip ->
                    EnhancedTipRow(tip = tip, number = index + 1)
                }

                Button(
                    onClick = { /* TODO: navigate to seller guide */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        "Xem hướng dẫn chi tiết",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedTipRow(tip: TipItem, number: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = tip.color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = tip.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ListingsSection(
    state: SellerDashboardUiState,
    listingsTab: ListingCategory,
    onListingTabChange: (ListingCategory) -> Unit,
    onBatteryClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📝 Tin đăng của tôi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${state.vehicles.size + state.batteries.size} tin hiện có",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ListingCategory.values().forEach { category ->
                        FilterChip(
                            selected = category == listingsTab,
                            onClick = { onListingTabChange(category) },
                            label = { Text(category.label) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        when (listingsTab) {
            ListingCategory.Vehicles -> VehiclesSection(state.vehicles)
            ListingCategory.Batteries -> BatteriesSection(
                items = state.batteries,
                onBatteryClick = onBatteryClick
            )
        }
    }
}

@Composable
private fun VehiclesSection(vehicles: List<VehicleItem>) {
    if (vehicles.isEmpty()) {
        EmptyState(message = "Chưa có phương tiện nào", icon = Icons.Default.DirectionsCar)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        vehicles.forEach { vehicle ->
            EnhancedVehicleCard(vehicle = vehicle)
        }
    }
}

@Composable
private fun EnhancedVehicleCard(vehicle: VehicleItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedListingImage(
                    imageUrl = vehicle.images.firstOrNull(),
                    contentDescription = vehicle.title,
                    placeholderIcon = Icons.Default.DirectionsCar
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ResponsiveText(
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            ResponsiveText(
                                text = vehicle.year.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                        ResponsiveText(
                            text = "${vehicle.brand} ${vehicle.model}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                EnhancedStatusBadge(vehicle.status)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    ResponsiveText(
                        text = "Giá bán",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    ResponsiveText(
                        text = formatCurrency(vehicle.price),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = PrimaryGreen,
                        maxLines = 1
                    )
                }

                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        ResponsiveText(
                            text = "${formatMileage(vehicle.mileage)} km",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteriesSection(
    items: List<BatteryItem>,
    onBatteryClick: (String) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState(message = "Chưa có pin nào", icon = Icons.Default.BatteryFull)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { battery ->
            EnhancedBatteryCard(
                battery = battery,
                onClick = { onBatteryClick(battery.id) }
            )
        }
    }
}

@Composable
private fun EnhancedBatteryCard(
    battery: BatteryItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedListingImage(
                    imageUrl = battery.images.firstOrNull(),
                    contentDescription = battery.title,
                    placeholderIcon = Icons.Default.BatteryFull
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ResponsiveText(
                        text = battery.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            ResponsiveText(
                                text = battery.brand,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(16.dp)
                            )
                            ResponsiveText(
                                text = "${battery.capacity} kWh",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                EnhancedStatusBadge(battery.status)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (battery.isAuction) Color(0xFFFF9800).copy(alpha = 0.15f)
                            else AccentBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            ResponsiveText(
                                text = if (battery.isAuction) "🔨 Đấu giá" else "💰 Giá cố định",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (battery.isAuction) Color(0xFFFF9800) else AccentBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                    ResponsiveText(
                        text = if (battery.isAuction) {
                            battery.startingPrice?.let { "Từ ${formatCurrency(it)}" } ?: "--"
                        } else {
                            formatCurrency(battery.price)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = PrimaryGreen,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Xem chi tiết",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            battery.auctionRejectionReason?.let { reason ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        ResponsiveText(
                            text = "Bị từ chối: $reason",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = ErrorRed,
                            maxLines = 3,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedListingImage(
    imageUrl: String?,
    contentDescription: String,
    placeholderIcon: ImageVector
) {
    Surface(
        modifier = Modifier.size(80.dp),
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (imageUrl.isNullOrBlank()) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = placeholderIcon,
                    contentDescription = contentDescription,
                    tint = TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EnhancedStatusBadge(status: String) {
    val normalized = status.uppercase(Locale.getDefault())
    val (tint, displayText, emoji) = when (normalized) {
        "AVAILABLE" -> Triple(PrimaryGreen, "Đang bán", "✓")
        "SOLD" -> Triple(AccentBlue, "Đã bán", "✓")
        "AUCTION_LIVE" -> Triple(Color(0xFFFF9800), "Đấu giá", "🔨")
        else -> Triple(TextSecondary, status, "•")
    }

    Surface(
        color = tint.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "$emoji ${displayText.uppercase(Locale.getDefault())}",
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyState(
    message: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Nhấn nút + để tạo tin đăng mới",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ErrorRed.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = ErrorRed.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Không thể tải dữ liệu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = PrimaryGreen
                )
            ) {
                Text("Thử lại", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class MetricCardData(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val gradient: List<Color>
)

private data class ActivityItem(
    val title: String,
    val description: String,
    val timestamp: String,
    val accent: Color,
    val icon: ImageVector
)

private data class TipItem(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color
)

private enum class PrimarySectionTab(val label: String) {
    Overview("Tổng quan"),
    Listings("Tin đăng của tôi")
}

private enum class ListingCategory(val label: String) {
    Vehicles("🚗 Xe/Phương tiện"),
    Batteries("🔋 Pin/Battery")
}

private fun formatMileage(value: Long): String {
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
    return formatter.format(value)
}

private val currencyFormatter by lazy {
    DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale("vi", "VN")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
    )
}

fun formatCurrency(vnd: Long): String = "${currencyFormatter.format(vnd)} ₫"