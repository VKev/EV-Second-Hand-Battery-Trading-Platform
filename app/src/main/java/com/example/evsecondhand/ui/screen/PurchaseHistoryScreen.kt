package com.example.evsecondhand.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.PurchaseTransaction
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.PurchaseHistoryViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchaseHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: PurchaseHistoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormatter = remember {
        NumberFormat.getInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
    }
    val dateFormatter = remember {
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back button & Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = "Lịch sử mua hàng",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Xem lịch sử giao dịch và theo dõi đơn hàng của bạn",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Error Message
            state.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Summary Card
            if (state.purchases.isNotEmpty()) {
                item {
                    SummaryCard(
                        totalPurchases = state.totalResults,
                        currentPage = state.currentPage,
                        totalPages = state.totalPages
                    )
                }
            }

            // Loading State
            if (state.isLoading && state.purchases.isEmpty()) {
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

            // Purchase Cards
            if (!state.isLoading && state.purchases.isEmpty()) {
                item {
                    EmptyPurchaseState()
                }
            } else {
                items(state.purchases) { purchase ->
                    PurchaseCard(
                        purchase = purchase,
                        currencyFormatter = currencyFormatter,
                        dateFormatter = dateFormatter
                    )
                }
            }

            // Pagination
            if (state.purchases.isNotEmpty() && state.totalPages > 1) {
                item {
                    PaginationRow(
                        currentPage = state.currentPage,
                        totalPages = state.totalPages,
                        onPageChange = { viewModel.loadPurchases(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalPurchases: Int,
    currentPage: Int,
    totalPages: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Đơn hàng của tôi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trang $currentPage / $totalPages",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryGreen.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$totalPurchases đơn",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PurchaseCard(
    purchase: PurchaseTransaction,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat
) {
    val imageUrl = purchase.vehicle?.images?.firstOrNull() 
        ?: purchase.battery?.images?.firstOrNull()
    val title = purchase.vehicle?.title ?: purchase.battery?.title ?: "Sản phẩm không xác định"
    val productType = if (purchase.vehicle != null) "Xe điện" else "Pin"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mã đơn:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = purchase.id.take(15) + "...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                StatusBadge(status = purchase.status)
            }

            // Date
            Text(
                text = "lúc ${dateFormatter.format(parseDate(purchase.createdAt))}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Divider(color = Color(0xFFE0E0E0))

            // Product Info with Image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (productType == "Vehicle") Icons.Default.DirectionsCar else Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Product Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (productType == "Vehicle") Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = productType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (productType == "Xe điện") Color(0xFF1976D2) else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "${currencyFormatter.format(purchase.finalPrice)} đ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            // Payment Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Payment",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Phương thức thanh toán",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = getPaymentMethodDisplay(purchase.paymentGateway),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Transaction Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Transaction Date",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Ngày giao dịch",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parseDate(purchase.createdAt)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Write Review Button
            if (purchase.status.uppercase() == "COMPLETED" && purchase.review == null) {
                Button(
                    onClick = { /* TODO: Write review */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Viết đánh giá",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when (status.uppercase()) {
            "COMPLETED" -> Color(0xFFE8F5E9)
            "PENDING" -> Color(0xFFFFF9C4)
            "CANCELLED" -> Color(0xFFFFEBEE)
            "PROCESSING" -> Color(0xFFE3F2FD)
            else -> Color(0xFFF5F5F5)
        }
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = when (status.uppercase()) {
                "COMPLETED" -> Color(0xFF2E7D32)
                "PENDING" -> Color(0xFFF57F17)
                "CANCELLED" -> Color(0xFFC62828)
                "PROCESSING" -> Color(0xFF1976D2)
                else -> TextSecondary
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyPurchaseState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = "Chưa có đơn hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = "Lịch sử mua hàng của bạn sẽ hiển thị tại đây",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Text(
                    text = "Trước",
                    fontSize = 14.sp,
                    color = if (currentPage > 1) Color(0xFF2196F3) else TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            (1..minOf(totalPages, 3)).forEach { page ->
                val isSelected = page == currentPage
                Surface(
                    onClick = { onPageChange(page) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = page.toString(),
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Text(
                    text = "Sau",
                    fontSize = 14.sp,
                    color = if (currentPage < totalPages) Color(0xFF2196F3) else TextSecondary
                )
            }
        }
    }
}

// Helper functions
private fun parseDate(dateString: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

private fun getPaymentMethodDisplay(gateway: String): String = when (gateway.uppercase()) {
    "WALLET" -> "Ví điện tử"
    "MOMO" -> "MoMo"
    "VNPAY" -> "VNPay"
    "BANK" -> "Bank Transfer"
    else -> gateway
}