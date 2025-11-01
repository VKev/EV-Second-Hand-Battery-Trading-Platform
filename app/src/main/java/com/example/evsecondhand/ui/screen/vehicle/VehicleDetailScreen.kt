package com.example.evsecondhand.ui.screen.vehicle

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.Seller
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.VehicleDetailState
import com.example.evsecondhand.ui.viewmodel.VehicleDetailViewModel
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    onBackClick: () -> Unit,
    onBidClick: (String) -> Unit,
    viewModel: VehicleDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentVehicle = state.vehicle
    val context = LocalContext.current


    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId)
    }

    LaunchedEffect(state.depositMessage) {
        val message = state.depositMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearDepositMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết xe") },
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
                state.isLoading && currentVehicle == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }

                currentVehicle != null -> {
                    VehicleDetailContent(
                        vehicle = currentVehicle,
                        state = state,
                        modifier = Modifier.navigationBarsPadding(),
                        onDepositClick = { viewModel.placeDeposit() },
                        onBidClick = { onBidClick(currentVehicle.id) }
                    )
                }

                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Unknown error",
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailContent(
    vehicle: Vehicle,
    state: VehicleDetailState,
    modifier: Modifier = Modifier,
    onDepositClick: () -> Unit,
    onBidClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F9)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            VehicleGalleryCard(
                images = vehicle.images,
                isVerified = vehicle.isVerified
            )
        }

        item {
            VehicleOverviewCard(vehicle = vehicle)
        }

        item {
            QuickHighlightsSection(vehicle = vehicle)
        }

        if (vehicle.isAuction == true) {
            item {
                AuctionSummaryCard(vehicle = vehicle)
            }

            item {
                DepositActionSection(
                    vehicle = vehicle,
                    canBid = state.canBid,
                    hasDeposit = state.hasDeposit,
                    isProcessing = state.isProcessingDeposit,
                    depositError = state.depositError,
                    onDepositClick = onDepositClick,
                    onBidClick = onBidClick
                )
            }
        } else {
            item {
                PurchaseShortcutCard(price = vehicle.price)
            }
        }

        vehicle.specifications?.let { specs ->
            item {
                SpecificationSection(specs = specs)
            }
        }

        vehicle.seller?.let { seller ->
            item {
                SellerContactSection(seller = seller)
            }
        }

        if (vehicle.description.isNotBlank()) {
            item {
                DescriptionSection(description = vehicle.description)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VehicleGalleryCard(
    images: List<String>,
    isVerified: Boolean
) {
    val safeImages = images.ifEmpty { emptyList() }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(safeImages) {
        selectedIndex = 0
    }

    val heroImage = safeImages.getOrNull(selectedIndex)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!heroImage.isNullOrBlank()) {
                    AsyncImage(
                        model = heroImage,
                        contentDescription = "Vehicle image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryGreen.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                if (isVerified) {
                    VerifiedBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }
            }
        }

        if (safeImages.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(safeImages) { index, imageUrl ->
                    GalleryThumbnail(
                        imageUrl = imageUrl,
                        isSelected = index == selectedIndex,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryThumbnail(
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 72.dp, height = 60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryGreen) else null,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Preview image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrimaryGreen.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            }
        }
    }
}

@Composable
private fun VerifiedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = PrimaryGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(40),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Đã kiểm duyệt",
                style = MaterialTheme.typography.labelMedium.copy(color = PrimaryGreen)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun VehicleOverviewCard(vehicle: Vehicle) {
    val mileageFormatter = NumberFormat.getInstance(Locale.US)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = vehicle.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatCurrency(vehicle.price),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                color = PrimaryGreen
            )
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoChip(label = "Hãng", value = vehicle.brand)
            InfoChip(label = "Loại", value = vehicle.model)
            InfoChip(label = "Năm", value = vehicle.year.toString())
            InfoChip(
                label = "Số km",
                value = "${mileageFormatter.format(vehicle.mileage)} km"
            )
        }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Surface(
        shape = RoundedCornerShape(40),
        color = PrimaryGreen.copy(alpha = 0.08f)
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelLarge.copy(color = PrimaryGreen),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun QuickHighlightsSection(vehicle: Vehicle) {
    val range = vehicle.specifications?.batteryAndCharging?.range ?: "--"
    val topSpeed = vehicle.specifications?.performance?.topSpeed ?: "--"
    val acceleration = vehicle.specifications?.performance?.acceleration ?: "--"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HighlightStatCard(
                label = "Quãng đường",
                value = range,
                modifier = Modifier.weight(1f)
            )
            HighlightStatCard(
                label = "Tốc độ tối đa",
                value = topSpeed,
                modifier = Modifier.weight(1f)
            )
            HighlightStatCard(
                label = "0-100 km/h",
                value = acceleration,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HighlightStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun AuctionSummaryCard(vehicle: Vehicle) {
    val currentBid = vehicle.startingPrice ?: vehicle.price
    var countdown by rememberSaveable { mutableStateOf(formatAuctionCountdown(vehicle.auctionEndsAt)) }

    LaunchedEffect(vehicle.auctionEndsAt) {
        while (true) {
            countdown = formatAuctionCountdown(vehicle.auctionEndsAt)
            if (countdown == null) break
            delay(1_000)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Mức giá cao nhất",
                    style = MaterialTheme.typography.titleSmall.copy(color = TextSecondary)
                )
                Text(
                    text = formatCurrency(currentBid),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                )
            }

            countdown?.let {
                AuctionCountdownBadge(remaining = it)
            } ?: Text(
                text = "Phiên đấu giá đã kết thúc",
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.error)
            )

            Divider()

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuctionInfoRow(
                    label = "Bắt đầu",
                    value = formatAuctionDate(vehicle.auctionStartsAt)
                )
                AuctionInfoRow(
                    label = "Kết thúc",
                    value = formatAuctionDate(vehicle.auctionEndsAt)
                )
                AuctionInfoRow(
                    label = "Bước giá",
                    value = vehicle.bidIncrement?.takeIf { it > 0 }?.let { formatCurrency(it) }
                )
                AuctionInfoRow(
                    label = "Tiền cọc",
                    value = vehicle.depositAmount?.takeIf { it > 0 }?.let { formatCurrency(it) }
                )
            }
        }
    }
}

@Composable
private fun AuctionCountdownBadge(remaining: String) {
    Surface(
        color = Color(0xFFFFF3C2),
        border = BorderStroke(1.dp, Color(0xFFFFDD85)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Còn lại",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9A6B08))
            )
            Text(
                text = remaining,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9A6B08)
                )
            )
        }
    }
}

@Composable
private fun AuctionInfoRow(
    label: String,
    value: String?
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun DepositActionSection(
    vehicle: Vehicle,
    canBid: Boolean,
    hasDeposit: Boolean,
    isProcessing: Boolean,
    depositError: String?,
    onDepositClick: () -> Unit,
    onBidClick: () -> Unit
) {
    val depositAmount = vehicle.depositAmount?.takeIf { it > 0 }
    val depositAmountText = depositAmount?.let { formatCurrency(it) }

    AuctionDepositCard(
        depositAmountText = depositAmountText,
        canBid = canBid,
        hasDeposit = hasDeposit,
        isProcessing = isProcessing,
        depositError = depositError,
        onDepositClick = onDepositClick,
        onBidClick = onBidClick
    )
}
@Composable
private fun AuctionDepositCard(
    depositAmountText: String?,
    canBid: Boolean,
    hasDeposit: Boolean,
    isProcessing: Boolean,
    depositError: String?,
    onDepositClick: () -> Unit,
    onBidClick: () -> Unit
) {
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
                text = "Đặt cọc để tham gia đấu giá",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (depositAmountText != null) {
                Text(
                    text = "Số tiền đặt cọc: $depositAmountText",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
            } else {
                Text(
                    text = "Liên hệ với người bán để biết số tiền đặt cọc.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            val infoText = if (canBid) {
                "Bạn đã sẵn sàng đấu giá phiên này."
            } else {
                "Sau khi thanh toán đặt cọc thành công, bạn sẽ được kích hoạt quyền đặt giá."
            }

            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            if (hasDeposit && !canBid) {
                Text(
                    text = "Hệ thống đang cập nhật trạng thái đấu giá...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            if (!depositError.isNullOrBlank()) {
                Text(
                    text = depositError,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                )
            }

            val buttonLabel = when {
                canBid -> "Đấu giá ngay"
                depositAmountText != null -> "Đặt cọc ngay"
                else -> "Liên hệ đặt cọc"
            }

            Button(
                onClick = if (canBid) onBidClick else onDepositClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = buttonLabel,
                        color = Color.White
                    )
                }
            }
        }
    }
}
@Composable
private fun PurchaseShortcutCard(price: Int) {
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
                text = "Giá niêm yết",
                style = MaterialTheme.typography.titleSmall.copy(color = TextSecondary)
            )
            Text(
                text = formatCurrency(price),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            )
            Button(
                onClick = { /* TODO: contact seller */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = "Mua ngay",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SpecificationSection(specs: VehicleSpecifications) {
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
                text = "Thông số kĩ thuật",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            val groups = listOf(
                "Hiệu năng" to listOf(
                    "Tốc độ tối đa" to specs.performance?.topSpeed,
                    "0-100 km/h" to specs.performance?.acceleration,
                    "Động cơ" to specs.performance?.motorType,
                    "Công suất" to specs.performance?.horsepower
                ),
                "Pin & sạc" to listOf(
                    "Dung lượng pin" to specs.batteryAndCharging?.batteryCapacity,
                    "Quãng đường" to specs.batteryAndCharging?.range,
                    "Thời gian sạc" to specs.batteryAndCharging?.chargeTime,
                    "Tốc độ sạc" to specs.batteryAndCharging?.chargingSpeed
                ),
                "Kích thuớc" to listOf(
                    "Chiều dài" to specs.dimensions?.length,
                    "Chiều rộng" to specs.dimensions?.width,
                    "Chiều cao" to specs.dimensions?.height,
                    "Khối lượng" to specs.dimensions?.curbWeight
                ),
                "Bảo hành" to listOf(
                    "Bảo hành tiêu chuẩn" to specs.warranty?.basic,
                    "Bảo hành pin" to specs.warranty?.battery,
                    "Bảo hành truyền động" to specs.warranty?.drivetrain
                )
            )

            groups.forEachIndexed { index, (title, data) ->
                SpecGroup(title = title, specs = data)
                if (index != groups.lastIndex) {
                    Divider(thickness = 1.dp, color = Color(0xFFE8EAED))
                }
            }
        }
    }
}

@Composable
private fun SpecGroup(
    title: String,
    specs: List<Pair<String, String?>>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen
            )
        )

        specs.forEachIndexed { index, (label, value) ->
            SpecRow(label = label, value = value.orDefaultSpecValue())
            if (index != specs.lastIndex) {
                Divider()
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

private fun String?.orDefaultSpecValue(): String = if (this.isNullOrBlank()) "--" else this

@Composable
private fun SellerContactSection(seller: Seller) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!seller.avatar.isNullOrBlank()) {
                AsyncImage(
                    model = seller.avatar,
                    contentDescription = seller.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = seller.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Liên hệ để thương lượng hoặc đặt lịch xem xe.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
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
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun ErrorState(
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
            Text("Thử lại")
        }
    }
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}

private fun formatAuctionCountdown(auctionEndsAt: String?): String? {
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
            val seconds = duration
                .minusHours(hours)
                .minusMinutes(minutes)
                .seconds
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    } catch (exception: DateTimeParseException) {
        null
    }
}

private fun formatAuctionDate(value: String?): String? {
    if (value.isNullOrBlank()) return null
    return try {
        val dateTime = OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(formatter)
    } catch (exception: DateTimeParseException) {
        null
    }
}
