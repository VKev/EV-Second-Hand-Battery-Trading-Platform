package com.example.evsecondhand.ui.screen.vehicle

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
import com.example.evsecondhand.data.model.Seller
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.VehicleDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    onBackClick: () -> Unit,
    viewModel: VehicleDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentVehicle = state.vehicle

    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId)
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
private fun VehicleDetailContent(
    vehicle: Vehicle,
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
            HeroCard(images = vehicle.images, isVerified = vehicle.isVerified)
        }

        item {
            TitleSection(
                title = vehicle.title,
                price = vehicle.price,
                subtitle = "${vehicle.brand} ${vehicle.model} • ${vehicle.year}"
            )
        }

        item {
            MetricsSection(
                mileage = vehicle.mileage,
                status = vehicle.status.replace('_', ' '),
                year = vehicle.year,
                price = vehicle.price
            )
        }

        vehicle.specifications?.let { specs ->
            item {
                SpecificationSection(specs = specs)
            }
        }

        vehicle.seller?.let { seller ->
            item {
                SellerSection(seller = seller)
            }
        }

        if (vehicle.description.isNotBlank()) {
            item {
                DescriptionSection(description = vehicle.description)
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
                    contentDescription = "Vehicle image",
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
                        imageVector = Icons.Default.DirectionsCar,
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
    mileage: Int,
    status: String,
    year: Int,
    price: Int
) {
    val mileageFormatter = NumberFormat.getInstance(Locale.US)
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Quãng đường",
                value = "${mileageFormatter.format(mileage)} km",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Trạng thái",
                value = status,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Năm sản xuất",
                value = year.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Giá niêm yết",
                value = currencyFormatter.format(price),
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
private fun SpecificationSection(specs: VehicleSpecifications) {
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

            specs.dimensions?.let {
                SpecRow("Rộng", it.width)
                SpecRow("Cao", it.height)
                SpecRow("Dài", it.length)
                SpecRow("Khối lượng", it.curbWeight)
            }

            specs.performance?.let {
                SpecRow("Tốc độ tối đa", it.topSpeed)
                SpecRow("Động cơ", it.motorType)
                SpecRow("Công suất", it.horsepower)
                SpecRow("0-100 km/h", it.acceleration)
            }

            specs.batteryAndCharging?.let {
                SpecRow("Quãng đường", it.range)
                SpecRow("Thời gian sạc", it.chargeTime)
                SpecRow("Tốc độ sạc", it.chargingSpeed)
                SpecRow("Dung lượng pin", it.batteryCapacity)
            }

            specs.warranty?.let {
                SpecRow("Bảo hành cơ bản", it.basic)
                SpecRow("Bảo hành pin", it.battery)
                SpecRow("Truyền động", it.drivetrain)
            }
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
        Divider(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
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
            onClick = { /* TODO: Buy now */ },
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
            onClick = { /* TODO: Compare */ },
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
