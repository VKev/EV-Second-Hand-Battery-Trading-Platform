package com.example.evsecondhand.ui.screen.auctions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.ui.components.EmptyStateView
import com.example.evsecondhand.ui.components.ErrorStateView
import com.example.evsecondhand.ui.components.LoadingStateView
import com.example.evsecondhand.ui.components.ModernSectionHeader
import com.example.evsecondhand.ui.components.ResponsiveText
import com.example.evsecondhand.ui.components.StatusBadge
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.AuctionListViewModel
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun AuctionsScreen(
    onAuctionClick: (AuctionSummary) -> Unit,
    viewModel: AuctionListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color.White
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Modern Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGreen.copy(alpha = 0.2f),
                                            PrimaryGreen.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            ResponsiveText(
                                text = "Đấu giá đang diễn ra",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFF1A1A1A),
                                maxLines = 1
                            )
                            ResponsiveText(
                                text = "Khám phá các phiên đấu giá pin và xe điện",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

        when {
            state.isLoading -> {
                item {
                    LoadingStateView(
                        message = "Đang tải phiên đấu giá...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp)
                    )
                }
            }

            state.error != null &&
                state.presentAuctions.isEmpty() &&
                state.futureAuctions.isEmpty() &&
                state.pastAuctions.isEmpty() -> {
                item {
                    ErrorStateView(
                        message = state.error ?: "Đã xảy ra lỗi khi tải dữ liệu",
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            state.presentAuctions.isEmpty() &&
                state.futureAuctions.isEmpty() &&
                state.pastAuctions.isEmpty() -> {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Gavel,
                        title = "Chưa có phiên đấu giá",
                        subtitle = "Hiện tại chưa có phiên đấu giá nào. Vui lòng quay lại sau!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    )
                }
            }

            else -> {
                val sections = listOf(
                    AuctionSectionConfig(
                        title = "Dang dau gia",
                        emptyMessage = "Khong co phien dau gia dang dien ra.",
                        items = state.presentAuctions
                    ),
                    AuctionSectionConfig(
                        title = "Da ket thuc",
                        emptyMessage = "Chua co phien dau gia nao ket thuc.",
                        items = state.pastAuctions
                    ),
                    AuctionSectionConfig(
                        title = "Sap dien ra",
                        emptyMessage = "Khong co phien dau gia sap dien ra.",
                        items = state.futureAuctions
                    )
                )

                sections.forEach { section ->
                    item {
                        AuctionCategorySection(
                            title = section.title,
                            auctions = section.items,
                            emptyMessage = section.emptyMessage,
                            onAuctionClick = onAuctionClick
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
    }
}

private data class AuctionSectionConfig(
    val title: String,
    val emptyMessage: String,
    val items: List<AuctionSummary>
)

@Composable
private fun AuctionCategorySection(
    title: String,
    auctions: List<AuctionSummary>,
    emptyMessage: String,
    onAuctionClick: (AuctionSummary) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modern Section Title with Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PrimaryGreen.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = when {
                            title.contains("Đang") -> Icons.Default.TrendingUp
                            title.contains("Sắp") -> Icons.Default.Schedule
                            else -> Icons.Default.Gavel
                        },
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            ResponsiveText(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A),
                maxLines = 1
            )
        }

        val vehicleItems = auctions.filter { it.isVehicle() }
        val batteryItems = auctions.filter { it.isBattery() }

        if (vehicleItems.isEmpty() && batteryItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ResponsiveText(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }
            return
        }

        if (vehicleItems.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(20.dp)
                )
                ResponsiveText(
                    text = "Xe điện",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1A1A1A),
                    maxLines = 1
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(vehicleItems, key = { it.listingId }) { auction ->
                    ModernAuctionCard(
                        auction = auction,
                        placeholderIcon = Icons.Default.DirectionsCar,
                        onClick = { onAuctionClick(auction) }
                    )
                }
            }
        }

        if (batteryItems.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                ResponsiveText(
                    text = "Pin EV",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1A1A1A),
                    maxLines = 1
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(batteryItems, key = { it.listingId }) { auction ->
                    ModernAuctionCard(
                        auction = auction,
                        placeholderIcon = Icons.Default.Bolt,
                        onClick = { onAuctionClick(auction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernAuctionCard(
    auction: AuctionSummary,
    placeholderIcon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Modern Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (auction.imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryGreen.copy(alpha = 0.1f),
                                        PrimaryGreen.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = placeholderIcon,
                            contentDescription = null,
                            tint = PrimaryGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = auction.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    ),
                                    startY = 80f
                                )
                            )
                    )
                }
                
                // Status Badge
                StatusBadge(
                    status = "ĐANG ĐẤU GIÁ",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResponsiveText(
                    text = auction.title.orEmpty().ifBlank { "Sản phẩm đấu giá" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF1A1A1A),
                    maxLines = 2
                )

                // Price Section
                val priceText = auction.currentBid ?: auction.startingPrice
                priceText?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ResponsiveText(
                            text = "Giá hiện tại:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        ResponsiveText(
                            text = formatCurrency(it),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = PrimaryGreen,
                            maxLines = 1
                        )
                    }
                }

                // Time Info
                val startsAt = auction.auctionStartsAt?.let(::formatAuctionDate)
                val endsAt = auction.auctionEndsAt?.let(::formatAuctionDate)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        when {
                            endsAt != null -> ResponsiveText(
                                text = "Kết thúc: $endsAt",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextSecondary,
                                maxLines = 1
                            )
                            startsAt != null -> ResponsiveText(
                                text = "Bắt đầu: $startsAt",
                                style = MaterialTheme.typography.bodySmall.copy(
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
}

@Composable
private fun AuctionCoverImage(
    imageUrl: String?,
    placeholderIcon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFFEAEAEA)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            PlaceholderIllustration(placeholderIcon)
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PlaceholderIllustration(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(PrimaryGreen.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = PrimaryGreen
        )
    }
}

@Composable
private fun AuctionErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Khong the tai du lieu",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(onClick = onRetry) {
                Text("Thu lai")
            }
        }
    }
}

private fun AuctionSummary.isVehicle(): Boolean =
    listingType.equals("vehicle", ignoreCase = true) ||
        listingType.equals("vehicles", ignoreCase = true)

private fun AuctionSummary.isBattery(): Boolean =
    listingType.equals("battery", ignoreCase = true) ||
        listingType.equals("batteries", ignoreCase = true)

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}

private fun formatAuctionDate(value: String): String? = try {
    val dateTime = OffsetDateTime.parse(value)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    dateTime.format(formatter)
} catch (exception: DateTimeParseException) {
    null
}


