package com.example.evsecondhand.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.evsecondhand.ui.screen.*
import com.example.evsecondhand.ui.screen.battery.BatteryDetailScreen
import com.example.evsecondhand.ui.screen.auth.LoginScreen
import com.example.evsecondhand.ui.screen.auth.RegisterScreen
import com.example.evsecondhand.ui.screen.home.HomeScreen
import com.example.evsecondhand.ui.screen.vehicle.VehicleDetailScreen
import com.example.evsecondhand.ui.screen.checkout.CheckoutSuccessScreen
import com.example.evsecondhand.ui.screen.checkout.CheckoutFailureScreen
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Trang chủ", Icons.Default.Home)
    object Products : BottomNavItem(Screen.Products.route, "Sản phẩm", Icons.Default.ShoppingCart)
    object AddPost : BottomNavItem(Screen.AddPost.route, "Đăng tin", Icons.Default.Add)
    object Wallet : BottomNavItem(Screen.Wallet.route, "Ví", Icons.Default.Wallet)
    object Profile : BottomNavItem(Screen.Profile.route, "Hồ sơ", Icons.Default.Person)
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    // Always start at Home - no need to force login
    val startDestination = Screen.Home.route
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(navController)) {
                BottomNavigationBar(navController = navController, isLoggedIn = isLoggedIn)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Home.route) {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    onBatteryClick = { batteryId ->
                        navController.navigate(Screen.BatteryDetail.createRoute(batteryId))
                    },
                    onVehicleClick = { vehicleId ->
                        navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                    }
                )
            }

            composable(
                route = Screen.BatteryDetail.route,
                arguments = listOf(navArgument("batteryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val batteryId = backStackEntry.arguments?.getString("batteryId")
                if (batteryId == null) {
                    navController.popBackStack()
                } else {
                    BatteryDetailScreen(
                        batteryId = batteryId,
                        onBackClick = { navController.popBackStack() },
                        onBuyNow = { id, name, price, image ->
                            navController.navigate(
                                Screen.Checkout.createRoute(
                                    listingId = id,
                                    listingType = "BATTERY",
                                    listingName = name,
                                    listingPrice = price,
                                    listingImage = image
                                )
                            )
                        }
                    )
                }
            }

            composable(
                route = Screen.VehicleDetail.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId")
                if (vehicleId == null) {
                    navController.popBackStack()
                } else {
                    VehicleDetailScreen(
                        vehicleId = vehicleId,
                        onBackClick = { navController.popBackStack() },
                        onBuyNow = { id, name, price, image ->
                            navController.navigate(
                                Screen.Checkout.createRoute(
                                    listingId = id,
                                    listingType = "VEHICLE",
                                    listingName = name,
                                    listingPrice = price,
                                    listingImage = image
                                )
                            )
                        }
                    )
                }
            }
            
            composable(Screen.Products.route) {
                ProductsScreen()
            }
            
            composable(Screen.AddPost.route) {
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                } else {
                    AddPostScreen()
                }
            }
            
            composable(Screen.Wallet.route) {
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                } else {
                    WalletScreen()
                }
            }
            
            composable(Screen.Profile.route) {
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                } else {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onNavigateToPurchaseHistory = {
                            navController.navigate(Screen.PurchaseHistory.route)
                        }
                    )
                }
            }
            
            composable(
                route = Screen.Checkout.route,
                arguments = listOf(
                    navArgument("listingId") { type = NavType.StringType },
                    navArgument("listingType") { type = NavType.StringType },
                    navArgument("listingName") { type = NavType.StringType },
                    navArgument("listingPrice") { type = NavType.IntType },
                    navArgument("listingImage") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                val listingType = backStackEntry.arguments?.getString("listingType") ?: ""
                val listingName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("listingName") ?: "", 
                    "UTF-8"
                )
                val listingPrice = backStackEntry.arguments?.getInt("listingPrice") ?: 0
                val listingImage = backStackEntry.arguments?.getString("listingImage")
                    ?.let { 
                        val decoded = java.net.URLDecoder.decode(it, "UTF-8")
                        if (decoded == "null") null else decoded
                    }
                
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                } else {
                    com.example.evsecondhand.ui.screen.checkout.CheckoutScreen(
                        listingId = listingId,
                        listingType = listingType,
                        listingName = listingName,
                        listingPrice = listingPrice,
                        listingImage = listingImage,
                        onNavigateToWallet = {
                            navController.navigate(Screen.Wallet.route) {
                                popUpTo(Screen.Checkout.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onCheckoutSuccess = { transactionId, productName, amount, paymentMethod ->
                            android.util.Log.d("AppNavigation", "onCheckoutSuccess triggered: txId=$transactionId, name=$productName, amount=$amount, method=$paymentMethod")
                            val route = Screen.CheckoutSuccess.createRoute(
                                transactionId = transactionId,
                                listingName = productName,
                                amount = amount,
                                paymentMethod = paymentMethod
                            )
                            android.util.Log.d("AppNavigation", "Navigating to: $route")
                            navController.navigate(route) {
                                popUpTo(Screen.Checkout.route) { inclusive = true }
                            }
                            android.util.Log.d("AppNavigation", "Navigation command executed")
                        }
                    )
                }
            }
            
            composable(
                route = Screen.CheckoutSuccess.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.StringType },
                    navArgument("listingName") { type = NavType.StringType },
                    navArgument("amount") { type = NavType.IntType },
                    navArgument("paymentMethod") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                val listingName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("listingName") ?: "", 
                    "UTF-8"
                )
                val amount = backStackEntry.arguments?.getInt("amount") ?: 0
                val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""
                
                CheckoutSuccessScreen(
                    transactionId = transactionId,
                    listingName = listingName,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    onNavigateToHistory = {
                        navController.navigate(Screen.PurchaseHistory.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.CheckoutFailure.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.StringType },
                    navArgument("listingName") { type = NavType.StringType },
                    navArgument("amount") { type = NavType.IntType },
                    navArgument("paymentMethod") { type = NavType.StringType },
                    navArgument("errorMessage") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                val listingName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("listingName") ?: "", 
                    "UTF-8"
                )
                val amount = backStackEntry.arguments?.getInt("amount") ?: 0
                val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""
                val errorMessage = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("errorMessage") ?: "", 
                    "UTF-8"
                )
                
                CheckoutFailureScreen(
                    transactionId = transactionId,
                    productName = listingName,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    errorMessage = errorMessage,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onRetryCheckout = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.PurchaseHistory.route) {
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                } else {
                    PurchaseHistoryScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    return currentRoute !in listOf(Screen.Login.route, Screen.Register.route)
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    val items = if (isLoggedIn) {
        // Khi đã login: show full menu
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Products,
            BottomNavItem.AddPost,
            BottomNavItem.Wallet,
            BottomNavItem.Profile
        )
    } else {
        // Khi chưa login: chỉ show Home, Products, và Profile (để vào login)
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Products,
            BottomNavItem.Profile
        )
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    if (item == BottomNavItem.AddPost) {
                        FloatingActionButton(
                            onClick = { navController.navigate(item.route) },
                            containerColor = PrimaryGreen,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = if (item != BottomNavItem.AddPost) {
                    { Text(item.title) }
                } else null,
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
