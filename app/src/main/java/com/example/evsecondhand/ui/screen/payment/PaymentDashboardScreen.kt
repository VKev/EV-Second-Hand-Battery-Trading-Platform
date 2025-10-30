package com.example.evsecondhand.ui.screen.payment

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.evsecondhand.R
import com.example.evsecondhand.data.model.CheckoutPaymentInfo
import com.example.evsecondhand.ui.viewmodel.CheckoutPaymentMethod
import com.example.evsecondhand.ui.viewmodel.CheckoutProductSummary
import com.example.evsecondhand.ui.viewmodel.PaymentUiState
import com.example.evsecondhand.ui.viewmodel.PaymentViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PaymentDashboardScreen(
    viewModel: PaymentViewModel,
    productType: String? = null,
    productId: String? = null,
    onBackClick: () -> Unit = {},
    onPaymentSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(productType, productId) {
        viewModel.loadCheckoutProduct(productType, productId)
    }

    LaunchedEffect(uiState.checkoutSuccessMessage, uiState.navigateToHome) {
        val message = uiState.checkoutSuccessMessage
        val shouldNavigateHome = uiState.navigateToHome

        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        if (shouldNavigateHome) {
            onPaymentSuccess()
        }

        if (message != null || shouldNavigateHome) {
            viewModel.clearCheckoutSuccessMessage()
        }
    }

    PaymentDashboardContent(
        state = uiState,
        onRefresh = viewModel::refresh,
        onCheckout = viewModel::initiateCheckout,
        onConfirmMomoPayment = viewModel::confirmMomoPayment,
        onDismissMomoPayment = viewModel::clearPendingMomoPayment,
        onBackClick = onBackClick
    )
}
val PrimaryGreen = Color(0xFF00C853)
val AccentBlue = Color(0xFF2196F3)
val BackgroundLight = Color(0xFFF5F5F5)

// Supporting Colors
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
val ErrorRed = Color(0xFFD32F2F)

// Gradient variations
val PrimaryGreenLight = Color(0xFF69F0AE)
val AccentBlueLight = Color(0xFF64B5F6)
@Composable
private fun PaymentDashboardContent(
    state: PaymentUiState,
    onRefresh: () -> Unit,
    onCheckout: (CheckoutPaymentMethod) -> Unit,
    onConfirmMomoPayment: (String) -> Unit,
    onDismissMomoPayment: () -> Unit,
    onBackClick: () -> Unit
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isLoading)
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.WALLET) }
    var fullName by remember { mutableStateOf("Nguyễn Văn A") }
    var email by remember { mutableStateOf("example@email.com") }
    var phoneNumber by remember { mutableStateOf("0123 456 789") }
    var billingAddress by remember { mutableStateOf("123 Đường ABC, Quận 1, TP.HCM") }
    var agreedToTerms by remember { mutableStateOf(false) }

    val availableBalance = state.balance?.availableBalance?.toLong() ?: 0L
    val checkoutProduct = state.checkoutProduct
    val payableAmount = checkoutProduct?.let { it.price + PLATFORM_FEE } ?: 0L
    val productReady = checkoutProduct != null && !state.isProductLoading && state.productError == null

    val animatedGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9),  // Xanh lá nhạt
            Color(0xFFF1F8E9),  // Xanh lá rất nhạt
            Color(0xFFF5F5F5)   // Background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedGradient)
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh,
            indicator = { refreshState, trigger ->
                SwipeRefreshIndicator(
                    state = refreshState,
                    refreshTriggerDistance = trigger,
                    backgroundColor = Color.White,
                    contentColor = AccentBlue,
                    scale = true
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                CheckoutHeader(
                    onBackClick = onBackClick,
                )

                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    state.errorMessage?.let { error ->
                        ErrorBanner(message = error, onRetry = onRefresh)
                    }
                }

                EnhancedOrderSummaryCard(
                    product = checkoutProduct,
                    availableBalance = availableBalance,
                    lastUpdated = state.balance?.updatedAt,
                    isProductLoading = state.isProductLoading,
                    productError = state.productError
                )

                EnhancedPaymentInformationSection(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    email = email,
                    onEmailChange = { email = it },
                    phone = phoneNumber,
                    onPhoneChange = { phoneNumber = it },
                    address = billingAddress,
                    onAddressChange = { billingAddress = it }
                )

                EnhancedPaymentMethodSection(
                    selectedMethod = selectedPaymentMethod,
                    onMethodSelect = { selectedPaymentMethod = it },
                    availableBalance = availableBalance,
                    walletReference = state.balance?.id
                )

                EnhancedPaymentSupportStrip()

                EnhancedCheckoutActionSection(
                    agreedToTerms = agreedToTerms,
                    onAgreementChange = { agreedToTerms = it },
                    payableAmount = payableAmount,
                    availableBalance = availableBalance,
                    selectedMethod = selectedPaymentMethod,
                    isProcessing = state.isCheckoutProcessing,
                    productReady = productReady,
                    onCompletePayment = { method ->
                        val checkoutMethod = when (method) {
                            PaymentMethod.WALLET -> CheckoutPaymentMethod.WALLET
                            PaymentMethod.MOMO -> CheckoutPaymentMethod.MOMO
                        }
                        onCheckout(checkoutMethod)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (state.isCheckoutProcessing && state.pendingMomoPayment == null) {
            ProcessingOverlay()
        }

        state.pendingMomoPayment?.let { pending ->
            val paymentInfo = pending.paymentInfo
            val clipboardManager = LocalClipboardManager.current

            AlertDialog(
                onDismissRequest = {
                    if (!state.isCheckoutProcessing) {
                        onDismissMomoPayment()
                    }
                },
                title = {
                    Text(
                        text = "Hoàn tất thanh toán MoMo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Quét mã QR MoMo bên dưới để thanh toán, sau đó quay lại và xác nhận.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        paymentInfo.amount?.takeIf { it > 0L }?.let { amount ->
                            Text(
                                text = "Số tiền: ${formatCurrency(amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "Mã giao dịch: ${pending.transactionId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        MoMoQrDisplay(
                            paymentInfo = paymentInfo,
                            onCopyLink = { link ->
                                clipboardManager.setText(AnnotatedString(link))
                                Toast.makeText(
                                    context,
                                    "Đã sao chép liên kết thanh toán.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirmMomoPayment(pending.transactionId) },
                        enabled = !state.isCheckoutProcessing
                    ) {
                        if (state.isCheckoutProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryGreen
                            )
                        } else {
                            Text(
                                text = "Tôi đã thanh toán",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissMomoPayment,
                        enabled = !state.isCheckoutProcessing
                    ) {
                        Text(
                            text = "Để sau",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }

        if (state.isLoading && state.balance == null && state.history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    color = PrimaryGreen,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Đang xử lý thanh toán...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9).copy(alpha = 0.3f + gradientOffset * 0.2f),
            Color(0xFFF1F8E9),
            Color(0xFFF5F5F5)
        )
    )

    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = PrimaryGreen,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(48.dp)
            )
        }
    }
}




@Composable
private fun CheckoutHeader(
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = TextPrimary
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Thanh toán",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Hoàn tất đơn hàng của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EnhancedOrderSummaryCard(
    product: CheckoutProductSummary?,
    availableBalance: Long,
    lastUpdated: String?,
    isProductLoading: Boolean,
    productError: String?
) {
    val basePrice = product?.price ?: 0L
    val platformFee = if (product != null) PLATFORM_FEE else 0L
    val totalPayable = basePrice + platformFee

    EnhancedPaymentSectionCard(
        title = "Tóm tắt đơn hàng",
        subtitle = "Kiểm tra lại thông tin xe",
        icon = Icons.Filled.Receipt
    ) {
        when {
            isProductLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            product != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF8FBFF),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryGreen, AccentBlue)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = product.brand.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = product.variant,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            product.specs.take(2).forEach { spec ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(PrimaryGreen, CircleShape)
                                    )
                                    Text(
                                        text = spec,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            productError != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ErrorRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Không thể tải thông tin đơn hàng",
                            style = MaterialTheme.typography.titleMedium,
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = productError,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }
                }
            }

            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F8F8)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Chưa có sản phẩm nào được chọn.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Hãy chọn một pin hoặc xe để tiếp tục thanh toán.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (product != null) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE8E8E8)
            )
            EnhancedSummaryRow(label = "Giá sản phẩm", value = formatCurrency(basePrice))
            EnhancedSummaryRow(label = "Phí nền tảng", value = formatCurrency(platformFee))
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE8E8E8)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryGreen.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng thanh toán",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(totalPayable),
                        style = MaterialTheme.typography.headlineSmall,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentBlue.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Số dư ví khả dụng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = formatCurrency(availableBalance),
                style = MaterialTheme.typography.titleMedium,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Cập nhật: ${formatDateTime(lastUpdated)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EnhancedSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EnhancedPaymentSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    icon?.let {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = AccentBlue.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                trailingContent?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun EnhancedPaymentInformationSection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit
) {
    EnhancedPaymentSectionCard(
        title = "Thông tin thanh toán",
        subtitle = "Thông tin để liên hệ và xuất hoá đơn",
        icon = Icons.Filled.Person
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EnhancedPaymentTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Họ và tên",
                leadingIcon = Icons.Filled.Person,
                modifier = Modifier.weight(1f)
            )
            EnhancedPaymentTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Số điện thoại",
                leadingIcon = Icons.Filled.Phone,
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Phone
            )
        }
        EnhancedPaymentTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email
        )
        EnhancedPaymentTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "Địa chỉ thanh toán",
            leadingIcon = Icons.Filled.Home
        )
    }
}

@Composable
private fun EnhancedPaymentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,

) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = Color(0xFFE8E8E8)
        )
    )
}

@Composable
private fun EnhancedPaymentMethodSection(
    selectedMethod: PaymentMethod,
    onMethodSelect: (PaymentMethod) -> Unit,
    availableBalance: Long,
    walletReference: String?
) {
    EnhancedPaymentSectionCard(
        title = "Phương thức thanh toán",
        subtitle = "Chọn kênh thanh toán an toàn",
        icon = Icons.Filled.CreditCard,
        trailingContent = {
            SecurityBadge()
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PaymentMethod.values().forEach { method ->
                EnhancedPaymentMethodChip(
                    method = method,
                    selected = method == selectedMethod,
                    onSelect = onMethodSelect,
                    availableBalance = if (method == PaymentMethod.WALLET) availableBalance else null
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            when (selectedMethod) {
                PaymentMethod.WALLET -> EnhancedWalletDetails(availableBalance)
                PaymentMethod.MOMO -> EnhancedMomoDetails()
            }
        }
    }
}

@Composable
private fun SecurityBadge() {
    Surface(
        color = PrimaryGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "3D Secure",
                color = PrimaryGreen,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedPaymentMethodChip(
    method: PaymentMethod,
    selected: Boolean,
    onSelect: (PaymentMethod) -> Unit,
    availableBalance: Long? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onSelect(method) },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            2.dp,
            if (selected) PrimaryGreen else Color(0xFFE8E8E8)
        ),
        color = if (selected) PrimaryGreen.copy(alpha = 0.06f) else Color.White,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = if (selected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGreen,
                                    AccentBlue
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF0F0F0),
                                    Color(0xFFE8E8E8)
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.label,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (method == PaymentMethod.WALLET && availableBalance != null) {
                        "Số dư: ${formatCurrency(availableBalance)}"
                    } else {
                        method.description
                    },
                    color = if (method == PaymentMethod.WALLET && availableBalance != null) {
                        PrimaryGreen
                    } else {
                        TextSecondary
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (method == PaymentMethod.WALLET && availableBalance != null) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedWalletDetails(availableBalance: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Wallet Balance Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryGreen,
                                PrimaryGreen.copy(alpha = 0.8f),
                                AccentBlue.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EV Market Wallet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Số dư khả dụng",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatCurrency(availableBalance),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Bảo mật 256-bit",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (availableBalance > 0L) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (availableBalance > 0L) "Đủ số dư" else "Không đủ",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Wallet Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AccentBlue.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = AccentBlue
                )
                Text(
                    text = "Thanh toán bằng ví EV Market nhanh chóng và bảo mật. Số tiền sẽ được trừ trực tiếp từ số dư ví của bạn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        // Wallet Features
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WalletFeatureRow(
                icon = Icons.Filled.Speed,
                title = "Thanh toán tức thì",
                description = "Giao dịch được xử lý ngay lập tức"
            )
            WalletFeatureRow(
                icon = Icons.Filled.Security,
                title = "Bảo mật cao",
                description = "Mã hóa 256-bit & xác thực 2 lớp"
            )
            WalletFeatureRow(
                icon = Icons.Filled.AttachMoney,
                title = "Không phí giao dịch",
                description = "Miễn phí 100% khi thanh toán bằng ví"
            )
        }
    }
}

@Composable
private fun WalletFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    PrimaryGreen.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EnhancedMomoDetails() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AccentBlue.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = AccentBlue
                )
                Column {
                    Text(
                        text = "EV Market sẽ tạo mã QR MoMo để bạn quét và thanh toán an toàn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sau khi quét xong, quay lại ứng dụng và xác nhận hoàn tất thanh toán.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        EWalletOptionWithLogo(
            name = "MoMo",
            description = "Ví điện tử MoMo",
            logoRes = R.drawable.momo_logo,
            backgroundColor = Color(0xFFB0006D)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Kiểm tra lại số tiền và nội dung giao dịch trong MoMo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Đảm bảo thông tin chính xác trước khi xác nhận.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Nhấn \"Tôi đã thanh toán\" sau khi giao dịch thành công",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hệ thống sẽ xác nhận và cập nhật trạng thái đơn hàng của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Cần hỗ trợ?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Liên hệ EV Market qua hotline 1900 2233 để được trợ giúp kịp thời.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EWalletOptionWithLogo(
    name: String,
    description: String,
    @DrawableRes logoRes: Int,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE8E8E8)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        backgroundColor.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "$name logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            // Thông tin ví
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Arrow icon với màu brand
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        backgroundColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedBankTransferDetails(walletReference: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AccentBlue.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = AccentBlue
                )
                Text(
                    text = "Chuyển khoản theo thông tin bên dưới. Đơn hàng sẽ tự động kích hoạt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        EnhancedBankInfoRow(
            label = "Người nhận",
            value = "EV Market JSC",
            icon = Icons.Filled.Business
        )
        EnhancedBankInfoRow(
            label = "Ngân hàng",
            value = "Vietcombank - CN TP.HCM",
            icon = Icons.Filled.AccountBalance
        )
        EnhancedBankInfoRow(
            label = "Số tài khoản",
            value = "0123 456 789",
            icon = Icons.Filled.CreditCard
        )
        EnhancedBankInfoRow(
            label = "Nội dung",
            value = walletReference?.uppercase(Locale.getDefault()) ?: "EV-CHECKOUT",
            icon = Icons.Filled.Description,
            highlight = true
        )
    }
}

@Composable
private fun EnhancedBankInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    highlight: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (highlight) PrimaryGreen.copy(alpha = 0.08f) else Color.White,
        border = BorderStroke(1.dp, if (highlight) PrimaryGreen else Color(0xFFE8E8E8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (highlight) PrimaryGreen.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (highlight) PrimaryGreen else AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = value,
                    color = if (highlight) PrimaryGreen else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun EnhancedPaymentSupportStrip() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = 0.1f),
                            PrimaryGreen.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AccentBlue,
                                        PrimaryGreen
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SupportAgent,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "EV Market Care 24/7",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hỗ trợ mọi lúc trong quá trình thanh toán",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "1900 2233",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedCheckoutActionSection(
    agreedToTerms: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    payableAmount: Long,
    availableBalance: Long,
    selectedMethod: PaymentMethod,
    isProcessing: Boolean,
    productReady: Boolean,
    onCompletePayment: (PaymentMethod) -> Unit
) {
    val hasEnoughBalance = availableBalance >= payableAmount && payableAmount > 0L
    val baseReady = agreedToTerms && productReady && payableAmount > 0L
    val buttonEnabled = when (selectedMethod) {
        PaymentMethod.WALLET -> baseReady && hasEnoughBalance
        PaymentMethod.MOMO -> baseReady
    }
    val buttonLabel = when (selectedMethod) {
        PaymentMethod.WALLET -> "Thanh toán bằng Ví EV Market • ${formatCurrency(payableAmount)}"
        PaymentMethod.MOMO -> "Thanh toán qua MoMo • ${formatCurrency(payableAmount)}"
    }

    EnhancedPaymentSectionCard(
        title = "Xác nhận thanh toán",
        subtitle = "Hoàn tất bước cuối cùng",
        icon = Icons.Filled.Verified
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8F8F8)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = onAgreementChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryGreen,
                        uncheckedColor = TextSecondary
                    )
                )
                Text(
                    text = "Tôi đồng ý với Điều khoản & Điều kiện của EV Market",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentBlue.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedMethod == PaymentMethod.WALLET) {
                Text(
                    text = "Số dư ví hiện tại",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(availableBalance),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (availableBalance > 0L) PrimaryGreen else ErrorRed,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Text(
                    text = "Tổng thanh toán",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(payableAmount),
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        PulsingPaymentButton(
            text = buttonLabel,
            enabled = buttonEnabled,
            loading = isProcessing,
            onClick = { onCompletePayment(selectedMethod) }
        )

        if (!productReady) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Thông tin sản phẩm đang được cập nhật. Vui lòng đợi trong giây lát.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else if (selectedMethod == PaymentMethod.WALLET && availableBalance < payableAmount) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ErrorRed.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Số dư ví không đủ để hoàn tất thanh toán",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
                }
            }
        } else if (selectedMethod == PaymentMethod.MOMO) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = AccentBlue.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhoneIphone,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Mã QR MoMo sẽ được hiển thị để bạn quét và thanh toán.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MoMoQrDisplay(
    paymentInfo: CheckoutPaymentInfo,
    onCopyLink: (String) -> Unit
) {
    val context = LocalContext.current
    val qrPainter = remember(paymentInfo.qrCodeUrl) {
        decodeBase64QrPainter(paymentInfo.qrCodeUrl)
    }
    val networkQrUrl = remember(paymentInfo.qrCodeUrl) {
        paymentInfo.qrCodeUrl
            ?.takeIf { it.startsWith("http", ignoreCase = true) }
    }
    val fallbackLink = remember(paymentInfo.payUrl, paymentInfo.deeplink, paymentInfo.deeplinkMiniApp) {
        paymentInfo.payUrl?.takeIf { it.isNotBlank() }
            ?: paymentInfo.deeplink?.takeIf { it.isNotBlank() }
            ?: paymentInfo.deeplinkMiniApp?.takeIf { it.isNotBlank() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quét mã bằng ứng dụng MoMo",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        when {
            qrPainter != null -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
                    shadowElevation = 4.dp
                ) {
                    Image(
                        painter = qrPainter,
                        contentDescription = "Mã QR MoMo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                }
            }
            networkQrUrl != null -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(networkQrUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Mã QR MoMo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlue.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Không tìm thấy mã QR. Vui lòng sử dụng liên kết thanh toán dự phòng bên dưới.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        fallbackLink?.let { link ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liên kết thanh toán dự phòng",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        SelectionContainer {
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentBlue
                            )
                        }
                    }
                    TextButton(onClick = { onCopyLink(link) }) {
                        Text(
                            text = "Sao chép",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun decodeBase64QrPainter(data: String?): BitmapPainter? {
    if (data.isNullOrBlank()) return null
    val base64Part = when {
        data.startsWith("data:image", ignoreCase = true) -> data.substringAfter("base64,", "")
        else -> return null
    }
    if (base64Part.isBlank()) return null
    return runCatching {
        val decoded = Base64.decode(base64Part, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        bitmap?.let { BitmapPainter(it.asImageBitmap()) }
    }.getOrNull()
}


@Composable
private fun PulsingPaymentButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (enabled) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(if (enabled) scale else 1f)
            .shadow(if (enabled) 8.dp else 2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryGreen,
                                AccentBlue
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF81C784),
                                Color(0xFF66BB6A)
                            )
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(2.dp, ErrorRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ErrorRed.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Đã xảy ra lỗi",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ErrorRed
                )
            ) {
                Text(
                    text = "Thử lại",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val currencyFormatter by lazy {
    DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale("vi", "VN")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
    )
}

private val dateFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
}

fun formatCurrency(amount: Long): String = "${currencyFormatter.format(amount)} ₫"

fun formatDateTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "--:--"
    return runCatching {
        val instant = Instant.parse(timestamp)
        dateFormatter.format(instant)
    }.getOrElse { "--:--" }
}

private enum class PaymentMethod(
    val label: String,
    val description: String,
    val icon: ImageVector
) {
    WALLET("Ví EV Market", "Sử dụng số dư ví của bạn", Icons.Filled.AccountBalanceWallet),
    MOMO("MoMo", "Thanh toán qua ví MoMo", Icons.Filled.PhoneIphone),
}

private const val PLATFORM_FEE = 20_000L
