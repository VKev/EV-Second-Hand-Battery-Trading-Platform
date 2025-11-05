package com.example.evsecondhand.ui.screen.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.example.evsecondhand.data.model.seller.BatterySpecifications
import com.example.evsecondhand.data.model.seller.CreateBatteryRequest
import com.example.evsecondhand.data.model.seller.CreateVehicleRequest
import com.example.evsecondhand.data.model.*
import com.example.evsecondhand.ui.theme.*
import com.example.evsecondhand.ui.viewmodel.SellerCreateListingViewModel
import com.example.evsecondhand.ui.viewmodel.SellerCreateUiState

@OptIn(ExperimentalMaterial3Api::class)
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

    var activeTab by rememberSaveable { mutableStateOf(ListingType.Vehicle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đăng bán sản phẩm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab selector - Compact version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ListingType.values().forEach { type ->
                    FilterChip(
                        selected = activeTab == type,
                        onClick = { activeTab = type },
                        label = { 
                            Text(
                                text = type.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (activeTab == type) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        },
                        leadingIcon = {
                            Text(
                                text = type.emoji,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF0F0F0),
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.15f),
                            labelColor = Color(0xFF666666),
                            selectedLabelColor = PrimaryGreen
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = activeTab == type,
                            borderColor = if (activeTab == type) PrimaryGreen.copy(alpha = 0.3f) else Color.Transparent,
                            selectedBorderColor = PrimaryGreen.copy(alpha = 0.3f),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        )
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

            // Content based on selected tab
            when (activeTab) {
                ListingType.Vehicle -> VehicleForm(viewModel, uiState, snackbarHostState, onNavigateToDashboard)
                ListingType.Battery -> BatteryForm(viewModel, uiState, snackbarHostState, onNavigateToDashboard)
            }
        }
    }
}

@Composable
private fun VehicleForm(
    viewModel: SellerCreateListingViewModel,
    uiState: SellerCreateUiState,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }
    
    // Warranty fields
    var baseWarranty by rememberSaveable { mutableStateOf("") }
    var batteryWarranty by rememberSaveable { mutableStateOf("") }
    var drivetrainWarranty by rememberSaveable { mutableStateOf("") }
    
    // Dimensions
    var length by rememberSaveable { mutableStateOf("") }
    var width by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var curbWeight by rememberSaveable { mutableStateOf("") }
    
    // Performance
    var topSpeed by rememberSaveable { mutableStateOf("") }
    var motorType by rememberSaveable { mutableStateOf("") }
    var horsepower by rememberSaveable { mutableStateOf("") }
    var acceleration by rememberSaveable { mutableStateOf("") }
    
    // Battery & Charging
    var range by rememberSaveable { mutableStateOf("") }
    var chargeTime by rememberSaveable { mutableStateOf("") }
    var chargingSpeed by rememberSaveable { mutableStateOf("") }
    var batteryCapacity by rememberSaveable { mutableStateOf("") }
    
    var selectedImages by remember { mutableStateOf(emptyList<Uri>()) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = (selectedImages + uris).take(5)
            selectedImages = newImages
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp), // Extra padding for button
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Thông tin cơ bản
        Text(
            text = "Thông tin cơ bản",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        SimpleTextField(
            value = title,
            onValueChange = { title = it },
            label = "Tiêu đề *",
            placeholder = "Nhập tiêu đề xe"
        )

        SimpleTextField(
            value = description,
            onValueChange = { description = it },
            label = "Mô tả *",
            placeholder = "Mô tả chi tiết về xe",
            minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() } },
                label = "Giá (VND) *",
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "Thương hiệu *",
                placeholder = "Honda, Toyota...",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = model,
                onValueChange = { model = it },
                label = "Model *",
                placeholder = "Civic, Camry...",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = year,
                onValueChange = { year = it.filter { char -> char.isDigit() }.take(4) },
                label = "Năm sản xuất *",
                placeholder = "2020",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        SimpleTextField(
            value = mileage,
            onValueChange = { mileage = it.filter { char -> char.isDigit() } },
            label = "Số km đã đi *",
            placeholder = "50000",
            keyboardType = KeyboardType.Number
        )

        // Thông số kỹ thuật (tùy chọn)
        Text(
            text = "Thông số kỹ thuật (tùy chọn)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Bảo hành
        Text(
            text = "Bảo hành",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        
        SimpleTextField(
            value = baseWarranty,
            onValueChange = { baseWarranty = it },
            label = "Bảo hành cơ bản",
            placeholder = "4 years / 50,000 miles"
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = batteryWarranty,
                onValueChange = { batteryWarranty = it },
                label = "Bảo hành pin",
                placeholder = "8 years / 120,000",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = drivetrainWarranty,
                onValueChange = { drivetrainWarranty = it },
                label = "Bảo hành hệ dẫn động",
                placeholder = "8 years / 120,000",
                modifier = Modifier.weight(1f)
            )
        }

        // Kích thước
        Text(
            text = "Kích thước",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = length,
                onValueChange = { length = it },
                label = "Chiều dài",
                placeholder = "173.3 in",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = width,
                onValueChange = { width = it },
                label = "Chiều rộng",
                placeholder = "74.8 in",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = height,
                onValueChange = { height = it },
                label = "Chiều cao",
                placeholder = "66 in",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = curbWeight,
                onValueChange = { curbWeight = it },
                label = "Trọng lượng",
                placeholder = "3569 lbs",
                modifier = Modifier.weight(1f)
            )
        }

        // Hiệu suất
        Text(
            text = "Hiệu suất",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = topSpeed,
                onValueChange = { topSpeed = it },
                label = "Tốc độ tối đa",
                placeholder = "160 mph",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = motorType,
                onValueChange = { motorType = it },
                label = "Loại motor",
                placeholder = "Single Motor RW",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = horsepower,
                onValueChange = { horsepower = it },
                label = "Công suất",
                placeholder = "491 hp",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = acceleration,
                onValueChange = { acceleration = it },
                label = "Tăng tốc 0-60",
                placeholder = "4.9 seconds",
                modifier = Modifier.weight(1f)
            )
        }

        // Pin và Sạc
        Text(
            text = "Pin và Sạc",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = range,
                onValueChange = { range = it },
                label = "Phạm vi hoạt động",
                placeholder = "430 miles (EPA)",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = chargeTime,
                onValueChange = { chargeTime = it },
                label = "Thời gian sạc",
                placeholder = "41 minutes",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = chargingSpeed,
                onValueChange = { chargingSpeed = it },
                label = "Tốc độ sạc",
                placeholder = "275 kW",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = batteryCapacity,
                onValueChange = { batteryCapacity = it },
                label = "Dung lượng pin",
                placeholder = "81 kWh",
                modifier = Modifier.weight(1f)
            )
        }

        // Image picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Hình ảnh (${selectedImages.size}/5)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                OutlinedButton(
                    onClick = {
                        pickImagesLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chọn ảnh", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                // Image previews
                if (selectedImages.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImages.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                // Delete button
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                // Validation
                val validationErrors = mutableListOf<String>()
                
                if (title.trim().length < 10) {
                    validationErrors.add("Tiêu đề phải có ít nhất 10 ký tự")
                }
                if (description.trim().length < 20) {
                    validationErrors.add("Mô tả phải có ít nhất 20 ký tự")
                }
                if (price.isBlank() || price.toLongOrNull() == null || price.toLong() <= 0) {
                    validationErrors.add("Giá phải là số dương hợp lệ")
                }
                if (brand.trim().isEmpty()) {
                    validationErrors.add("Vui lòng nhập thương hiệu")
                }
                if (model.trim().isEmpty()) {
                    validationErrors.add("Vui lòng nhập model")
                }
                if (year.length != 4 || year.toIntOrNull() == null) {
                    validationErrors.add("Năm sản xuất phải là 4 chữ số")
                }
                if (mileage.isBlank() || mileage.toLongOrNull() == null) {
                    validationErrors.add("Số km đã đi phải là số hợp lệ")
                }
                
                if (validationErrors.isNotEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = validationErrors.first(),
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@Button
                }
                
                val warranty = if (baseWarranty.isNotBlank() || batteryWarranty.isNotBlank() || drivetrainWarranty.isNotBlank()) {
                    Warranty(
                        basic = baseWarranty.ifBlank { null },
                        battery = batteryWarranty.ifBlank { null },
                        drivetrain = drivetrainWarranty.ifBlank { null }
                    )
                } else null
                
                val dimensions = if (length.isNotBlank() || width.isNotBlank() || height.isNotBlank() || curbWeight.isNotBlank()) {
                    Dimensions(
                        length = length.ifBlank { null },
                        width = width.ifBlank { null },
                        height = height.ifBlank { null },
                        curbWeight = curbWeight.ifBlank { null }
                    )
                } else null
                
                val performance = if (topSpeed.isNotBlank() || motorType.isNotBlank() || horsepower.isNotBlank() || acceleration.isNotBlank()) {
                    Performance(
                        topSpeed = topSpeed.ifBlank { null },
                        motorType = motorType.ifBlank { null },
                        horsepower = horsepower.ifBlank { null },
                        acceleration = acceleration.ifBlank { null }
                    )
                } else null
                
                val batteryAndCharging = if (range.isNotBlank() || chargeTime.isNotBlank() || chargingSpeed.isNotBlank() || batteryCapacity.isNotBlank()) {
                    BatteryAndCharging(
                        range = range.ifBlank { null },
                        chargeTime = chargeTime.ifBlank { null },
                        chargingSpeed = chargingSpeed.ifBlank { null },
                        batteryCapacity = batteryCapacity.ifBlank { null }
                    )
                } else null
                
                val specs = VehicleSpecifications(
                    warranty = warranty,
                    dimensions = dimensions,
                    performance = performance,
                    batteryAndCharging = batteryAndCharging
                )
                
                val request = CreateVehicleRequest(
                    title = title.trim(),
                    description = description.trim(),
                    price = price.toLong(),
                    status = "AVAILABLE",
                    brand = brand,
                    model = model,
                    year = year.toInt(),
                    mileage = mileage.toLong(),
                    specifications = specs,
                    isAuction = null,
                    startingPrice = null,
                    bidIncrement = null,
                    depositAmount = null
                )
                viewModel.createVehicle(request, selectedImages)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isSubmitting && title.isNotBlank() && price.isNotBlank() && brand.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.DirectionsCar, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.width(8.dp))
                Text("Đăng bán xe", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            }
        }
    }
}

@Composable
private fun BatteryForm(
    viewModel: SellerCreateListingViewModel,
    uiState: SellerCreateUiState,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var capacity by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var health by rememberSaveable { mutableStateOf("") }
    
    // Technical specs
    var weight by rememberSaveable { mutableStateOf("") }
    var voltage by rememberSaveable { mutableStateOf("") }
    var chemistry by rememberSaveable { mutableStateOf("") }
    var degradation by rememberSaveable { mutableStateOf("") }
    var chargingTime by rememberSaveable { mutableStateOf("") }
    var installation by rememberSaveable { mutableStateOf("") }
    var warrantyPeriod by rememberSaveable { mutableStateOf("") }
    var temperatureRange by rememberSaveable { mutableStateOf("") }
    
    var selectedImages by remember { mutableStateOf(emptyList<Uri>()) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = (selectedImages + uris).take(5)
            selectedImages = newImages
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp), // Extra padding for button
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Thông tin cơ bản
        Text(
            text = "Thông tin cơ bản",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        SimpleTextField(
            value = title,
            onValueChange = { title = it },
            label = "Tiêu đề *",
            placeholder = "Nhập tiêu đề pin"
        )

        SimpleTextField(
            value = description,
            onValueChange = { description = it },
            label = "Mô tả *",
            placeholder = "Mô tả chi tiết về pin",
            minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() } },
                label = "Giá (VND) *",
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "Thương hiệu *",
                placeholder = "Tesla, BYD, LG...",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = capacity,
                onValueChange = { capacity = it.filter { char -> char.isDigit() } },
                label = "Dung lượng (kWh) *",
                placeholder = "75",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = year,
                onValueChange = { year = it.filter { char -> char.isDigit() }.take(4) },
                label = "Năm sản xuất *",
                placeholder = "2020",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        SimpleTextField(
            value = health,
            onValueChange = { health = it.filter { char -> char.isDigit() } },
            label = "Sức khỏe pin (%) *",
            placeholder = "85",
            keyboardType = KeyboardType.Number
        )

        // Thông số kỹ thuật (tùy chọn)
        Text(
            text = "Thông số kỹ thuật (tùy chọn)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "Trọng lượng",
                placeholder = "528kg",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = voltage,
                onValueChange = { voltage = it },
                label = "Điện áp",
                placeholder = "408V",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = chemistry,
                onValueChange = { chemistry = it },
                label = "Loại hóa học",
                placeholder = "NMC, LFP, NCA...",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = degradation,
                onValueChange = { degradation = it },
                label = "Mức độ suy giảm",
                placeholder = "27% (73%",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = chargingTime,
                onValueChange = { chargingTime = it },
                label = "Thời gian sạc",
                placeholder = "75 minutes",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = installation,
                onValueChange = { installation = it },
                label = "Yêu cầu lắp đặt",
                placeholder = "Professional",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleTextField(
                value = warrantyPeriod,
                onValueChange = { warrantyPeriod = it },
                label = "Thời hạn bảo hành",
                placeholder = "1 years",
                modifier = Modifier.weight(1f)
            )
            SimpleTextField(
                value = temperatureRange,
                onValueChange = { temperatureRange = it },
                label = "Phạm vi nhiệt độ",
                placeholder = "-20°C to 60°C",
                modifier = Modifier.weight(1f)
            )
        }

        // Image picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Hình ảnh (${selectedImages.size}/5)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                OutlinedButton(
                    onClick = {
                        pickImagesLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chọn ảnh", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                // Image previews
                if (selectedImages.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImages.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Delete button
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                // Validation
                val validationErrors = mutableListOf<String>()
                
                if (title.trim().length < 10) {
                    validationErrors.add("Tiêu đề phải có ít nhất 10 ký tự")
                }
                if (description.trim().length < 20) {
                    validationErrors.add("Mô tả phải có ít nhất 20 ký tự")
                }
                if (price.isBlank() || price.toLongOrNull() == null || price.toLong() <= 0) {
                    validationErrors.add("Giá phải là số dương hợp lệ")
                }
                if (brand.trim().isEmpty()) {
                    validationErrors.add("Vui lòng nhập thương hiệu")
                }
                if (capacity.isBlank() || capacity.toIntOrNull() == null || capacity.toInt() <= 0) {
                    validationErrors.add("Dung lượng pin phải là số dương hợp lệ")
                }
                if (year.length != 4 || year.toIntOrNull() == null) {
                    validationErrors.add("Năm sản xuất phải là 4 chữ số")
                }
                if (health.isBlank() || health.toIntOrNull() == null || health.toInt() !in 1..100) {
                    validationErrors.add("Sức khỏe pin phải từ 1-100%")
                }
                
                if (validationErrors.isNotEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = validationErrors.first(),
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@Button
                }
                
                val specs = BatterySpecifications(
                    weight = weight.ifBlank { null },
                    voltage = voltage.ifBlank { null },
                    chemistry = chemistry.ifBlank { null },
                    degradation = degradation.ifBlank { null },
                    chargingTime = chargingTime.ifBlank { null },
                    installation = installation.ifBlank { null },
                    warrantyPeriod = warrantyPeriod.ifBlank { null },
                    temperatureRange = temperatureRange.ifBlank { null }
                )
                
                val request = CreateBatteryRequest(
                    title = title.trim(),
                    description = description.trim(),
                    price = price.toLong(),
                    status = "AVAILABLE",
                    brand = brand,
                    capacity = capacity.toInt(),
                    year = year.toInt(),
                    health = health.toInt(),
                    specifications = specs,
                    isAuction = null,
                    startingPrice = null,
                    bidIncrement = null,
                    depositAmount = null
                )
                viewModel.createBattery(request, selectedImages)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isSubmitting && title.isNotBlank() && price.isNotBlank() && brand.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.BatteryChargingFull, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.width(8.dp))
                Text("Đăng bán pin", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    text = placeholder, 
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            maxLines = if (minLines > 1) 5 else 1,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = PrimaryGreen,
                cursorColor = PrimaryGreen
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
        )
    }
}

private enum class ListingType(val displayName: String, val emoji: String) {
    Vehicle("Xe điện", "🚗"),
    Battery("Pin điện", "🔋")
}