package com.example.evsecondhand.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evsecondhand.data.model.Transaction
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.WalletViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val currencyFormatter = remember {
        NumberFormat.getInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
    }
    val dateFormatter = remember {
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    }
    
    // Handle opening MoMo payment URL
    LaunchedEffect(state.depositPayUrl) {
        state.depositPayUrl?.let { url ->
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
            viewModel.clearDepositPayUrl()
        }
    }
    
    // Show deposit dialog
    if (state.showDepositDialog) {
        DepositDialog(
            onDismiss = { viewModel.hideDepositDialog() },
            onConfirm = { amount ->
                viewModel.depositFunds(amount)
            }
        )
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
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Quản lý Ví",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quản lý số dư cho đấu giá và mua sắm\ntrên EV Market",
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

            // Balance Card
            item {
                BalanceCard(
                    availableBalance = state.availableBalance,
                    lockedBalance = state.lockedBalance,
                    currencyFormatter = currencyFormatter,
                    isLoading = state.isLoading,
                    onDeposit = { viewModel.showDepositDialog() },
                    onWithdraw = { viewModel.withdrawFunds() }
                )
            }

            // Transaction History
            item {
                TransactionHistoryCard(
                    transactions = state.transactions,
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    totalTransactions = state.transactions.size,
                    isLoading = state.isLoadingTransactions,
                    onPageChange = { viewModel.loadTransactions(it) },
                    currencyFormatter = currencyFormatter,
                    dateFormatter = dateFormatter
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    availableBalance: Double,
    lockedBalance: Double,
    currencyFormatter: NumberFormat,
    isLoading: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Số dư khả dụng",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(vertical = 12.dp),
                    color = PrimaryGreen
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "${currencyFormatter.format(availableBalance)} ",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "đ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
            
            // Locked Balance
            if (lockedBalance > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF9E6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Bị khóa: ${currencyFormatter.format(lockedBalance)} đ",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDeposit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nạp tiền", fontSize = 14.sp)
                }

                Button(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Text("Rút tiền", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    transactions: List<Transaction>,
    currentPage: Int,
    totalPages: Int,
    totalTransactions: Int,
    isLoading: Boolean,
    onPageChange: (Int) -> Unit,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch sử giao dịch",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.size(18.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = "Tất cả giao dịch",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                // Scrollable table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column {
                        // Table Header
                        TransactionTableHeader()
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE0E0E0)
                        )

                        // Transaction Rows
                        if (transactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có giao dịch",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            transactions.forEach { transaction ->
                                TransactionTableRow(
                                    transaction = transaction,
                                    currencyFormatter = currencyFormatter,
                                    dateFormatter = dateFormatter
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pagination
                PaginationRow(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    totalTransactions = totalTransactions,
                    displayedCount = transactions.size,
                    onPageChange = onPageChange
                )
            }
        }
    }
}

@Composable
private fun TransactionTableHeader() {
    Row(
        modifier = Modifier.widthIn(min = 800.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("NGÀY", Modifier.width(160.dp))
        TableHeaderCell("LOẠI", Modifier.width(140.dp))
        TableHeaderCell("MÔ TẢ", Modifier.width(300.dp))
        TableHeaderCell("SỐ TIỀN", Modifier.width(160.dp))
        TableHeaderCell("TRẠNG THÁI", Modifier.width(120.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = modifier
    )
}

@Composable
private fun TransactionTableRow(
    transaction: Transaction,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat
) {
    Row(
        modifier = Modifier.widthIn(min = 800.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(
            text = dateFormatter.format(parseTransactionDate(transaction.createdAt)),
            fontSize = 13.sp,
            modifier = Modifier.width(160.dp)
        )

        // Type
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = getTypeBackgroundColor(transaction.type),
            modifier = Modifier.width(140.dp)
        ) {
            Text(
                text = getTypeDisplayText(transaction.type),
                fontSize = 12.sp,
                color = getTypeTextColor(transaction.type),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        // Description
        Text(
            text = transaction.description ?: "-",
            fontSize = 13.sp,
            modifier = Modifier.width(300.dp),
            maxLines = 2
        )

        // Amount
        Text(
            text = formatAmount(transaction.amount, currencyFormatter),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.amount >= 0) PrimaryGreen else Color.Red,
            modifier = Modifier.width(160.dp)
        )

        // Status
        StatusBadge(
            status = transaction.status,
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (status.uppercase()) {
            "COMPLETED" -> Color(0xFFE8F5E9)
            "PENDING" -> Color(0xFFFFF9C4)
            "CANCELLED" -> Color(0xFFFFEBEE)
            else -> Color(0xFFF5F5F5)
        },
        modifier = modifier
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            color = when (status.uppercase()) {
                "COMPLETED" -> Color(0xFF2E7D32)
                "PENDING" -> Color(0xFFF57F17)
                "CANCELLED" -> Color(0xFFC62828)
                else -> TextSecondary
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    totalTransactions: Int,
    displayedCount: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hiển thị 1 đến $displayedCount\ntrong tổng số $totalTransactions giao dịch",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Text(
                    text = "Trước",
                    fontSize = 13.sp,
                    color = if (currentPage > 1) Color(0xFF2196F3) else TextSecondary
                )
            }
            
            (1..minOf(totalPages, 3)).forEach { page ->
                val isSelected = page == currentPage
                Surface(
                    onClick = { onPageChange(page) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = page.toString(),
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            TextButton(
                onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Text(
                    text = "Sau",
                    fontSize = 13.sp,
                    color = if (currentPage < totalPages) Color(0xFF2196F3) else TextSecondary
                )
            }
        }
    }
}

// Helper functions
private fun getTypeDisplayText(type: String): String = when (type.uppercase()) {
    "DEPOSIT" -> "Nạp tiền"
    "WITHDRAWAL" -> "Rút tiền"
    "AUCTION_DEPOSIT" -> "Đặt cọc đấu giá"
    "AUCTION_BID" -> "Đặt giá"
    "PURCHASE" -> "Mua hàng"
    else -> type
}

private fun getTypeBackgroundColor(type: String): Color = when (type.uppercase()) {
    "DEPOSIT" -> Color(0xFFE8F5E9)
    "WITHDRAWAL" -> Color(0xFFFCE4EC)
    "AUCTION_DEPOSIT" -> Color(0xFFE3F2FD)
    "AUCTION_BID" -> Color(0xFFE3F2FD)
    "PURCHASE" -> Color(0xFFF3E5F5)
    else -> Color(0xFFF5F5F5)
}

private fun getTypeTextColor(type: String): Color = when (type.uppercase()) {
    "DEPOSIT" -> Color(0xFF2E7D32)
    "WITHDRAWAL" -> Color(0xFFC2185B)
    "AUCTION_DEPOSIT" -> Color(0xFF1976D2)
    "AUCTION_BID" -> Color(0xFF1976D2)
    "PURCHASE" -> Color(0xFF7B1FA2)
    else -> TextSecondary
}

private fun formatAmount(amount: Double, formatter: NumberFormat): String {
    val prefix = if (amount >= 0) "+" else ""
    return "$prefix${formatter.format(amount)} đ"
}

private fun parseTransactionDate(dateString: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

@Composable
private fun DepositDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    val quickAmounts = listOf(50000, 100000, 200000, 500000, 1000000)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nạp tiền vào ví",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chọn số tiền hoặc nhập số tiền muốn nạp:",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                // Quick amount buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.take(3).forEach { quickAmount ->
                        OutlinedButton(
                            onClick = { 
                                amount = quickAmount.toString()
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (amount == quickAmount.toString()) 
                                    PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = "${quickAmount / 1000}K",
                                fontSize = 12.sp,
                                color = if (amount == quickAmount.toString()) 
                                    PrimaryGreen else Color.Gray
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.drop(3).forEach { quickAmount ->
                        OutlinedButton(
                            onClick = { 
                                amount = quickAmount.toString()
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (amount == quickAmount.toString()) 
                                    PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = "${quickAmount / 1000}K",
                                fontSize = 12.sp,
                                color = if (amount == quickAmount.toString()) 
                                    PrimaryGreen else Color.Gray
                            )
                        }
                    }
                    // Empty spacer for alignment
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // Custom amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            amount = it
                            errorMessage = ""
                        }
                    },
                    label = { Text("Số tiền (đ)") },
                    placeholder = { Text("Nhập số tiền") },
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Text(
                            text = "đ",
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
                
                // Info text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Thanh toán qua MoMo",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountInt = amount.toIntOrNull()
                    when {
                        amountInt == null || amountInt <= 0 -> {
                            errorMessage = "Vui lòng nhập số tiền hợp lệ"
                        }
                        amountInt < 10000 -> {
                            errorMessage = "Số tiền tối thiểu là 10.000đ"
                        }
                        amountInt > 50000000 -> {
                            errorMessage = "Số tiền tối đa là 50.000.000đ"
                        }
                        else -> {
                            onConfirm(amountInt)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Hủy")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}