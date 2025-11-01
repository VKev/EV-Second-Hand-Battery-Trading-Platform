package com.example.evsecondhand.ui.screen.checkout

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.evsecondhand.data.model.PaymentMethod
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.CheckoutState
import com.example.evsecondhand.ui.viewmodel.CheckoutViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    listingId: String,
    listingType: String,
    listingName: String,
    listingPrice: Int,
    listingImage: String?,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    onNavigateToWallet: () -> Unit,
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: (transactionId: String, productName: String, amount: Int, paymentMethod: String) -> Unit
) {
    val uiState by checkoutViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Initialize checkout data
    LaunchedEffect(listingId) {
        checkoutViewModel.initCheckout(
            listingId = listingId,
            listingType = listingType,
            listingName = listingName,
            listingPrice = listingPrice,
            listingImage = listingImage
        )
    }
    
    // Handle checkout state
    LaunchedEffect(uiState.checkoutState) {
        when (val state = uiState.checkoutState) {
            is CheckoutState.Success -> {
                android.util.Log.d("CheckoutScreen", "Checkout success! TransactionId: ${state.checkoutResponse.data.transactionId}")
                val paymentInfo = state.checkoutResponse.data.paymentInfo
                val paymentDetail = state.checkoutResponse.data.paymentDetail
                
                // Check if this is ZaloPay payment (has payUrl to open)
                val hasZaloPayPayment = (paymentInfo?.payUrl != null) || (paymentDetail?.payUrl != null)
                android.util.Log.d("CheckoutScreen", "Has ZaloPay payment: $hasZaloPayPayment")
                
                if (hasZaloPayPayment) {
                    // ZaloPay payment: Open payment URL (App to App)
                    val payUrl = paymentInfo?.payUrl ?: paymentDetail?.payUrl
                    val deeplink = paymentInfo?.deeplink
                    
                    android.util.Log.d("CheckoutScreen", "Opening ZaloPay payment - deeplink: $deeplink, payUrl: $payUrl")
                    
                    try {
                        // Prefer deeplink first, fallback to payUrl
                        val urlToOpen = deeplink ?: payUrl
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen))
                        intent.setPackage("com.vng.zalopay") // Force open ZaloPay app
                        context.startActivity(intent)
                        android.util.Log.d("CheckoutScreen", "Successfully opened ZaloPay app")
                    } catch (e: Exception) {
                        // If ZaloPay app not installed, fallback to browser
                        android.util.Log.e("CheckoutScreen", "ZaloPay app not found, opening in browser", e)
                        try {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                            context.startActivity(webIntent)
                        } catch (e2: Exception) {
                            android.util.Log.e("CheckoutScreen", "Failed to open payment", e2)
                        }
                    }
                    // Don't navigate to success yet - user needs to complete payment
                } else {
                    // Wallet payment: Transaction completed (already polled by ViewModel)
                    android.util.Log.d("CheckoutScreen", "Wallet payment completed - navigating to success")
                    onCheckoutSuccess(
                        state.checkoutResponse.data.transactionId,
                        listingName,
                        listingPrice,
                        uiState.selectedPaymentMethod.displayName
                    )
                }
            }
            is CheckoutState.ProcessingTransaction -> {
                android.util.Log.d("CheckoutScreen", "Processing transaction: ${state.transactionId}")
            }
            is CheckoutState.Loading -> {
                android.util.Log.d("CheckoutScreen", "Checkout loading...")
            }
            is CheckoutState.Error -> {
                android.util.Log.e("CheckoutScreen", "Checkout error: ${state.message}")
            }
            else -> {}
        }
    }
    
    // Show insufficient balance dialog
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.checkoutState) {
        if (uiState.checkoutState is CheckoutState.Error && 
            (uiState.checkoutState as CheckoutState.Error).isInsufficientBalance) {
            showInsufficientBalanceDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Product Info Card
            ProductInfoSection(
                name = listingName,
                price = listingPrice,
                imageUrl = listingImage,
                type = listingType
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Method Selection
            PaymentMethodSection(
                selectedMethod = uiState.selectedPaymentMethod,
                walletBalance = uiState.walletBalance,
                isLoadingBalance = uiState.isLoadingWallet,
                productPrice = listingPrice,
                onMethodSelected = { checkoutViewModel.selectPaymentMethod(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Price Summary
            PriceSummarySection(price = listingPrice)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Message
            if (uiState.checkoutState is CheckoutState.Error) {
                val error = uiState.checkoutState as CheckoutState.Error
                if (!error.isInsufficientBalance) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error.message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Checkout Button
            CheckoutButton(
                enabled = checkoutViewModel.canProceedCheckout(),
                isLoading = uiState.checkoutState is CheckoutState.Loading,
                walletBalance = uiState.walletBalance,
                productPrice = listingPrice,
                paymentMethod = uiState.selectedPaymentMethod,
                onClick = { checkoutViewModel.processCheckout() }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Insufficient Balance Dialog
    if (showInsufficientBalanceDialog) {
        InsufficientBalanceDialog(
            currentBalance = uiState.walletBalance,
            requiredAmount = listingPrice,
            onDismiss = {
                showInsufficientBalanceDialog = false
                checkoutViewModel.resetCheckoutState()
            },
            onNavigateToWallet = {
                showInsufficientBalanceDialog = false
                onNavigateToWallet()
            }
        )
    }
    
    // Processing Payment Overlay (for WALLET payment)
    if (uiState.checkoutState is CheckoutState.ProcessingTransaction) {
        val processingState = uiState.checkoutState as CheckoutState.ProcessingTransaction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Đang xử lý giao dịch...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vui lòng chờ trong giây lát",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transaction: ${processingState.transactionId.take(8)}...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ProductInfoSection(
    name: String,
    price: Int,
    imageUrl: String?,
    type: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Image
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (type == "BATTERY") "Pin" else "Xe điện",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatPrice(price),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSection(
    selectedMethod: PaymentMethod,
    walletBalance: Int,
    isLoadingBalance: Boolean,
    productPrice: Int,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phương thức thanh toán",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wallet Payment Option
            PaymentOption(
                icon = Icons.Default.AccountBalanceWallet,
                name = PaymentMethod.WALLET.displayName,
                description = if (isLoadingBalance) {
                    "Đang tải..."
                } else {
                    "Số dư: ${formatPrice(walletBalance)}"
                },
                isSelected = selectedMethod == PaymentMethod.WALLET,
                isEnabled = !isLoadingBalance,
                showWarning = walletBalance < productPrice && !isLoadingBalance,
                warningText = "Số dư không đủ",
                onClick = { onMethodSelected(PaymentMethod.WALLET) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ZaloPay Payment Option
            PaymentOption(
                icon = Icons.Default.Payment,
                name = PaymentMethod.ZALOPAY.displayName,
                description = "Thanh toán qua ví điện tử ZaloPay",
                isSelected = selectedMethod == PaymentMethod.ZALOPAY,
                isEnabled = true,
                onClick = { onMethodSelected(PaymentMethod.ZALOPAY) }
            )
        }
    }
}

@Composable
fun PaymentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    showWarning: Boolean = false,
    warningText: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
            .border(
                width = 2.dp,
                color = if (isSelected) PrimaryGreen else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.Black else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = if (showWarning) MaterialTheme.colorScheme.error else Color.Gray
                )
                if (showWarning) {
                    Text(
                        text = warningText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                enabled = isEnabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PrimaryGreen
                )
            )
        }
    }
}

@Composable
fun PriceSummarySection(price: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chi tiết thanh toán",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Giá sản phẩm", color = Color.Gray)
                Text(formatPrice(price))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tổng thanh toán",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatPrice(price),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun CheckoutButton(
    enabled: Boolean,
    isLoading: Boolean,
    walletBalance: Int,
    productPrice: Int,
    paymentMethod: PaymentMethod,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Show warning if wallet balance insufficient
        if (paymentMethod == PaymentMethod.WALLET && walletBalance < productPrice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bạn cần thêm ${formatPrice(productPrice - walletBalance)} để thanh toán",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = if (enabled) "Thanh toán ngay" else "Không đủ số dư",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InsufficientBalanceDialog(
    currentBalance: Int,
    requiredAmount: Int,
    onDismiss: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Số dư không đủ",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Số dư hiện tại: ${formatPrice(currentBalance)}",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Số tiền cần thanh toán: ${formatPrice(requiredAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn cần nạp thêm: ${formatPrice(requiredAmount - currentBalance)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Vui lòng nạp tiền vào ví để tiếp tục thanh toán.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onNavigateToWallet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                )
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nạp tiền")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(price).replace("₫", "đ")
}
