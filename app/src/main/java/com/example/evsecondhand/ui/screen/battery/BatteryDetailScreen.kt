package com.example.evsecondhand.ui.screen.battery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.BatterySpecifications
import com.example.evsecondhand.data.model.Seller
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.BatteryDetailState
import com.example.evsecondhand.ui.viewmodel.BatteryDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDetailScreen(
    batteryId: String,
    onBackClick: () -> Unit,
    onBidClick: (String) -> Unit,
    onPaymentDashboard: (Battery) -> Unit = {},
    viewModel: BatteryDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentBattery = state.battery
    val context = LocalContext.current

    LaunchedEffect(batteryId) {
        viewModel.loadBattery(batteryId)
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
                title = { Text("Chi tiết pin") },
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
                state.isLoading && currentBattery == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }

                currentBattery != null -> {
                    BatteryDetailContent(
                        battery = currentBattery,
                        state = state,
                        modifier = Modifier.navigationBarsPadding(),
                        onDepositClick = { viewModel.placeDeposit() },
                        onBidClick = { onBidClick(currentBattery.id) },
                        onPaymentDashboard = onPaymentDashboard
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
private fun BatteryDetailContent(
    battery: Battery,
    state: BatteryDetailState,
    modifier: Modifier = Modifier,
    onDepositClick: () -> Unit,
    onBidClick: () -> Unit,
    onPaymentDashboard: (Battery) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeroCard(images = battery.images, isVerified = battery.isVerified)
        }

        item {
            TitleSection(
                title = battery.title,
                price = battery.price,
                subtitle = "${battery.brand} - ${battery.year}"
            )
        }

        item {
            MetricsSection(
                capacity = battery.capacity,
                health = battery.health,
                status = battery.status.replace('_', ' '),
                year = battery.year
            )
        }

        battery.specifications?.let { specs ->
            item {
                SpecificationSection(specs = specs)
            }
        }

        if (battery.isAuction == true) {
            item {
                BatteryDepositSection(
                    battery = battery,
                    state = state,
                    onDepositClick = onDepositClick,
                    onBidClick = onBidClick
                )
            }
        }

        battery.seller?.let { seller ->
            item {
                SellerSection(seller = seller)
            }
        }

        if (battery.description.isNotBlank()) {
            item {
                DescriptionSection(description = battery.description)
            }
        }

        item {
            ActionButtonsRow(
                battery = battery,
                onPaymentDashboard = onPaymentDashboard
            )
        }
    }
}

@Composable
private fun HeroCard(
    images: List<String>,
    isVerified: Boolean
) {
    val imageUrl = images.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Battery image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            if (isVerified) {
                VerifiedBadge(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun VerifiedBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryGreen.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Đã kiểm duyệt",
            style = MaterialTheme.typography.labelMedium.copy(color = PrimaryGreen)
        )
    }
}

@Composable
private fun TitleSection(
    title: String,
    price: Int,
    subtitle: String
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = currencyFormatter.format(price),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = PrimaryGreen
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
    }
}

@Composable
private fun MetricsSection(
    capacity: Int,
    health: Int?,
    status: String,
    year: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Dung lượng",
                value = "$capacity kWh",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Tình trạng",
                value = health?.let { "$it%" } ?: "Chưa rõ",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Trạng thái",
                value = status,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Năm sản xuất",
                value = year.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun SpecificationSection(specs: BatterySpecifications) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thông số kỹ thuật",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            SpecRow("Khối lượng", specs.weight)
            SpecRow("Điện áp", specs.voltage)
            SpecRow("Hóa học", specs.chemistry)
            SpecRow("Suy hao", specs.degradation)
            SpecRow("Thời gian sạc", specs.chargingTime)
            SpecRow("Lắp đặt", specs.installation)
            SpecRow("Bảo hành", specs.warrantyPeriod)
            SpecRow("Nhiệt độ", specs.temperatureRange)
        }
    }
}
@Composable
private fun SpecRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Divider(modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth())
    }
}

@Composable
private fun BatteryDepositSection(
    battery: Battery,
    state: BatteryDetailState,
    onDepositClick: () -> Unit,
    onBidClick: () -> Unit
) {
    val depositAmount = battery.depositAmount?.takeIf { it > 0 }
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val depositAmountText = depositAmount?.let { formatter.format(it) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

            val infoText = if (state.canBid) {
                "Bạn đã sẵn sàng đấu giá phiên này."
            } else {
                "Sau khi thanh toán đặt cọc thành công, bạn sẽ được kích hoạt quyền đặt giá."
            }

            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            if (state.hasDeposit && !state.canBid) {
                Text(
                    text = "Hệ thống đang cập nhật trạng thái đấu giá...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            if (!state.depositError.isNullOrBlank()) {
                Text(
                    text = state.depositError,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                )
            }

            val buttonLabel = when {
                state.canBid -> "Đấu giá ngay"
                depositAmountText != null -> "Đặt cọc ngay"
                else -> "Liên hệ đặt cọc"
            }

            Button(
                onClick = if (state.canBid) onBidClick else onDepositClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !state.isProcessingDeposit
            ) {
                if (state.isProcessingDeposit) {
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
private fun SellerSection(seller: Seller) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = seller.avatar,
                contentDescription = seller.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = seller.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (seller.id.isNotBlank()) {
                    Text(
                        text = "Mã người bán: ${seller.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mô tả",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    battery: Battery?,
    onPaymentDashboard: (Battery) -> Unit
) {
    val isAuctionItem = battery?.isAuction == true ||
        battery?.status?.contains("AUCTION", ignoreCase = true) == true
    val primaryActionLabel = if (isAuctionItem) "Dau gia" else "Mua ngay"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                battery?.let { onPaymentDashboard(it) }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(
                text = primaryActionLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        OutlinedButton(
            onClick = { /* TODO: Add to compare */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
        ) {
            Text(
                text = "Thêm so sánh",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
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

