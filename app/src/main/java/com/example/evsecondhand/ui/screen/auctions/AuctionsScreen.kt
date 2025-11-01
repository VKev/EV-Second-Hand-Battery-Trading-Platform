package com.example.evsecondhand.ui.screen.auctions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.AuctionSummary
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dau gia dang dien ra",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
                Text(
                    text = "Kham pha cac phien dau gia pin va xe dien theo thoi gian thuc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        when {
            state.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }

            state.error != null &&
                state.presentAuctions.isEmpty() &&
                state.futureAuctions.isEmpty() &&
                state.pastAuctions.isEmpty() -> {
                item {
                    AuctionErrorCard(
                        message = state.error ?: "Unknown error",
                        onRetry = { viewModel.retry() }
                    )
                }
            }

            state.presentAuctions.isEmpty() &&
                state.futureAuctions.isEmpty() &&
                state.pastAuctions.isEmpty() -> {
                item {
                    Text(
                        text = "Chua co phien dau gia nao.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryGreen
        )

        val vehicleItems = auctions.filter { it.isVehicle() }
        val batteryItems = auctions.filter { it.isBattery() }

        if (vehicleItems.isEmpty() && batteryItems.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            return
        }

        if (vehicleItems.isNotEmpty()) {
            Text(
                text = "Xe dien",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(vehicleItems, key = { it.listingId }) { auction ->
                    AuctionSummaryCard(
                        auction = auction,
                        placeholderIcon = Icons.Default.DirectionsCar,
                        onClick = { onAuctionClick(auction) }
                    )
                }
            }
        }

        if (batteryItems.isNotEmpty()) {
            Text(
                text = "Pin EV",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(batteryItems, key = { it.listingId }) { auction ->
                    AuctionSummaryCard(
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
private fun AuctionSummaryCard(
    auction: AuctionSummary,
    placeholderIcon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuctionCoverImage(
                imageUrl = auction.imageUrl,
                placeholderIcon = placeholderIcon
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = auction.title.orEmpty().ifBlank { "San pham dau gia" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val priceText = auction.currentBid ?: auction.startingPrice
                priceText?.let {
                    Text(
                        text = formatCurrency(it),
                        style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen)
                    )
                }

                val startsAt = auction.auctionStartsAt?.let(::formatAuctionDate)
                val endsAt = auction.auctionEndsAt?.let(::formatAuctionDate)

                when {
                    endsAt != null -> Text(
                        text = "Ket thuc: $endsAt",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    startsAt != null -> Text(
                        text = "Bat dau: $startsAt",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
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


