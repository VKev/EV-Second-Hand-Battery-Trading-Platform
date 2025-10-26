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
import com.example.evsecondhand.data.model.TransactionType
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import com.example.evsecondhand.ui.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormatter = remember {
        NumberFormat.getInstance(Locale.US).apply {
            maximumFractionDigits = 3
        }
    }
    val dateFormatter = remember {
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    }

    // Không dùng Scaffold với TopAppBar nữa, chỉ dùng LazyColumn
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header - Wallet Management
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Wallet Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage your funds for auctions and purchases\non EcoTrade EV",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        // Balance Card
        item {
            BalanceCard(
                balance = state.balance,
                currencyFormatter = currencyFormatter,
                onDeposit = { viewModel.depositFunds() },
                onWithdraw = { viewModel.withdrawFunds() }
            )
        }

        // Transaction History
        item {
            TransactionHistoryCard(
                transactions = state.transactions,
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                totalTransactions = state.totalTransactions,
                onPageChange = { viewModel.loadTransactions(it) },
                currencyFormatter = currencyFormatter,
                dateFormatter = dateFormatter
            )
        }
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    currencyFormatter: NumberFormat,
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
                text = "Current Balance",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = "${currencyFormatter.format(balance)} ",
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
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Deposit Funds", fontSize = 14.sp)
                }

                Button(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Withdraw", fontSize = 14.sp)
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
                    text = "Transaction History",
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
                        text = "All Transactions",
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
                                text = "No transactions yet",
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

@Composable
private fun TransactionTableHeader() {
    Row(
        modifier = Modifier.widthIn(min = 800.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("DATE", Modifier.width(160.dp))
        TableHeaderCell("TYPE", Modifier.width(140.dp))
        TableHeaderCell("DESCRIPTION", Modifier.width(300.dp))
        TableHeaderCell("AMOUNT", Modifier.width(160.dp))
        TableHeaderCell("STATUS", Modifier.width(120.dp))
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
            text = dateFormatter.format(parseTransactionDate(transaction.date)),
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
                text = getTypeText(transaction.type),
                fontSize = 12.sp,
                color = getTypeTextColor(transaction.type),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        // Description
        Text(
            text = transaction.description,
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
            status = getTransactionStatus(transaction),
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (status) {
            "Completed" -> Color(0xFFE8F5E9)
            "Pending" -> Color(0xFFFFF9C4)
            "CANCELLED" -> Color(0xFFFFEBEE)
            else -> Color(0xFFF5F5F5)
        },
        modifier = modifier
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            color = when (status) {
                "Completed" -> Color(0xFF2E7D32)
                "Pending" -> Color(0xFFF57F17)
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
            text = "Showing 1 to $displayedCount\nof $totalTransactions transactions",
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
                    text = "Previous",
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
                    text = "Next",
                    fontSize = 13.sp,
                    color = if (currentPage < totalPages) Color(0xFF2196F3) else TextSecondary
                )
            }
        }
    }
}

// Helper functions
private fun getTypeText(type: TransactionType): String = when (type) {
    TransactionType.DEPOSIT -> "Deposit"
    TransactionType.WITHDRAWAL -> "Withdraw"
    TransactionType.PURCHASE -> "Purchase"
    TransactionType.AUCTION_BID -> "Auction Bid"
}

private fun getTypeBackgroundColor(type: TransactionType): Color = when (type) {
    TransactionType.DEPOSIT -> Color(0xFFE8F5E9)
    TransactionType.WITHDRAWAL -> Color(0xFFFCE4EC)
    TransactionType.PURCHASE -> Color(0xFFF3E5F5)
    TransactionType.AUCTION_BID -> Color(0xFFE3F2FD)
}

private fun getTypeTextColor(type: TransactionType): Color = when (type) {
    TransactionType.DEPOSIT -> Color(0xFF2E7D32)
    TransactionType.WITHDRAWAL -> Color(0xFFC2185B)
    TransactionType.PURCHASE -> Color(0xFF7B1FA2)
    TransactionType.AUCTION_BID -> Color(0xFF1976D2)
}

private fun formatAmount(amount: Double, formatter: NumberFormat): String {
    val prefix = if (amount >= 0) "+" else ""
    return "$prefix${formatter.format(amount)} đ"
}

private fun getTransactionStatus(transaction: Transaction): String {
    // Mock logic - replace with actual status from transaction
    return when {
        transaction.amount == -2000.0 -> "Completed"
        transaction.amount == 2000.0 -> if (transaction.id == "2" || transaction.id == "4") "CANCELLED" else "Completed"
        transaction.amount == 5000.0 -> if (transaction.id == "4") "CANCELLED" else "Completed"
        transaction.amount == 50.0 -> if (transaction.id == "6" || transaction.id == "7") "Pending" else "Completed"
        else -> "Completed"
    }
}

private fun parseTransactionDate(dateString: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}