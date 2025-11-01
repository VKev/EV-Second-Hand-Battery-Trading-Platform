
package com.example.evsecondhand.ui.screen.auction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.BatteryAndCharging
import com.example.evsecondhand.data.model.Dimensions
import com.example.evsecondhand.data.model.Performance
import com.example.evsecondhand.data.model.Seller
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.data.model.Warranty
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.data.repository.ProductRepository
import com.example.evsecondhand.ui.theme.EVSecondHandTheme
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.PrimaryGreenDark
import com.example.evsecondhand.ui.theme.TextSecondary
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionScreen(
    vehicleId: String,
    onBackClick: () -> Unit,
    viewModel: AuctionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.error
    val vehicle = uiState.vehicle

    LaunchedEffect(vehicleId) {
        viewModel.loadAuction(vehicleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auction") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }

                errorMessage != null -> {
                    AuctionErrorState(
                        message = errorMessage,
                        onRetry = { viewModel.reload() }
                    )
                }

                vehicle != null -> {
                    AuctionContent(
                        vehicle = vehicle,
                        currentBid = uiState.currentBid,
                        bidIncrement = uiState.bidIncrement,
                        buyNowPrice = uiState.buyNowPrice,
                        bids = uiState.bidHistory,
                        onPlaceBid = viewModel::placeBid
                    )
                }
            }
        }
    }
}

data class AuctionUiState(
    val vehicle: Vehicle? = null,
    val currentBid: Int? = null,
    val bidIncrement: Int = 0,
    val buyNowPrice: Int? = null,
    val bidHistory: List<AuctionBid> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuctionViewModel : ViewModel() {

    private val repository = ProductRepository(RetrofitClient.productApi)

    private val _uiState = MutableStateFlow(AuctionUiState(isLoading = true))
    val uiState: StateFlow<AuctionUiState> = _uiState.asStateFlow()

    private var currentVehicleId: String? = null

    fun loadAuction(vehicleId: String, force: Boolean = false) {
        if (!force && currentVehicleId == vehicleId && _uiState.value.vehicle != null) {
            return
        }
        currentVehicleId = vehicleId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = repository.getVehicleDetail(vehicleId)

            result.onSuccess { vehicle ->
                val bids = buildSampleBids(vehicle)
                val highestBid = bids.maxOfOrNull { it.amount } ?: vehicle.startingPrice ?: vehicle.price
                _uiState.value = AuctionUiState(
                    vehicle = vehicle,
                    currentBid = highestBid,
                    bidIncrement = vehicle.bidIncrement ?: 0,
                    buyNowPrice = vehicle.price,
                    bidHistory = bids,
                    isLoading = false,
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = AuctionUiState(
                    isLoading = false,
                    error = parseErrorMessage(throwable)
                )
            }
        }
    }

    fun reload() {
        currentVehicleId?.let { loadAuction(it, force = true) }
    }

    fun placeBid(amount: Int) {
        val state = _uiState.value
        val vehicle = state.vehicle ?: return
        val updatedHistory = listOf(
            AuctionBid(
                bidder = "You",
                amount = amount,
                timeAgo = "Just now",
                isLeading = true
            )
        ) + state.bidHistory.map { it.copy(isLeading = false) }

        _uiState.value = state.copy(
            currentBid = amount,
            bidHistory = updatedHistory,
            bidIncrement = vehicle.bidIncrement ?: state.bidIncrement
        )
    }
}

data class AuctionBid(
    val bidder: String,
    val amount: Int,
    val timeAgo: String,
    val isLeading: Boolean
)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuctionContent(
    vehicle: Vehicle,
    currentBid: Int?,
    bidIncrement: Int,
    buyNowPrice: Int?,
    bids: List<AuctionBid>,
    onPlaceBid: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            VehicleHeaderCard(vehicle = vehicle)
        }

        item {
            CurrentBidCard(currentBid = currentBid)
        }

        item {
            AuctionCountdownCard(
                auctionEndsAt = vehicle.auctionEndsAt,
                depositAmount = vehicle.depositAmount
            )
        }

        item {
            BidInputCard(
                currentBid = currentBid,
                bidIncrement = bidIncrement,
                onPlaceBid = onPlaceBid
            )
        }

        item {
            BuyNowCard(price = buyNowPrice)
        }

        if (bids.isNotEmpty()) {
            item {
                BidHistorySection(bids = bids)
            }
        }

        vehicle.specifications?.let { specs ->
            item {
                SpecificationSection(specs = specs)
            }
        }
    }
}

@Composable
private fun VehicleHeaderCard(vehicle: Vehicle) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val primaryImage = vehicle.images.firstOrNull()

                if (primaryImage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = primaryImage,
                        contentDescription = vehicle.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (vehicle.isVerified) {
                    VerifiedBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = vehicle.brand.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formatCurrency(vehicle.price),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(label = "Year", value = vehicle.year.toString())
                    StatChip(label = "Mileage", value = "${formatNumber(vehicle.mileage)} km")
                    StatChip(label = "Model", value = vehicle.model)
                    StatChip(
                        label = "Status",
                        value = vehicle.status.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifiedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PrimaryGreen.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = PrimaryGreen
            )
            Text(
                text = "Verified seller",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PrimaryGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
@Composable
private fun CurrentBidCard(currentBid: Int?) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current bid",
                style = MaterialTheme.typography.titleSmall.copy(color = TextSecondary)
            )
            Text(
                text = currentBid?.let { formatCurrency(it) } ?: "No bids yet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            )
        }
    }
}

@Composable
private fun AuctionCountdownCard(
    auctionEndsAt: String?,
    depositAmount: Int?
) {
    var countdown by rememberSaveable { mutableStateOf(formatCountdown(auctionEndsAt)) }

    LaunchedEffect(auctionEndsAt) {
        countdown = formatCountdown(auctionEndsAt)
        if (!auctionEndsAt.isNullOrBlank()) {
            while (true) {
                delay(1_000)
                val next = formatCountdown(auctionEndsAt)
                countdown = next
                if (next == null) break
            }
        }
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color(0xFFFFF3C2),
                border = BorderStroke(1.dp, Color(0xFFFFDD85)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auction ends in",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9A6B08))
                    )
                    Text(
                        text = countdown ?: "Closed",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A6B08)
                        )
                    )
                }
            }

            depositAmount?.takeIf { it > 0 }?.let {
                Text(
                    text = "Deposit required: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun BidInputCard(
    currentBid: Int?,
    bidIncrement: Int,
    onPlaceBid: (Int) -> Unit
) {
    val suggestion = nextSuggestedBid(currentBid, bidIncrement)
    var bidInput by rememberSaveable(currentBid, bidIncrement) {
        mutableStateOf(suggestion?.toString() ?: "")
    }

    val numericBid = bidInput.filter { it.isDigit() }.toIntOrNull()
    val isValidBid = numericBid != null &&
        (currentBid == null || numericBid > currentBid) &&
        (bidIncrement <= 0 || currentBid == null || numericBid >= currentBid + bidIncrement)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Your bid",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = if (bidIncrement > 0) {
                        "Min step: ${formatCurrency(bidIncrement)}"
                    } else {
                        "Enter an amount higher than the current bid."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            OutlinedTextField(
                value = bidInput,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    bidInput = filtered
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bid amount") },
                leadingIcon = {
                    Text(
                        text = "VND",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    numericBid?.let { onPlaceBid(it) }
                },
                enabled = isValidBid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = "Place bid",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
@Composable
private fun BuyNowCard(price: Int?) {
    if (price == null || price <= 0) return

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Buy now",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = formatCurrency(price),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            )
            OutlinedButton(
                onClick = { /* TODO: implement buy now */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                border = BorderStroke(1.dp, PrimaryGreen)
            ) {
                Text(
                    text = "Buy instantly",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreenDark
                    )
                )
            }
        }
    }
}

@Composable
private fun BidHistorySection(bids: List<AuctionBid>) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recent bids",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            bids.forEachIndexed { index, bid ->
                BidHistoryRow(bid = bid)
                if (index != bids.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun BidHistoryRow(bid: AuctionBid) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = bid.bidder,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (bid.isLeading) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (bid.isLeading) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = bid.timeAgo,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
        Text(
            text = formatCurrency(bid.amount),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (bid.isLeading) PrimaryGreen else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun SpecificationSection(specs: VehicleSpecifications) {
    val items = buildSpecificationItems(specs)
    if (items.isEmpty()) return

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            items.forEachIndexed { index, item ->
                SpecificationItem(
                    label = item.first,
                    value = item.second
                )
                if (index != items.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SpecificationItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AuctionErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 24.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Try again")
        }
    }
}
private fun buildSpecificationItems(specs: VehicleSpecifications): List<Pair<String, String>> {
    val items = mutableListOf<Pair<String, String>>()

    specs.warranty?.let { warranty ->
        warranty.basic?.let { items.add("Warranty (basic)" to it) }
        warranty.battery?.let { items.add("Warranty (battery)" to it) }
        warranty.drivetrain?.let { items.add("Warranty (drivetrain)" to it) }
    }

    specs.performance?.let { performance ->
        performance.motorType?.let { items.add("Motor type" to it) }
        performance.horsepower?.let { items.add("Horsepower" to it) }
        performance.topSpeed?.let { items.add("Top speed" to it) }
        performance.acceleration?.let { items.add("0-100 km/h" to it) }
    }

    specs.dimensions?.let { dimensions ->
        dimensions.length?.let { items.add("Length" to it) }
        dimensions.width?.let { items.add("Width" to it) }
        dimensions.height?.let { items.add("Height" to it) }
        dimensions.curbWeight?.let { items.add("Curb weight" to it) }
    }

    specs.batteryAndCharging?.let { battery ->
        battery.range?.let { items.add("Range" to it) }
        battery.chargeTime?.let { items.add("Charge time" to it) }
        battery.chargingSpeed?.let { items.add("Charging speed" to it) }
        battery.batteryCapacity?.let { items.add("Battery capacity" to it) }
    }

    return items
}

private fun buildSampleBids(vehicle: Vehicle): List<AuctionBid> {
    if (vehicle.isAuction != true) return emptyList()
    val increment = vehicle.bidIncrement ?: return emptyList()
    if (increment <= 0) return emptyList()
    val baseBid = vehicle.startingPrice ?: vehicle.price

    val amounts = listOf(baseBid + increment * 3, baseBid + increment * 2, baseBid + increment)
    val names = listOf("Alex Nguyen", "Bao Tran", "Minh Pham")
    val times = listOf("3 minutes ago", "15 minutes ago", "40 minutes ago")

    return names.indices.map { index ->
        AuctionBid(
            bidder = names[index],
            amount = amounts[index],
            timeAgo = times[index],
            isLeading = index == 0
        )
    }
}

private fun nextSuggestedBid(currentBid: Int?, increment: Int): Int? {
    if (currentBid == null) return null
    if (increment <= 0) return currentBid + 1
    return currentBid + increment
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}

private fun formatNumber(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(value)
}

private fun formatCountdown(auctionEndsAt: String?): String? {
    if (auctionEndsAt.isNullOrBlank()) return null
    return try {
        val end = OffsetDateTime.parse(auctionEndsAt)
        val now = OffsetDateTime.now(end.offset)
        val duration = Duration.between(now, end)
        if (duration.isNegative || duration.isZero) {
            null
        } else {
            val hours = duration.toHours()
            val minutes = duration.minusHours(hours).toMinutes()
            val seconds = duration.minusHours(hours).minusMinutes(minutes).seconds
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    } catch (exception: DateTimeParseException) {
        null
    }
}

private fun parseErrorMessage(exception: Throwable): String {
    val message = exception.message.orEmpty()
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) ->
            "Cannot reach server. Please check your connection."
        message.contains("timeout", ignoreCase = true) ->
            "Request timed out. Please try again."
        message.contains("JSON", ignoreCase = true) || message.contains("Serialization", ignoreCase = true) ->
            "Data parsing error: $message"
        else -> "Error: ${message.ifBlank { "Unknown error" }}"
    }
}

@Preview(showBackground = true)
@Composable
private fun AuctionScreenPreview() {
    val specs = VehicleSpecifications(
        warranty = Warranty(
            basic = "4 years / 80,000 km",
            battery = "8 years / 160,000 km"
        ),
        performance = Performance(
            topSpeed = "225 km/h",
            motorType = "Dual motor AWD",
            horsepower = "450 hp",
            acceleration = "3.2s"
        ),
        dimensions = Dimensions(
            length = "4,694 mm",
            width = "1,849 mm",
            height = "1,443 mm"
        ),
        batteryAndCharging = BatteryAndCharging(
            range = "507 km",
            chargeTime = "30 min (supercharger)",
            chargingSpeed = "250 kW",
            batteryCapacity = "82 kWh"
        )
    )

    val vehicle = Vehicle(
        id = "vehicle-1",
        title = "Tesla Model 3 2023",
        description = "Well maintained Tesla Model 3 with premium interior package.",
        price = 890000000,
        images = listOf(),
        status = "AVAILABLE",
        brand = "Tesla",
        model = "Model 3",
        year = 2023,
        mileage = 7500,
        specifications = specs,
        isVerified = true,
        isAuction = true,
        auctionStartsAt = OffsetDateTime.now().minusDays(1).toString(),
        auctionEndsAt = OffsetDateTime.now().plusHours(12).toString(),
        startingPrice = 820000000,
        bidIncrement = 5000000,
        depositAmount = 100000000,
        auctionRejectionReason = null,
        createdAt = OffsetDateTime.now().minusMonths(1).toString(),
        updatedAt = OffsetDateTime.now().toString(),
        sellerId = "seller-1",
        seller = Seller(
            id = "seller-1",
            name = "Sentry EV",
            avatar = null
        )
    )

    val bids = buildSampleBids(vehicle)

    EVSecondHandTheme {
        AuctionContent(
            vehicle = vehicle,
            currentBid = bids.maxOfOrNull { it.amount } ?: vehicle.startingPrice,
            bidIncrement = vehicle.bidIncrement ?: 0,
            buyNowPrice = vehicle.price,
            bids = bids,
            onPlaceBid = {}
        )
    }
}
