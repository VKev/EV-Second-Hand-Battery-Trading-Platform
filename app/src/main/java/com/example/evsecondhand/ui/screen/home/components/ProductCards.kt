package com.example.evsecondhand.ui.screen.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.evsecondhand.data.model.Battery
import com.example.evsecondhand.data.model.Vehicle
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

// Modern Battery Card with animations
@Composable
fun ModernBatteryCard(
    battery: Battery,
    onClick: () -> Unit = {}
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(340.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image with gradient overlay
                Box(modifier = Modifier.height(180.dp)) {
                    AsyncImage(
                        model = battery.images.firstOrNull(),
                        contentDescription = battery.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )
                    
                    // Verified badge
                    if (battery.isVerified) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryGreen,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Verified",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = battery.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(48.dp),
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Specs in colored chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecChip(
                            icon = Icons.Default.BatteryChargingFull,
                            text = "${battery.capacity} kWh",
                            color = PrimaryGreen
                        )
                        battery.health?.let {
                            SpecChip(
                                icon = Icons.Default.Favorite,
                                text = "$it%",
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Brand and year
                    Text(
                        text = "${battery.brand} • ${battery.year}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatter.format(battery.price),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen,
                            letterSpacing = (-0.5).sp
                        )
                        
                        // Arrow button
                        Surface(
                            shape = CircleShape,
                            color = PrimaryGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "View",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modern Vehicle Card with animations
@Composable
fun ModernVehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit = {}
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(340.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image with gradient overlay
                Box(modifier = Modifier.height(180.dp)) {
                    AsyncImage(
                        model = vehicle.images.firstOrNull(),
                        contentDescription = vehicle.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )
                    
                    if (vehicle.isVerified) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryGreen,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Verified",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = vehicle.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(48.dp),
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecChip(
                            icon = Icons.Default.DirectionsCar,
                            text = "${vehicle.brand} ${vehicle.model}",
                            color = Color(0xFF6C63FF)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecChip(
                            icon = Icons.Default.Speed,
                            text = "${NumberFormat.getInstance(Locale.US).format(vehicle.mileage)} km",
                            color = Color(0xFFFF9800)
                        )
                        SpecChip(
                            icon = Icons.Default.CalendarMonth,
                            text = "${vehicle.year}",
                            color = Color(0xFF00BCD4)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatter.format(vehicle.price),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen,
                            letterSpacing = (-0.5).sp
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = PrimaryGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "View",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
