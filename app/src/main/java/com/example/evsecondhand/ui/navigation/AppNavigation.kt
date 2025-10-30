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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.evsecondhand.ui.screen.seller.SellerCreateListingScreen
import com.example.evsecondhand.ui.screen.seller.SellerDashboardScreen
import com.example.evsecondhand.ui.screen.payment.PaymentDashboardScreen
import com.example.evsecondhand.ui.screen.vehicle.VehicleDetailScreen
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel
import com.example.evsecondhand.ui.viewmodel.PaymentViewModel
import com.example.evsecondhand.ui.viewmodel.SellerCreateListingViewModel
import com.example.evsecondhand.ui.viewmodel.SellerDashboardViewModel

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
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    
    // Navigate to login when logged out
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            if (isLoggedIn && shouldShowBottomBar(navController)) {
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
                        onPaymentDashboard = {
                            navController.navigate(
                                Screen.Payment.createRoute(
                                    itemType = "battery",
                                    itemId = batteryId
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
                        onPaymentDashboard = {
                            navController.navigate(
                                Screen.Payment.createRoute(
                                    itemType = "vehicle",
                                    itemId = vehicleId
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
                val token = authViewModel.getAccessToken()
                if (token.isNullOrBlank()) {
                    AddPostScreen()
                } else {
                    val application = LocalContext.current.applicationContext as android.app.Application
                    val createViewModel: SellerCreateListingViewModel = viewModel(
                        factory = SellerCreateListingViewModel.provideFactory(application, token)
                    )
                    SellerCreateListingScreen(
                        viewModel = createViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToDashboard = {
                            navController.navigate(Screen.SellerDashboard.route) {
                                popUpTo(Screen.AddPost.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Screen.SellerDashboard.route) {
                val token = authViewModel.getAccessToken()
                if (token.isNullOrBlank()) {
                    AddPostScreen()
                } else {
                    val sellerDashboardViewModel: SellerDashboardViewModel = viewModel(
                        factory = SellerDashboardViewModel.provideFactory(token)
                    )
                    SellerDashboardScreen(
                        viewModel = sellerDashboardViewModel,
                        onBatteryClick = { batteryId ->
                            navController.navigate(Screen.BatteryDetail.createRoute(batteryId))
                        },
                        onBackClick = { navController.popBackStack() },
                        onAddListingClick = {
                            navController.navigate(Screen.AddPost.route)
                        }
                    )
                }
            }
            
            composable(Screen.Wallet.route) {
                WalletScreen()
            }

            composable(
                route = "${Screen.Payment.route}?${Screen.Payment.ARG_ITEM_TYPE}={${Screen.Payment.ARG_ITEM_TYPE}}&${Screen.Payment.ARG_ITEM_ID}={${Screen.Payment.ARG_ITEM_ID}}",
                arguments = listOf(
                    navArgument(Screen.Payment.ARG_ITEM_TYPE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(Screen.Payment.ARG_ITEM_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val token = authViewModel.getAccessToken()
                if (token.isNullOrBlank()) {
                    Text(
                        text = "Vui lòng đăng nhập để truy cập trang thanh toán.",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val itemType = backStackEntry.arguments?.getString(Screen.Payment.ARG_ITEM_TYPE)
                    val itemId = backStackEntry.arguments?.getString(Screen.Payment.ARG_ITEM_ID)

                    val paymentViewModel: PaymentViewModel = viewModel(
                        factory = PaymentViewModel.provideFactory(token)
                    )
                    PaymentDashboardScreen(
                        viewModel = paymentViewModel,
                        productType = itemType,
                        productId = itemId,
                        onBackClick = { navController.popBackStack() },
                        onPaymentSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onSellerDashboardClick = {
                        navController.navigate(Screen.SellerDashboard.route)
                    },
                    onPaymentDashboard = {
                        navController.navigate(Screen.Payment.route)
                    }
                )
            }
        }
    }
}

@Composable
fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    return currentRoute !in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.AddPost.route,
        Screen.SellerDashboard.route,
        Screen.Payment.route
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    val items = if (isLoggedIn) {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Products,
            BottomNavItem.AddPost,
            BottomNavItem.Wallet,
            BottomNavItem.Profile
        )
    } else {
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
