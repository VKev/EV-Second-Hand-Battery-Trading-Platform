package com.example.evsecondhand.ui.screen.battery

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
import com.example.evsecondhand.ui.viewmodel.BatteryDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDetailScreen(
    batteryId: String,
    onBackClick: () -> Unit,
    viewModel: BatteryDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentBattery = state.battery

    LaunchedEffect(batteryId) {
        viewModel.loadBattery(batteryId)
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
                        modifier = Modifier.navigationBarsPadding()
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
    modifier: Modifier = Modifier
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
                subtitle = "${battery.brand} • ${battery.year}"
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
            ActionButtonsRow()
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
            SpecRow("Hoá học", specs.chemistry)
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
private fun ActionButtonsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* TODO: Buy now action */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(
                text = "Mua ngay",
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
