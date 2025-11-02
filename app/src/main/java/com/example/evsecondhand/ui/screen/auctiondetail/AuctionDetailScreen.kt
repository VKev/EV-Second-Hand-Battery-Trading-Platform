package com.example.evsecondhand.ui.screen.auctiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
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
import com.example.evsecondhand.ui.screen.vehicle.AuctionCountdownBadge
import com.example.evsecondhand.ui.screen.vehicle.DescriptionSection
import com.example.evsecondhand.ui.screen.vehicle.HighlightStatCard
import com.example.evsecondhand.ui.screen.vehicle.QuickHighlightsSection
import com.example.evsecondhand.ui.screen.vehicle.VehicleGalleryCard
import com.example.evsecondhand.ui.screen.vehicle.VehicleOverviewCard
import com.example.evsecondhand.ui.screen.vehicle.VerifiedBadge
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
        // GIỮ NGUYÊN FETCH
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
                title = { Text("Chi tiết đấu giá") },
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
                .background(Color(0xFFF3F5F9))
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: "Đã xảy ra lỗi",
                        onRetry = { viewModel.retry() }
                    )
                }

                uiState.detail != null -> {
                    val rawType = uiState.summary?.listingType ?: uiState.listingType ?: listingType
                    val normalizedType = rawType.uppercase(Locale.ROOT)
                    val vehicleDetail = uiState.vehicle
                    val batteryDetail = uiState.battery
                    val detail = requireNotNull(uiState.detail)

                    when {
                        // CASE 1: ĐẤU GIÁ XE – bám y chang template VehicleDetailScreen
                        normalizedType == "VEHICLE" && vehicleDetail != null -> {
                            VehicleAuctionDetailContent(
                                vehicle = vehicleDetail,
                                detail = detail,
                                summary = uiState.summary,
                                isProcessingDeposit = uiState.isProcessingDeposit,
                                isPlacingBid = uiState.isPlacingBid,
                                onPlaceDeposit = { viewModel.placeDeposit() },
                                onPlaceBid = { amount -> viewModel.placeBid(amount) }
                            )
                        }

                        // CASE 2: ĐẤU GIÁ PIN
                        normalizedType == "BATTERY" && batteryDetail != null -> {
                            BatteryAuctionDetailContent(
                                battery = batteryDetail,
                                detail = detail,
                                summary = uiState.summary,
                                isProcessingDeposit = uiState.isProcessingDeposit,
                                isPlacingBid = uiState.isPlacingBid,
                                onPlaceDeposit = { viewModel.placeDeposit() },
                                onPlaceBid = { amount -> viewModel.placeBid(amount) }
                            )
                        }

                        // CASE 3: DỮ LIỆU SẢN PHẨM ĐANG LOAD RIÊNG
                        uiState.isProductLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = PrimaryGreen
                            )
                        }

                        // CASE 4: fallback – vẫn show theo style mới
                        else -> {
                            AuctionDetailContent(
                                summary = uiState.summary,
                                detail = detail,
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

/**
 * Fallback cho loại đấu giá không phải VEHICLE/BATTERY.
 * Làm lại layout gần giống VehicleDetailScreen: list, card trắng, padding 20dp.
 */
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
            .background(Color(0xFFF3F5F9)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Ảnh cover nếu có
        val imageUrl = summary?.imageUrl ?: detail.images?.firstOrNull() ?: detail.image
        if (imageUrl != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = summary?.title ?: "Ảnh đấu giá"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = summary?.title.orEmpty().ifBlank { "Sản phẩm đấu giá" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Giá ưu tiên: currentBid -> startingPrice
                    val price = detail.currentBid ?: detail.startingPrice ?: summary?.startingPrice
                    price?.let {
                        Text(
                            text = "Giá hiện tại: ${formatCurrency(it)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreen
                            )
                        )
                    }

                    detail.depositAmount?.let {
                        Text(
                            text = "Đặt cọc: ${formatCurrency(it)}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }

                    val timeline = buildTimeline(summary?.auctionStartsAt, summary?.auctionEndsAt ?: detail.auctionEndsAt)
                    if (timeline.isNotBlank()) {
                        Text(
                            text = timeline,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    detail.auctionEndsAt?.let(::computeTimeRemaining)?.let {
                        AuctionCountdownBadge(remaining = it)
                    }
                }
            }
        }

        item {
            AuctionFactsCard(detail = detail)
        }

        detail.description?.takeIf { it.isNotBlank() }?.let { desc ->
            item {
                DescriptionSection(description = desc)
            }
        }

        item {
            AuctionActionSection(
                canBid = canBid,
                hasDeposit = userHasDeposit,
                depositAmount = detail.depositAmount,
                bidIncrement = detail.bidIncrement,
                currentBid = detail.currentBid ?: detail.bids?.firstOrNull()?.amount,
                startingPrice = resolvedStartingPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }
    }
}

/**
 * PHẦN XE – chỉnh lại để giống hẳn VehicleDetailScreen:
 * - dùng VehicleGalleryCard
 * - dùng VehicleOverviewCard nhưng ép price = giá đấu giá (currentBid / starting / vehicle.startingPrice / vehicle.price)
 * - show QuickHighlightsSection, card tóm tắt đấu giá, rồi mới tới card đặt cọc/đấu giá (GIỮ LOGIC CŨ)
 */
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

    // Giá hiển thị ưu tiên: currentBid -> detail.startingPrice -> summary.currentBid -> summary.startingPrice -> vehicle.startingPrice -> vehicle.price
    val displayPrice = detail.currentBid
        ?: detail.startingPrice
        ?: summary?.currentBid
        ?: summary?.startingPrice
        ?: vehicle.startingPrice
        ?: vehicle.price

    // ảnh ưu tiên của auction -> ảnh xe
    val galleryImages = when {
        vehicle.images.isNotEmpty() -> vehicle.images
        summary?.imageUrl != null -> listOf(summary.imageUrl)
        detail.images?.isNotEmpty() == true -> detail.images
        detail.image != null -> listOf(detail.image)
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F9)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            VehicleGalleryCard(
                images = galleryImages,
                isVerified = vehicle.isVerified
            )
        }

        item {
            // ép price để giống template
            VehicleOverviewCard(vehicle = vehicle.copy(price = displayPrice))
        }

        item {
            QuickHighlightsSection(vehicle = vehicle)
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
                currentBid = detail.currentBid ?: detail.bids?.firstOrNull()?.amount,
                startingPrice = displayPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }

        val vehicleSpecs = detail.specifications ?: vehicle.specifications
        vehicleSpecs?.let { specs ->
            item {
                VehicleAuctionSpecification(specs = specs)
            }
        }

        val sellerInfo = vehicle.seller
        sellerInfo?.let { seller ->
            item {
                SellerContactSection(seller = seller)
            }
        }

        val description = detail.description?.takeIf { it.isNotBlank() } ?: vehicle.description
        if (description.isNotBlank()) {
            item {
                DescriptionSection(description = description)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VehicleAuctionSummaryCard(
    vehicle: Vehicle,
    detail: AuctionDetailData,
    summary: AuctionSummary?
) {
    val timeline = buildTimeline(
        startsAt = summary?.auctionStartsAt ?: vehicle.auctionStartsAt,
        endsAt = summary?.auctionEndsAt ?: vehicle.auctionEndsAt ?: detail.auctionEndsAt
    )
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

            val currentAmount = detail.currentBid
                ?: detail.bids?.firstOrNull()?.amount
                ?: summary?.currentBid

            val price = currentAmount
                ?: detail.startingPrice
                ?: summary?.startingPrice
                ?: vehicle.startingPrice
                ?: vehicle.price

            price?.let {
                Text(
                    text = "Giá hiện tại: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            val nextBidAmount = when {
                currentAmount != null && detail.bidIncrement != null ->
                    currentAmount + detail.bidIncrement
                currentAmount != null -> currentAmount
                detail.startingPrice != null && detail.bidIncrement != null ->
                    detail.startingPrice + detail.bidIncrement
                else -> null
            }
            nextBidAmount?.let {
                Text(
                    text = "Giá đặt tối thiểu: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            detail.bidIncrement?.takeIf { it > 0 }?.let {
                Text(
                    text = "Bước giá: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            detail.depositAmount?.takeIf { it > 0 }?.let {
                Text(
                    text = "Tiền đặt cọc: ${formatCurrency(it)}",
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

/**
 * PHẦN PIN – vẫn giữ layout card, nhưng ép lại ảnh + giá giống logic trên
 */
@Composable
private fun VehicleAuctionSpecification(
    specs: VehicleSpecifications
) {
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

            val sections = listOf(
                "Hiệu năng" to listOfNotNull(
                    specs.performance?.topSpeed?.let { "Tốc độ tối đa: $it" },
                    specs.performance?.acceleration?.let { "0-100 km/h: $it" },
                    specs.performance?.motorType?.let { "Động cơ: $it" },
                    specs.performance?.horsepower?.let { "Công suất: $it" }
                ),
                "Kích thước" to listOfNotNull(
                    specs.dimensions?.length?.let { "Chiều dài: $it" },
                    specs.dimensions?.width?.let { "Chiều rộng: $it" },
                    specs.dimensions?.height?.let { "Chiều cao: $it" },
                    specs.dimensions?.curbWeight?.let { "Khối lượng: $it" }
                ),
                "Bảo hành" to listOfNotNull(
                    specs.warranty?.basic?.let { "Tiêu chuẩn: $it" },
                    specs.warranty?.battery?.let { "Ắc quy: $it" },
                    specs.warranty?.drivetrain?.let { "Truyền động: $it" }
                ),
                "Pin & sạc" to listOfNotNull(
                    specs.batteryAndCharging?.range?.let { "Quãng đường: $it" },
                    specs.batteryAndCharging?.chargeTime?.let { "Thời gian sạc: $it" },
                    specs.batteryAndCharging?.chargingSpeed?.let { "Tốc độ sạc: $it" },
                    specs.batteryAndCharging?.batteryCapacity?.let { "Dung lượng pin: $it" }
                )
            )

            sections.filter { it.second.isNotEmpty() }.forEach { (title, items) ->
                SpecificationBlock(title = title, items = items)
            }
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

    val displayPrice = detail.currentBid
        ?: detail.startingPrice
        ?: summary?.currentBid
        ?: summary?.startingPrice
        ?: battery.startingPrice
        ?: battery.price

    val galleryImages = when {
        battery.images.isNotEmpty() -> battery.images
        summary?.imageUrl != null -> listOf(summary.imageUrl)
        detail.images?.isNotEmpty() == true -> detail.images
        detail.image != null -> listOf(detail.image)
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            BatteryAuctionHero(
                images = galleryImages,
                isVerified = battery.isVerified
            )
        }

        item {
            BatteryAuctionTitleSection(
                title = battery.title,
                price = displayPrice,
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

        val batterySpecs = detail.batterySpecifications ?: battery.specifications
        batterySpecs?.let { specs ->
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
                currentBid = detail.currentBid ?: detail.bids?.firstOrNull()?.amount,
                startingPrice = displayPrice,
                isProcessingDeposit = isProcessingDeposit,
                isPlacingBid = isPlacingBid,
                onPlaceDeposit = onPlaceDeposit,
                onPlaceBid = onPlaceBid
            )
        }

        battery.seller?.let { seller ->
            item {
                SellerContactSection(seller = seller)
            }
        }

        val description = detail.description?.takeIf { it.isNotBlank() } ?: battery.description
        if (description.isNotBlank()) {
            item {
                DescriptionSection(description = description)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SellerContactSection(seller: Seller) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    text = "Liên hệ để thương lượng hoặc đặt lịch xem sản phẩm.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

/* ======= phần dưới GIỮ NGUYÊN / CHỈ SỬA NHẸ TEXT ======= */

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
                text = "Th\u00F4ng tin phi\u00EAn \u0111\u1EA5u gi\u00E1",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            val currentAmount = detail.currentBid ?: detail.bids?.firstOrNull()?.amount
            currentAmount?.let {
                FactRow(label = "\u0110\u1EA5u gi\u00E1 hi\u1EC7n t\u1EA1i", value = formatCurrency(it))
            }

            val nextBidAmount = when {
                currentAmount != null && detail.bidIncrement != null ->
                    currentAmount + detail.bidIncrement
                currentAmount != null -> currentAmount
                detail.startingPrice != null && detail.bidIncrement != null ->
                    detail.startingPrice + detail.bidIncrement
                else -> null
            }
            nextBidAmount?.let {
                FactRow(label = "Gi\u00E1 \u0111\u1EB7t t\u1ED1i thi\u1EC3u", value = formatCurrency(it))
            }

            detail.bidIncrement?.takeIf { it > 0 }?.let {
                FactRow(label = "B\u01B0\u1EDBc gi\u00E1", value = formatCurrency(it))
            }

            detail.depositAmount?.takeIf { it > 0 }?.let {
                FactRow(label = "Ti\u1EC1n \u0111\u1EB7t c\u1ECDc", value = formatCurrency(it))
            }

            buildTimeline(detail.auctionStartsAt, detail.auctionEndsAt)
                .takeIf { it.isNotBlank() }
                ?.let { FactRow(label = "Th\u1EDDi gian", value = it) }

            detail.status?.takeIf { it.isNotBlank() }?.let {
                FactRow(label = "Tr\u1EA1ng th\u00E1i", value = it)
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

/**
 * GIỮ LOGIC NÚT: nếu chưa đặt cọc -> nút đặt cọc, nếu rồi -> cho nhập giá và đấu giá.
 */
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
                bidAmountText = if (canBid) {
                    minimumBid?.toString().orEmpty()
                } else {
                    ""
                }
            }

            val bidAmount = bidAmountText.toIntOrNull()
            val bidAmountValid = bidAmount != null && (minimumBid == null || bidAmount >= minimumBid)
            val bidError = if (canBid && bidAmountText.isNotBlank() && !bidAmountValid) {
                minimumBid?.let { "S\u1ED1 ti\u1EC1n t\u1ED1i thi\u1EC3u l\u00E0 ${formatCurrency(it)}" }
            } else null
            val helperMessage = when {
                bidError != null -> bidError
                canBid && minimumBid != null -> "S\u1ED1 ti\u1EC1n t\u1ED1i thi\u1EC3u: ${formatCurrency(minimumBid)}"
                canBid && increment != null -> "B\u01B0\u1EDBc gi\u00E1 t\u1ED1i thi\u1EC3u: ${formatCurrency(increment)}"
                else -> null
            }
            val buttonEnabled = if (canBid) (!isBusy && bidAmountValid) else !isBusy

            if (!canBid) {
                Text(
                    text = "B\u1EA1n c\u1EA7n \u0111\u1EB7t c\u1ECDc tr\u01B0\u1EDBc khi c\u00F3 th\u1EC3 \u0111\u1EA5u gi\u00E1.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                depositAmount?.let {
                    Text(
                        text = "S\u1ED1 ti\u1EC1n \u0111\u1EB7t c\u1ECDc: ${formatCurrency(it)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            } else {
                val infoText = if (hasDeposit) {
                    "Bạn đã đặt cọc thành công. Nhập số tiền bạn muốn đầu giá."
                } else {
                    "B\u1EA1n c\u00F3 th\u1EC3 ti\u1EBFp t\u1EE5c \u0111\u1EA5u gi\u00E1 cho phi\u00EAn n\u00E0y."
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
                    label = { Text("Số tiền đấu giá") },
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
                        modifier = Modifier.height(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    val label = if (canBid) "Đấu giá" else "Đặt cọc ngay"
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
            Text("Thử lại")
        }
    }
}

private fun buildTimeline(startsAt: String?, endsAt: String?): String {
    val startText = startsAt?.let(::formatDateTime)
    val endText = endsAt?.let(::formatDateTime)
    return when {
        startText != null && endText != null -> "T\u1EEB $startText \u0111\u1EBFn $endText"
        startText != null -> "B\u1EAFt \u0111\u1EA7u: $startText"
        endText != null -> "K\u1EBFt th\u00FAc: $endText"
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
} catch (_: DateTimeParseException) {
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
} catch (_: DateTimeParseException) {
    null
}

/* --- battery helpers giữ nguyên như file cũ --- */

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
                    modifier = Modifier.fillMaxSize()
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
                        modifier = Modifier.height(56.dp)
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
                    text = "- $item",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
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
                text = "Thông tin phiên đấu giá",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            val currentAmount = detail.currentBid
                ?: detail.bids?.firstOrNull()?.amount
                ?: summary?.currentBid

            val price = currentAmount
                ?: detail.startingPrice
                ?: summary?.startingPrice

            price?.let {
                Text(
                    text = "Giá hiện tại: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.titleMedium.copy(color = PrimaryGreen, fontWeight = FontWeight.Bold)
                )
            }

            val nextBidAmount = when {
                currentAmount != null && detail.bidIncrement != null ->
                    currentAmount + detail.bidIncrement
                currentAmount != null -> currentAmount
                detail.startingPrice != null && detail.bidIncrement != null ->
                    detail.startingPrice + detail.bidIncrement
                else -> null
            }
            nextBidAmount?.let {
                Text(
                    text = "Giá đặt tối thiểu: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            detail.bidIncrement?.takeIf { it > 0 }?.let {
                Text(
                    text = "Bước giá: ${formatCurrency(it)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            detail.depositAmount?.takeIf { it > 0 }?.let {
                Text(
                    text = "Tiền đặt cọc: ${formatCurrency(it)}",
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






















