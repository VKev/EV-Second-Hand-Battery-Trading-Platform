package com.example.evsecondhand.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.evsecondhand.ui.screen.AddPostScreen
import com.example.evsecondhand.ui.screen.ProfileScreen
import com.example.evsecondhand.ui.screen.WalletScreen
import com.example.evsecondhand.ui.screen.auction.AuctionScreen
import com.example.evsecondhand.ui.screen.auctiondetail.AuctionDetailScreen
import com.example.evsecondhand.ui.screen.auctions.AuctionsScreen
import com.example.evsecondhand.ui.screen.auth.LoginScreen
import com.example.evsecondhand.ui.screen.auth.RegisterScreen
import com.example.evsecondhand.ui.screen.battery.BatteryDetailScreen
import com.example.evsecondhand.ui.screen.home.HomeScreen
import com.example.evsecondhand.ui.screen.vehicle.VehicleDetailScreen
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Trang chủ", Icons.Default.Home)
    object Auctions : BottomNavItem(Screen.Auctions.route, "Đấu giá", Icons.Default.Gavel)
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
                        onBidClick = { navController.navigate(Screen.Auctions.route) }
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
                        onBidClick = { navController.navigate(Screen.VehicleAuction.createRoute(vehicleId)) }
                    )
                }
            }

            composable(
                route = Screen.VehicleAuction.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val auctionVehicleId = backStackEntry.arguments?.getString("vehicleId")
                if (auctionVehicleId == null) {
                    navController.popBackStack()
                } else {
                    AuctionScreen(
                        vehicleId = auctionVehicleId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Auctions.route) {
                AuctionsScreen(onAuctionClick = { summary ->
                    if (summary.listingId.isNotBlank()) {
                        navController.navigate(
                            Screen.AuctionDetail.createRoute(summary.listingType, summary.listingId)
                        )
                    }
                })
            }

            composable(
                route = Screen.AuctionDetail.route,
                arguments = listOf(
                    navArgument("listingType") { type = NavType.StringType },
                    navArgument("listingId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listingType = backStackEntry.arguments?.getString("listingType")
                val listingId = backStackEntry.arguments?.getString("listingId")
                if (listingType.isNullOrBlank() || listingId.isNullOrBlank()) {
                    navController.popBackStack()
                } else {
                    AuctionDetailScreen(
                        listingType = listingType,
                        listingId = listingId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.AddPost.route) {
                AddPostScreen()
            }

            composable(Screen.Wallet.route) {
                WalletScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(authViewModel = authViewModel)
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
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Auctions,
            BottomNavItem.AddPost,
            BottomNavItem.Wallet,
            BottomNavItem.Profile
        )
    } else {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Auctions,
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
