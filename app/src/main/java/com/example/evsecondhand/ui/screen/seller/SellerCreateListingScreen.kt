package com.example.evsecondhand.ui.screen.seller

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.BatteryAndCharging
import com.example.evsecondhand.data.model.Dimensions
import com.example.evsecondhand.data.model.Performance
import com.example.evsecondhand.data.model.seller.BatterySpecifications
import com.example.evsecondhand.data.model.seller.CreateBatteryRequest
import com.example.evsecondhand.data.model.seller.CreateVehicleRequest
import com.example.evsecondhand.data.model.VehicleSpecifications
import com.example.evsecondhand.data.model.Warranty
import com.example.evsecondhand.ui.theme.*
import com.example.evsecondhand.ui.viewmodel.SellerCreateListingViewModel
import com.example.evsecondhand.ui.viewmodel.SellerCreateUiState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SellerCreateListingScreen(
    viewModel: SellerCreateListingViewModel,
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
            onNavigateToDashboard()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var activeTab by rememberSaveable { mutableStateOf(CreateListingTab.Vehicle) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        AnimatedGradientBackground()

        Scaffold(
            topBar = {
                EnhancedCreateTopBar(
                    activeTab = activeTab,
                    onTabChange = { activeTab = it },
                    onBackClick = onBackClick
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) + slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { it / 4 }
                        ) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        CreateListingTab.Vehicle -> VehicleForm(viewModel, uiState)
                        CreateListingTab.Battery -> BatteryForm(viewModel, uiState)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4FF).copy(alpha = 0.4f + offset * 0.2f),
                        Color(0xFFF0F9FF),
                        Color(0xFFFAFAFA)
                    )
                )
            )
    )
}

@Composable
private fun EnhancedCreateTopBar(
    activeTab: CreateListingTab,
    onTabChange: (CreateListingTab) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tạo tin mới",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryGreen, CircleShape)
                        )
                    }
                    Text(
                        text = "Chọn đăng xe hoặc pin để bắt đầu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Enhanced Tab Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CreateListingTab.values().forEach { tab ->
                        EnhancedTab(
                            tab = tab,
                            isSelected = tab == activeTab,
                            onClick = { onTabChange(tab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedTab(
    tab: CreateListingTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryGreen.copy(alpha = 0.08f),
                                PrimaryGreen.copy(alpha = 0.05f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tab == CreateListingTab.Vehicle)
                        Icons.Default.DirectionsCar
                    else
                        Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = tab.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryGreen else TextSecondary,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun VehicleForm(
    viewModel: SellerCreateListingViewModel,
    uiState: SellerCreateUiState
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }
    var isAuction by rememberSaveable { mutableStateOf(false) }
    var auctionEndsAt by rememberSaveable { mutableStateOf("") }
    var startingPrice by rememberSaveable { mutableStateOf("") }
    var bidIncrement by rememberSaveable { mutableStateOf("") }
    var depositAmount by rememberSaveable { mutableStateOf("") }
    var auctionEndsAtDisplay by rememberSaveable { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf(emptyList<Uri>()) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    // Specifications - Warranty
    var warrantyBasic by rememberSaveable { mutableStateOf("") }
    var warrantyBattery by rememberSaveable { mutableStateOf("") }
    var warrantyDrivetrain by rememberSaveable { mutableStateOf("") }

    // Specifications - Dimensions
    var dimWidth by rememberSaveable { mutableStateOf("") }
    var dimHeight by rememberSaveable { mutableStateOf("") }
    var dimLength by rememberSaveable { mutableStateOf("") }
    var dimCurbWeight by rememberSaveable { mutableStateOf("") }

    // Specifications - Performance
    var perfTopSpeed by rememberSaveable { mutableStateOf("") }
    var perfMotorType by rememberSaveable { mutableStateOf("") }
    var perfHorsepower by rememberSaveable { mutableStateOf("") }
    var perfAcceleration by rememberSaveable { mutableStateOf("") }

    // Specifications - Battery & Charging
    var battRange by rememberSaveable { mutableStateOf("") }
    var battChargeTime by rememberSaveable { mutableStateOf("") }
    var battChargingSpeed by rememberSaveable { mutableStateOf("") }
    var battCapacity by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val zoneId = remember { ZoneId.systemDefault() }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        EnhancedListingCard(
            title = "Thông tin xe",
            icon = Icons.Default.DirectionsCar,
            description = "Điền đầy đủ thông tin về phương tiện"
        ) {
            EnhancedListingTextField(
                value = title,
                onValueChange = { title = it },
                label = "Tiêu đề tin đăng",
                icon = Icons.Default.Title,
                placeholder = "VD: Tesla Model 3 2023 như mới"
            )

            EnhancedListingTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả chi tiết",
                icon = Icons.Default.Description,
                placeholder = "Mô tả tình trạng, đặc điểm nổi bật...",
                minLines = 3
            )

            EnhancedListingTextField(
                value = price,
                onValueChange = { price = digitsOnly(it) },
                label = "Giá bán (₫)",
                icon = Icons.Default.AttachMoney,
                placeholder = "500000000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = "Thương hiệu",
                    icon = Icons.Default.Business,
                    placeholder = "Tesla, VinFast...",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = "Dòng xe",
                    icon = Icons.Default.CarRental,
                    placeholder = "Model 3, VF8...",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = year,
                    onValueChange = { year = digitsOnly(it, 4) },
                    label = "Năm sản xuất",
                    icon = Icons.Default.CalendarToday,
                    placeholder = "2023",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                EnhancedListingTextField(
                    value = mileage,
                    onValueChange = { mileage = digitsOnly(it) },
                    label = "Số km đã đi",
                    icon = Icons.Default.Speed,
                    placeholder = "15000",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        // Specifications Section - Warranty
        EnhancedListingCard(
            title = "Bảo hành",
            icon = Icons.Default.Shield,
            description = "Thông tin bảo hành (tùy chọn)"
        ) {
            EnhancedListingTextField(
                value = warrantyBasic,
                onValueChange = { warrantyBasic = it },
                label = "Bảo hành cơ bản",
                icon = Icons.Default.VerifiedUser,
                placeholder = "VD: 3 năm hoặc 100,000 km"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = warrantyBattery,
                    onValueChange = { warrantyBattery = it },
                    label = "Bảo hành pin",
                    icon = Icons.Default.BatteryFull,
                    placeholder = "VD: 8 năm",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = warrantyDrivetrain,
                    onValueChange = { warrantyDrivetrain = it },
                    label = "Bảo hành động cơ",
                    icon = Icons.Default.Engineering,
                    placeholder = "VD: 5 năm",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Specifications Section - Dimensions
        EnhancedListingCard(
            title = "Kích thước & Trọng lượng",
            icon = Icons.Default.Straighten,
            description = "Thông số kích thước (tùy chọn)"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = dimWidth,
                    onValueChange = { dimWidth = it },
                    label = "Chiều rộng",
                    icon = Icons.Default.SwapHoriz,
                    placeholder = "VD: 1850 mm",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = dimHeight,
                    onValueChange = { dimHeight = it },
                    label = "Chiều cao",
                    icon = Icons.Default.Height,
                    placeholder = "VD: 1445 mm",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = dimLength,
                    onValueChange = { dimLength = it },
                    label = "Chiều dài",
                    icon = Icons.Default.LinearScale,
                    placeholder = "VD: 4694 mm",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = dimCurbWeight,
                    onValueChange = { dimCurbWeight = it },
                    label = "Trọng lượng",
                    icon = Icons.Default.FitnessCenter,
                    placeholder = "VD: 1611 kg",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Specifications Section - Performance
        EnhancedListingCard(
            title = "Hiệu suất",
            icon = Icons.Default.Speed,
            description = "Thông số vận hành (tùy chọn)"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = perfTopSpeed,
                    onValueChange = { perfTopSpeed = it },
                    label = "Tốc độ tối đa",
                    icon = Icons.Default.FlashOn,
                    placeholder = "VD: 225 km/h",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = perfAcceleration,
                    onValueChange = { perfAcceleration = it },
                    label = "Tăng tốc 0-100",
                    icon = Icons.Default.RocketLaunch,
                    placeholder = "VD: 5.6 giây",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = perfMotorType,
                    onValueChange = { perfMotorType = it },
                    label = "Loại động cơ",
                    icon = Icons.Default.SettingsSuggest,
                    placeholder = "VD: Dual Motor AWD",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = perfHorsepower,
                    onValueChange = { perfHorsepower = it },
                    label = "Công suất",
                    icon = Icons.Default.Bolt,
                    placeholder = "VD: 283 HP",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Specifications Section - Battery & Charging
        EnhancedListingCard(
            title = "Pin & Sạc",
            icon = Icons.Default.BatteryChargingFull,
            description = "Thông số pin và sạc (tùy chọn)"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = battCapacity,
                    onValueChange = { battCapacity = it },
                    label = "Dung lượng pin",
                    icon = Icons.Default.Battery6Bar,
                    placeholder = "VD: 60 kWh",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = battRange,
                    onValueChange = { battRange = it },
                    label = "Phạm vi hoạt động",
                    icon = Icons.Default.MyLocation,
                    placeholder = "VD: 510 km",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = battChargeTime,
                    onValueChange = { battChargeTime = it },
                    label = "Thời gian sạc",
                    icon = Icons.Default.Timer,
                    placeholder = "VD: 8-10 giờ (AC)",
                    modifier = Modifier.weight(1f)
                )
                EnhancedListingTextField(
                    value = battChargingSpeed,
                    onValueChange = { battChargingSpeed = it },
                    label = "Tốc độ sạc nhanh",
                    icon = Icons.Default.ElectricBolt,
                    placeholder = "VD: 250 kW DC",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        EnhancedListingCard(
            title = "Tuỳ chọn đấu giá",
            icon = Icons.Default.Gavel,
            description = "Cho phép người mua đặt giá thầu"
        ) {
            EnhancedAuctionToggle(
                isAuction = isAuction,
                onToggle = {
                    isAuction = it
                    if (!it) {
                        auctionEndsAt = ""
                        auctionEndsAtDisplay = ""
                        startingPrice = ""
                        bidIncrement = ""
                        depositAmount = ""
                    }
                }
            )

            AnimatedVisibility(
                visible = isAuction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    EnhancedListingTextField(
                        value = startingPrice,
                        onValueChange = { startingPrice = digitsOnly(it) },
                        label = "Giá khởi điểm",
                        icon = Icons.Default.MonetizationOn,
                        placeholder = "100000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EnhancedListingTextField(
                            value = bidIncrement,
                            onValueChange = { bidIncrement = digitsOnly(it) },
                            label = "Bước giá",
                            icon = Icons.Default.TrendingUp,
                            placeholder = "5000000",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        EnhancedListingTextField(
                            value = depositAmount,
                            onValueChange = { depositAmount = digitsOnly(it) },
                            label = "Đặt cọc",
                            icon = Icons.Default.AccountBalance,
                            placeholder = "10000000",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }

        EnhancedImagePickerSection(
            title = "Ảnh phương tiện",
            description = "Tối đa 5 ảnh, định dạng JPG/PNG",
            selectedImages = selectedImages,
            onPickImages = {
                pickImagesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        AnimatedVisibility(
            visible = localError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            localError?.let { ErrorMessage(it) }
        }

        EnhancedSubmitButton(
            text = if (uiState.isSubmitting) "Đang đăng tải..." else "Đăng tin xe",
            isLoading = uiState.isSubmitting,
            onClick = {
                val validationError = when {
                    title.isBlank() || description.isBlank() || price.isBlank() ||
                            brand.isBlank() || model.isBlank() || year.isBlank() ||
                            mileage.isBlank() -> "Vui lòng nhập đầy đủ thông tin bắt buộc."
                    selectedImages.isEmpty() -> "Vui lòng chọn ít nhất 1 ảnh."
                    year.toIntOrNull() == null || mileage.toLongOrNull() == null -> "Giá trị số không hợp lệ."
                    price.toLongOrNull()?.let { it < MIN_CURRENCY_VALUE } != false -> "Giá bán tối thiểu 1.000₫."
                    isAuction && auctionEndsAt.isBlank() ->
                        "Vui lòng chọn thời gian kết thúc đấu giá."
                    isAuction && (startingPrice.toLongOrNull()?.let { it < MIN_CURRENCY_VALUE } != false ||
                            bidIncrement.toLongOrNull()?.let { it < MIN_CURRENCY_VALUE } != false ||
                            depositAmount.toLongOrNull()?.let { it < MIN_CURRENCY_VALUE } != false) -> "Các giá trị tiền đấu giá phải từ 1.000₫."
                    else -> null
                }
                if (validationError != null) {
                    localError = validationError
                } else {
                    localError = null

                    // Build specifications object with nullable values
                    val specifications = VehicleSpecifications(
                        warranty = if (warrantyBasic.isNotBlank() || warrantyBattery.isNotBlank() || warrantyDrivetrain.isNotBlank()) {
                            Warranty(
                                basic = warrantyBasic.takeIf { it.isNotBlank() },
                                battery = warrantyBattery.takeIf { it.isNotBlank() },
                                drivetrain = warrantyDrivetrain.takeIf { it.isNotBlank() }
                            )
                        } else null,
                        dimensions = if (dimWidth.isNotBlank() || dimHeight.isNotBlank() || dimLength.isNotBlank() || dimCurbWeight.isNotBlank()) {
                            Dimensions(
                                width = dimWidth.takeIf { it.isNotBlank() },
                                height = dimHeight.takeIf { it.isNotBlank() },
                                length = dimLength.takeIf { it.isNotBlank() },
                                curbWeight = dimCurbWeight.takeIf { it.isNotBlank() }
                            )
                        } else null,
                        performance = if (perfTopSpeed.isNotBlank() || perfMotorType.isNotBlank() || perfHorsepower.isNotBlank() || perfAcceleration.isNotBlank()) {
                            Performance(
                                topSpeed = perfTopSpeed.takeIf { it.isNotBlank() },
                                motorType = perfMotorType.takeIf { it.isNotBlank() },
                                horsepower = perfHorsepower.takeIf { it.isNotBlank() },
                                acceleration = perfAcceleration.takeIf { it.isNotBlank() }
                            )
                        } else null,
                        batteryAndCharging = if (battRange.isNotBlank() || battChargeTime.isNotBlank() || battChargingSpeed.isNotBlank() || battCapacity.isNotBlank()) {
                            BatteryAndCharging(
                                range = battRange.takeIf { it.isNotBlank() },
                                chargeTime = battChargeTime.takeIf { it.isNotBlank() },
                                chargingSpeed = battChargingSpeed.takeIf { it.isNotBlank() },
                                batteryCapacity = battCapacity.takeIf { it.isNotBlank() }
                            )
                        } else null
                    )

                    val request = CreateVehicleRequest(
                        title = title,
                        description = description,
                        price = price.toLong(),
                        status = "AVAILABLE",
                        brand = brand,
                        model = model,
                        year = year.toInt(),
                        mileage = mileage.toLong(),
                        specifications = specifications,
                        isAuction = if (isAuction) true else null,
                        startingPrice = if (isAuction) startingPrice.toLongOrNull() else null,
                        bidIncrement = if (isAuction) bidIncrement.toLongOrNull() else null,
                        depositAmount = if (isAuction) depositAmount.toLongOrNull() else null
                    )
                    viewModel.createVehicle(request, selectedImages)
                }
            }
        )
    }
}

@Composable
private fun BatteryForm(
    viewModel: SellerCreateListingViewModel,
    uiState: SellerCreateUiState
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var capacity by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var health by rememberSaveable { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf(emptyList<Uri>()) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) selectedImages = uris
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        EnhancedListingCard(
            title = "Thông tin pin",
            icon = Icons.Default.BatteryChargingFull,
            description = "Điền đầy đủ thông tin về bộ pin"
        ) {
            EnhancedListingTextField(
                value = title,
                onValueChange = { title = it },
                label = "Tiêu đề tin đăng",
                icon = Icons.Default.Title,
                placeholder = "VD: Pin Tesla 75kWh còn mới 95%"
            )

            EnhancedListingTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả chi tiết",
                icon = Icons.Default.Description,
                placeholder = "Mô tả tình trạng, lịch sử sử dụng...",
                minLines = 3
            )

            EnhancedListingTextField(
                value = price,
                onValueChange = { price = digitsOnly(it) },
                label = "Giá bán (₫)",
                icon = Icons.Default.AttachMoney,
                placeholder = "50000000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            EnhancedListingTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "Thương hiệu",
                icon = Icons.Default.Business,
                placeholder = "Tesla, CATL, LG..."
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedListingTextField(
                    value = capacity,
                    onValueChange = { capacity = digitsOnly(it) },
                    label = "Dung lượng",
                    icon = Icons.Default.BatteryFull,
                    placeholder = "75 kWh",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                EnhancedListingTextField(
                    value = health,
                    onValueChange = { health = digitsOnly(it) },
                    label = "Tình trạng",
                    icon = Icons.Default.HealthAndSafety,
                    placeholder = "95%",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            EnhancedListingTextField(
                value = year,
                onValueChange = { year = digitsOnly(it, 4) },
                label = "Năm sản xuất",
                icon = Icons.Default.CalendarToday,
                placeholder = "2023",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        EnhancedImagePickerSection(
            title = "Ảnh pin",
            description = "Tối đa 5 ảnh, định dạng JPG/PNG",
            selectedImages = selectedImages,
            onPickImages = {
                pickImagesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        AnimatedVisibility(
            visible = localError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            localError?.let { ErrorMessage(it) }
        }

        EnhancedSubmitButton(
            text = if (uiState.isSubmitting) "Đang đăng tải..." else "Đăng tin pin",
            isLoading = uiState.isSubmitting,
            onClick = {
                val priceValue = price.toLongOrNull()
                val capacityValue = capacity.toIntOrNull()
                val yearValue = year.toIntOrNull()
                val healthValue = health.toIntOrNull()
                val validationError = when {
                    title.isBlank() || description.isBlank() || price.isBlank() ||
                            brand.isBlank() || capacity.isBlank() || year.isBlank() || health.isBlank() ->
                        "Vui lòng nhập đầy đủ thông tin bắt buộc."
                    selectedImages.isEmpty() -> "Vui lòng chọn ít nhất 1 ảnh."
                    priceValue == null || priceValue < MIN_CURRENCY_VALUE ->
                        "Giá bán tối thiểu 1.000₫."
                    capacityValue == null || yearValue == null || healthValue == null ->
                        "Giá trị số không hợp lệ."
                    else -> null
                }
                if (validationError != null) {
                    localError = validationError
                } else {
                    localError = null
                    val request = CreateBatteryRequest(
                        title = title,
                        description = description,
                        price = priceValue!!,
                        status = "AVAILABLE",
                        brand = brand,
                        capacity = capacityValue!!,
                        year = yearValue!!,
                        health = healthValue!!,
                        specifications = BatterySpecifications()
                    )
                    viewModel.createBattery(request, selectedImages)
                }
            }
        )
    }
}

@Composable
private fun EnhancedListingCard(
    title: String,
    icon: ImageVector,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
//                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryGreen.copy(alpha = 0.15f),
                                        PrimaryGreen.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                content()
            }
        }
    }
}

@Composable
private fun EnhancedListingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = {
            Text(
                placeholder,
                color = TextSecondary.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(22.dp)
            )
        },
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        maxLines = if (minLines > 1) 5 else 1,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = Color(0xFFE8E8E8),
            focusedContainerColor = Color(0xFFF8FCFF),
            unfocusedContainerColor = Color.White,
            focusedLabelColor = PrimaryGreen,
            unfocusedLabelColor = TextSecondary
        )
    )
}

@Composable
private fun EnhancedAuctionToggle(
    isAuction: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isAuction) },
        shape = RoundedCornerShape(18.dp),
        color = if (isAuction) PrimaryGreen.copy(alpha = 0.08f) else Color(0xFFF5F5F5),
        border = BorderStroke(
            1.5.dp,
            if (isAuction) PrimaryGreen.copy(alpha = 0.3f) else Color(0xFFE8E8E8)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isAuction) PrimaryGreen.copy(alpha = 0.15f) else Color.White,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = if (isAuction) PrimaryGreen else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Bật chế độ đấu giá",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Cho phép người mua đặt giá thầu",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Switch(
                checked = isAuction,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCCCCCC)
                )
            )
        }
    }
}

@Composable
private fun EnhancedImagePickerSection(
    title: String,
    description: String,
    selectedImages: List<Uri>,
    onPickImages: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
//                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF9C27B0).copy(alpha = 0.15f),
                                        Color(0xFF9C27B0).copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Upload button
                Surface(
                    onClick = onPickImages,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        2.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryGreen.copy(alpha = 0.5f),
                                PrimaryGreen.copy(alpha = 0.5f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF0F9FF),
                                        Color.White
                                    )
                                )
                            )
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        PrimaryGreen.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "Chọn hình ảnh",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Nhấn để chọn từ thư viện",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Selected images grid
                if (selectedImages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đã chọn ${selectedImages.size} ảnh",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PrimaryGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Hợp lệ",
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(selectedImages) { index, uri ->
                                var visible by remember { mutableStateOf(false) }

                                LaunchedEffect(Unit) {
                                    delay(index * 100L)
                                    visible = true
                                }

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = scaleIn(tween(300)) + fadeIn(tween(300))
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Box {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                            )
                                            // Image number badge
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp),
                                                shape = CircleShape,
                                                color = PrimaryGreen,
                                                shadowElevation = 4.dp
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .padding(4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Chưa có hình ảnh nào được chọn",
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEBEE),
        border = BorderStroke(1.5.dp, Color(0xFFEF5350).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EnhancedSubmitButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (!isLoading) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale),
        enabled = !isLoading,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = if (!isLoading) 12.dp else 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (!isLoading) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryGreen,
                                Color(0xFF00BFA5)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFCCCCCC),
                                Color(0xFFBBBBBB)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private enum class CreateListingTab(val label: String) {
    Vehicle("Xe / Phương tiện"),
    Battery("Pin / Bộ pin")
}


private const val MIN_CURRENCY_VALUE = 1_000L

private fun digitsOnly(input: String, maxLength: Int? = null): String {
    val filtered = input.filter { it.isDigit() }
    return maxLength?.let { filtered.take(it) } ?: filtered
}

private fun openAuctionPicker(
    context: Context,
    zoneId: ZoneId,
    formatter: DateTimeFormatter,
    onResult: (iso: String, display: String) -> Unit
) {
    val now = LocalDateTime.now()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selected = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                    if (selected.isBefore(LocalDateTime.now())) {
                        Toast.makeText(context, "Thời gian kết thúc phải lớn hơn hiện tại", Toast.LENGTH_SHORT).show()
                    } else {
                        val instant = selected.atZone(zoneId).toInstant()
                        onResult(instant.toString(), selected.format(formatter))
                    }
                },
                now.hour,
                now.minute,
                true
            ).show()
        },
        now.year,
        now.monthValue - 1,
        now.dayOfMonth
    )
    datePicker.datePicker.minDate = System.currentTimeMillis()
    datePicker.show()
}
