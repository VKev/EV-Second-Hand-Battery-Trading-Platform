package com.example.evsecondhand.ui.screen.auctiondetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.AuctionDetailData
import com.example.evsecondhand.data.model.AuctionSummary
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.BatterySpecifications
import com.example.evsecondhand.data.model.Seller
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.AuctionDetailViewModel
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(
    listingType: String,
    listingId: String,
    onBackClick: () -> Unit,
    viewModel: AuctionDetailViewModel = viewModel()
) {
    LaunchedEffect(listingType, listingId) {
        viewModel.loadDetail(listingType, listingId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.summary?.title ?: "Chi tiet dau gia") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            val errorMessage = uiState.error
            val detailState = uiState.detail
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }

                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage,
                        onRetry = { viewModel.retry() }
                    )
                }

                detailState != null -> {
                    val rawType = uiState.summary?.listingType ?: uiState.listingType ?: listingType
                    val normalizedType = rawType.uppercase(Locale.ROOT)
                    val vehicleDetail = uiState.vehicle
                    val batteryDetail = uiState.battery
                    when {
                        normalizedType == "VEHICLE" && vehicleDetail != null -> {
                            VehicleAuctionDetailContent(
                                vehicle = vehicleDetail,
                                detail = detailState,
                                summary = uiState.summary,
                                isProcessingDeposit = uiState.isProcessingDeposit,
                                isPlacingBid = uiState.isPlacingBid,
                                onPlaceDeposit = { viewModel.placeDeposit() },
                                onPlaceBid = { amount -> viewModel.placeBid(amount) }
                            )
                        }
                        normalizedType == "BATTERY" && batteryDetail != null -> {
                            BatteryAuctionDetailContent(
                                battery = batteryDetail,
                                detail = detailState,
                                summary = uiState.summary,
                                isProcessingDeposit = uiState.isProcessingDeposit,
                                isPlacingBid = uiState.isPlacingBid,
                                onPlaceDeposit = { viewModel.placeDeposit() },
                                onPlaceBid = { amount -> viewModel.placeBid(amount) }
                            )
                        }
                        uiState.isProductLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = PrimaryGreen
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                uiState.productError?.let { message ->
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                    )
                                }
                                AuctionDetailContent(
                                    summary = uiState.summary,
                                    detail = detailState,
                                    isProcessingDeposit = uiState.isProcessingDeposit,
                                    isPlacingBid = uiState.isPlacingBid,
                                    onPlaceDeposit = { viewModel.placeDeposit() },
                                    onPlaceBid = { amount -> viewModel.placeBid(amount) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuctionDetailContent(
    summary: AuctionSummary?,
    detail: AuctionDetailData,
    isProcessingDeposit: Boolean,
    isPlacingBid: Boolean,
    onPlaceDeposit: () -> Unit,
    onPlaceBid: (Int) -> Unit
) {
    val userHasDeposit = detail.hasUserDeposit == true
    val hasDeposit = userHasDeposit || detail.hasDeposit == true
    val canBid = hasDeposit || detail.hasUserBid == true
    val resolvedStartingPrice = detail.startingPrice ?: summary?.startingPrice

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            DetailHeader(summary = summary, detail = detail)
        }

        item {
            AuctionFactsCard(detail = detail)
        }

        detail.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        item {
            AuctionActionSection(
                canBid = canBid,
                hasDeposit = userHasDeposit,
                depositAmount = detail.depositAmount,
                bidIncrement = detail.bidIncrement,
                currentBid = detail.currentBid,
                startingPrice = resolvedStartingPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }
    }
}

@Composable
private fun DetailHeader(summary: AuctionSummary?, detail: AuctionDetailData) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val imageUrl = summary?.imageUrl ?: detail.images?.firstOrNull() ?: detail.image
            AsyncImage(
                model = imageUrl,
                contentDescription = summary?.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = summary?.title.orEmpty().ifBlank { "San pham dau gia" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                summary?.currentBid?.let {
                    Text(
                        text = "Gia hien tai: ${formatCurrency(it)}",
                        style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen)
                    )
                } ?: summary?.startingPrice?.let {
                    Text(
                        text = "Gia khoi diem: ${formatCurrency(it)}",
                        style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen)
                    )
                }

                detail.depositAmount?.let {
                    Text(
                        text = "Dat coc: ${formatCurrency(it)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }

                val timeline = buildTimeline(summary?.auctionStartsAt, summary?.auctionEndsAt)
                if (timeline.isNotBlank()) {
                    Text(
                        text = timeline,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                detail.auctionEndsAt?.let(::computeTimeRemaining)?.let {
                    Text(
                        text = "Con lai: $it",
                        style = MaterialTheme.typography.bodySmall.copy(color = PrimaryGreen, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleAuctionDetailContent(
    vehicle: Vehicle,
    detail: AuctionDetailData,
    summary: AuctionSummary?,
    isProcessingDeposit: Boolean,
    isPlacingBid: Boolean,
    onPlaceDeposit: () -> Unit,
    onPlaceBid: (Int) -> Unit
) {
    val userHasDeposit = detail.hasUserDeposit == true
    val hasDeposit = userHasDeposit || detail.hasDeposit == true
    val canBid = hasDeposit || detail.hasUserBid == true
    val resolvedStartingPrice = detail.startingPrice ?: summary?.startingPrice ?: vehicle.startingPrice

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F9)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            VehicleAuctionGallery(
                images = vehicle.images,
                isVerified = vehicle.isVerified
            )
        }

        item {
            VehicleAuctionOverviewCard(vehicle = vehicle)
        }

        item {
            VehicleAuctionHighlights(vehicle = vehicle)
        }

        item {
            VehicleAuctionSummaryCard(
                vehicle = vehicle,
                detail = detail,
                summary = summary
            )
        }

        item {
            AuctionActionSection(
                canBid = canBid,
                hasDeposit = userHasDeposit,
                depositAmount = detail.depositAmount,
                bidIncrement = detail.bidIncrement,
                currentBid = detail.currentBid,
                startingPrice = resolvedStartingPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }

        vehicle.specifications?.let { specs ->
            item {
                VehicleAuctionSpecificationSection(specs = specs)
            }
        }

        vehicle.seller?.let { seller ->
            item {
                AuctionSellerCard(seller = seller)
            }
        }

        if (vehicle.description.isNotBlank()) {
            item {
                AuctionDescriptionCard(description = vehicle.description)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VehicleAuctionGallery(
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
                    AuctionGalleryThumbnail(
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
private fun VehicleAuctionOverviewCard(vehicle: Vehicle) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = vehicle.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Giá khởi điểm: ${formatCurrency(vehicle.startingPrice ?: vehicle.price)}",
                style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleTraitChip(label = vehicle.brand)
                VehicleTraitChip(label = vehicle.model)
                VehicleTraitChip(label = vehicle.year.toString())
            }

            Surface(
                color = PrimaryGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = vehicle.status.replace('_', ' '),
                    style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun VehicleTraitChip(label: String) {
    Surface(
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFFF0F4F8)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun VehicleAuctionHighlights(vehicle: Vehicle) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thông số nổi bật",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HighlightStatCard(
                    label = "Số km",
                    value = "${vehicle.mileage} km",
                    modifier = Modifier.weight(1f)
                )
                HighlightStatCard(
                    label = "Năm SX",
                    value = vehicle.year.toString(),
                    modifier = Modifier.weight(1f)
                )
                HighlightStatCard(
                    label = "Tình trạng",
                    value = vehicle.status.replace('_', ' '),
                    modifier = Modifier.weight(1f)
                )
            }

            vehicle.specifications?.performance?.let { performance ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    performance.motorType?.let {
                        HighlightStatCard(
                            label = "Động cơ",
                            value = it,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    performance.topSpeed?.let {
                        HighlightStatCard(
                            label = "Tốc độ tối đa",
                            value = it,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    performance.acceleration?.let {
                        HighlightStatCard(
                            label = "0-100 km/h",
                            value = it,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
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
private fun VehicleAuctionSummaryCard(
    vehicle: Vehicle,
    detail: AuctionDetailData,
    summary: AuctionSummary?
) {
    val timeline = buildTimeline(summary?.auctionStartsAt ?: vehicle.auctionStartsAt, summary?.auctionEndsAt ?: vehicle.auctionEndsAt)
    val remaining = detail.auctionEndsAt?.let(::computeTimeRemaining)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thông tin đấu giá",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            detail.currentBid?.let {
                Text(
                    text = "Giá hiện tại: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
                )
            } ?: detail.startingPrice?.let {
                Text(
                    text = "Giá khởi điểm: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
                )
            }

            detail.depositAmount?.let {
                Text(
                    text = "Đặt cọc: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
            detail.bidIncrement?.takeIf { it > 0 }?.let {
                Text(
                    text = "Bước giá: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            if (timeline.isNotBlank()) {
                Text(
                    text = timeline,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            remaining?.let {
                AuctionCountdownBadge(remaining = it)
            }
        }
    }
}

@Composable
private fun VehicleAuctionSpecificationSection(specs: VehicleSpecifications) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Chi tiết kỹ thuật",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            specs.performance?.let { performance ->
                SpecificationBlock(
                    title = "Hiệu năng",
                    items = listOfNotNull(
                        performance.motorType?.let { "Động cơ: $it" },
                        performance.horsepower?.let { "Công suất: $it" },
                        performance.topSpeed?.let { "Tốc độ tối đa: $it" },
                        performance.acceleration?.let { "0-100 km/h: $it" }
                    )
                )
            }

            specs.batteryAndCharging?.let { battery ->
                SpecificationBlock(
                    title = "Pin & Sạc",
                    items = listOfNotNull(
                        battery.range?.let { "Quãng đường: $it" },
                        battery.chargeTime?.let { "Thời gian sạc: $it" },
                        battery.chargingSpeed?.let { "Tốc độ sạc: $it" },
                        battery.batteryCapacity?.let { "Dung lượng pin: $it" }
                    )
                )
            }

            specs.dimensions?.let { dims ->
                SpecificationBlock(
                    title = "Kích thước",
                    items = listOfNotNull(
                        dims.length?.let { "Chiều dài: $it" },
                        dims.width?.let { "Chiều rộng: $it" },
                        dims.height?.let { "Chiều cao: $it" },
                        dims.curbWeight?.let { "Khối lượng: $it" }
                    )
                )
            }

            specs.warranty?.let { warranty ->
                SpecificationBlock(
                    title = "Bảo hành",
                    items = listOfNotNull(
                        warranty.basic?.let { "Cơ bản: $it" },
                        warranty.battery?.let { "Pin: $it" },
                        warranty.drivetrain?.let { "Hệ thống truyền động: $it" }
                    )
                )
            }
        }
    }
}

@Composable
private fun SpecificationBlock(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun BatteryAuctionHero(
    images: List<String>,
    isVerified: Boolean
) {
    val heroImage = images.firstOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!heroImage.isNullOrBlank()) {
                AsyncImage(
                    model = heroImage,
                    contentDescription = "Battery image",
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
                        imageVector = Icons.Default.Bolt,
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
}

@Composable
private fun BatteryAuctionTitleSection(
    title: String,
    price: Int,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = formatCurrency(price),
                style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun BatteryAuctionMetrics(
    capacity: Int,
    health: Int?,
    status: String,
    year: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thông số pin",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HighlightStatCard(
                    label = "Dung lượng",
                    value = "$capacity Ah",
                    modifier = Modifier.weight(1f)
                )
                HighlightStatCard(
                    label = "Sức khỏe",
                    value = health?.let { "$it%" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                HighlightStatCard(
                    label = "Năm SX",
                    value = year.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Tình trạng: ${status.replace('_', ' ')}",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun BatteryAuctionSpecification(specs: BatterySpecifications) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Chi tiết kỹ thuật",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            SpecificationBlock(
                title = "Thông số chính",
                items = listOfNotNull(
                    specs.weight?.let { "Trọng lượng: $it" },
                    specs.voltage?.let { "Điện áp: $it" },
                    specs.chemistry?.let { "Hóa học: $it" },
                    specs.degradation?.let { "Mức hao hụt: $it" }
                )
            )

            SpecificationBlock(
                title = "Sạc & bảo trì",
                items = listOfNotNull(
                    specs.chargingTime?.let { "Thời gian sạc: $it" },
                    specs.installation?.let { "Lắp đặt: $it" },
                    specs.warrantyPeriod?.let { "Bảo hành: $it" },
                    specs.temperatureRange?.let { "Nhiệt độ hoạt động: $it" }
                )
            )
        }
    }
}

@Composable
private fun BatteryAuctionSummaryCard(
    detail: AuctionDetailData,
    summary: AuctionSummary?
) {
    val timeline = buildTimeline(summary?.auctionStartsAt, summary?.auctionEndsAt ?: detail.auctionEndsAt)
    val remaining = detail.auctionEndsAt?.let(::computeTimeRemaining)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thông tin đấu giá",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            detail.currentBid?.let {
                Text(
                    text = "Giá hiện tại: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
                )
            } ?: detail.startingPrice?.let {
                Text(
                    text = "Giá khởi điểm: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
                )
            }

            detail.depositAmount?.let {
                Text(
                    text = "Đặt cọc: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
            detail.bidIncrement?.takeIf { it > 0 }?.let {
                Text(
                    text = "Bước giá: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            if (timeline.isNotBlank()) {
                Text(
                    text = timeline,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            remaining?.let {
                AuctionCountdownBadge(remaining = it)
            }
        }
    }
}

@Composable
private fun AuctionSellerCard(seller: Seller) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = PrimaryGreen.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = seller.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryGreen
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = seller.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Người bán tin cậy, sẵn sàng hỗ trợ bạn.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun AuctionDescriptionCard(description: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun AuctionGalleryThumbnail(
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
                text = "Đã xác thực",
                style = MaterialTheme.typography.labelMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
            )
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
                text = "Thời gian còn lại",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC27A00), fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = remaining,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFC27A00), fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun BatteryAuctionDetailContent(
    battery: Battery,
    detail: AuctionDetailData,
    summary: AuctionSummary?,
    isProcessingDeposit: Boolean,
    isPlacingBid: Boolean,
    onPlaceDeposit: () -> Unit,
    onPlaceBid: (Int) -> Unit
) {
    val userHasDeposit = detail.hasUserDeposit == true
    val hasDeposit = userHasDeposit || detail.hasDeposit == true
    val canBid = hasDeposit || detail.hasUserBid == true
    val resolvedStartingPrice = detail.startingPrice ?: summary?.startingPrice ?: battery.startingPrice ?: battery.price

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            BatteryAuctionHero(
                images = battery.images,
                isVerified = battery.isVerified
            )
        }

        item {
            BatteryAuctionTitleSection(
                title = battery.title,
                price = battery.price,
                subtitle = "${battery.brand} - ${battery.year}"
            )
        }

        item {
            BatteryAuctionMetrics(
                capacity = battery.capacity,
                health = battery.health,
                status = battery.status,
                year = battery.year
            )
        }

        battery.specifications?.let { specs ->
            item {
                BatteryAuctionSpecification(specs)
            }
        }

        item {
            BatteryAuctionSummaryCard(
                detail = detail,
                summary = summary
            )
        }

        item {
            AuctionActionSection(
                canBid = canBid,
                hasDeposit = userHasDeposit,
                depositAmount = detail.depositAmount,
                bidIncrement = detail.bidIncrement,
                currentBid = detail.currentBid,
                startingPrice = resolvedStartingPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }

        battery.seller?.let { seller ->
            item {
                AuctionSellerCard(seller = seller)
            }
        }

        if (battery.description.isNotBlank()) {
            item {
                AuctionDescriptionCard(description = battery.description)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AuctionFactsCard(detail: AuctionDetailData) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thong tin phien dau gia",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            detail.bidIncrement?.takeIf { it > 0 }?.let {
                FactRow(label = "Buoc gia", value = formatCurrency(it))
            }

            detail.currentBid?.let {
                FactRow(label = "Gia hien tai", value = formatCurrency(it))
            }

            detail.status?.takeIf { it.isNotBlank() }?.let {
                FactRow(label = "Trang thai", value = it)
            }
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

@Composable
private fun AuctionActionSection(
    canBid: Boolean,
    hasDeposit: Boolean,
    depositAmount: Int?,
    bidIncrement: Int?,
    currentBid: Int?,
    startingPrice: Int?,
    isProcessingDeposit: Boolean,
    isPlacingBid: Boolean,
    onPlaceDeposit: () -> Unit,
    onPlaceBid: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isBusy = if (canBid) isPlacingBid else isProcessingDeposit
            val baseAmount = currentBid?.takeIf { it > 0 } ?: startingPrice
            val increment = bidIncrement?.takeIf { it > 0 }
            val fallbackIncrement = increment ?: 50_000
            val minimumBid = if (canBid) {
                when {
                    baseAmount != null -> baseAmount + fallbackIncrement
                    else -> fallbackIncrement
                }
            } else null

            var bidAmountText by rememberSaveable(canBid) { mutableStateOf("") }
            LaunchedEffect(canBid, minimumBid) {
                if (canBid) {
                    bidAmountText = minimumBid?.toString().orEmpty()
                } else {
                    bidAmountText = ""
                }
            }

            val bidAmount = bidAmountText.toIntOrNull()
            val bidAmountValid = bidAmount != null && (minimumBid == null || bidAmount >= minimumBid)
            val bidError = if (canBid && bidAmountText.isNotBlank() && !bidAmountValid) {
                minimumBid?.let { "So tien toi thieu la ${formatCurrency(it)}" }
            } else null
            val helperMessage = when {
                bidError != null -> bidError
                canBid && minimumBid != null -> "So tien toi thieu: ${formatCurrency(minimumBid)}"
                canBid && increment != null -> "Buoc gia toi thieu: ${formatCurrency(increment)}"
                else -> null
            }
            val buttonEnabled = if (canBid) (!isBusy && bidAmountValid) else !isBusy

            if (!canBid) {
                Text(
                    text = "Ban can dat coc truoc khi co the dau gia.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                depositAmount?.let {
                    Text(
                        text = "So tien dat coc: ${formatCurrency(it)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                val infoText = if (hasDeposit) {
                    "Ban da dat coc thanh cong. Nhap so tien ban muon dau gia."
                } else {
                    "Ban co the tiep tuc dau gia cho phien nay."
                }
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                OutlinedTextField(
                    value = bidAmountText,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        bidAmountText = filtered
                    },
                    label = { Text("So tien dau gia") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = bidError != null,
                    supportingText = {
                        helperMessage?.let { message ->
                            val color = if (bidError != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                TextSecondary
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(color = color)
                            )
                        }
                    }
                )
            }

            Button(
                onClick = {
                    if (canBid) {
                        bidAmount?.let(onPlaceBid)
                    } else {
                        onPlaceDeposit()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = buttonEnabled
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    val label = if (canBid) "Dau gia" else "Dat coc ngay"
                    Text(label, color = Color.White)
                }
            }
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
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Thu lai")
        }
    }
}

private fun buildTimeline(startsAt: String?, endsAt: String?): String {
    val startText = startsAt?.let(::formatDateTime)
    val endText = endsAt?.let(::formatDateTime)
    return when {
        startText != null && endText != null -> "Tu $startText den $endText"
        startText != null -> "Bat dau: $startText"
        endText != null -> "Ket thuc: $endText"
        else -> ""
    }
}

private fun computeTimeRemaining(endsAt: String): String? = try {
    val end = OffsetDateTime.parse(endsAt)
    val now = OffsetDateTime.now(end.offset)
    if (end.isAfter(now)) {
        val duration = Duration.between(now, end)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        val seconds = duration.minusHours(hours).minusMinutes(minutes).seconds
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        null
    }
} catch (exception: DateTimeParseException) {
    null
}

private fun formatCurrency(value: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(value)
}

private fun formatDateTime(value: String): String? = try {
    val dateTime = OffsetDateTime.parse(value)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    dateTime.format(formatter)
} catch (exception: DateTimeParseException) {
    null
}
