package com.example.evsecondhand.ui.navigation

import android.app.Application
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navArgument
import com.example.evsecondhand.ui.screen.ProfileScreen
import com.example.evsecondhand.ui.screen.PurchaseHistoryScreen
import com.example.evsecondhand.ui.screen.WalletScreen
import com.example.evsecondhand.ui.screen.auction.AuctionScreen
import com.example.evsecondhand.ui.screen.auctiondetail.AuctionDetailScreen
import com.example.evsecondhand.ui.screen.auctions.AuctionsScreen
import com.example.evsecondhand.ui.screen.auth.LoginScreen
import com.example.evsecondhand.ui.screen.auth.RegisterScreen
import com.example.evsecondhand.ui.screen.battery.BatteryDetailScreen
import com.example.evsecondhand.ui.screen.home.HomeScreen
import com.example.evsecondhand.ui.screen.payment.PaymentDashboardScreen
import com.example.evsecondhand.ui.screen.seller.SellerCreateListingScreen
import com.example.evsecondhand.ui.screen.seller.SellerDashboardScreen
import com.example.evsecondhand.ui.screen.vehicle.VehicleDetailScreen
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.AuthViewModel
import com.example.evsecondhand.ui.viewmodel.HomeViewModel
import com.example.evsecondhand.ui.viewmodel.PaymentViewModel
import com.example.evsecondhand.ui.viewmodel.SellerCreateListingViewModel
import com.example.evsecondhand.ui.viewmodel.SellerDashboardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Trang \u1EE7", Icons.Default.Home)
    object Auctions : BottomNavItem(Screen.Auctions.route, "\u0110\u1EA5u gi\u00E1", Icons.Default.Gavel)
    object AddPost : BottomNavItem(Screen.AddPost.route, "\u0110\u0103ng tin", Icons.Default.Add)
    object Wallet : BottomNavItem(Screen.Wallet.route, "V\u00ED", Icons.Default.Wallet)
    object Profile : BottomNavItem(Screen.Profile.route, "H\u1ED3 s\u01A1", Icons.Default.Person)
}

private const val WALLET_DEEP_LINK_RESULT_KEY = "wallet_deep_link_uri"

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    deepLinkFlow: Flow<Intent>? = null
) {
    val navController = rememberNavController()
    LaunchedEffect(deepLinkFlow) {
        deepLinkFlow?.let { flow ->
            flow.collectLatest { intent ->
                val uri = intent.data
                val handledByCustom = uri?.let { data ->
                    handleWalletDeepLink(navController, data)
                } ?: false

                if (handledByCustom) {
                    android.util.Log.d(
                        "AppNavigation",
                        "Handled wallet deep link from intent: ${intent.dataString}"
                    )
                    return@collectLatest
                }

                val handledByNav = navController.handleDeepLink(intent)
                android.util.Log.d(
                    "AppNavigation",
                    "Deep link intent received: ${intent.dataString}, handledByNav=$handledByNav"
                )

                if (!handledByNav) {
                    val fallbackHandled = handleGenericDeepLink(navController, uri)
                    if (!fallbackHandled) {
                        android.util.Log.w(
                            "AppNavigation",
                            "Unhandled deep link data=${intent.dataString}"
                        )
                    }
                }
            }
        }
    }
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

            composable(
                Screen.Home.route,
                deepLinks = listOf(navDeepLink { uriPattern = "evmarket://app" })
            ) {
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
                        onBidClick = { navController.navigate(Screen.Auctions.route) },
                        onPaymentDashboard = { battery ->
                            navController.navigate(
                                Screen.Payment.createRoute(
                                    itemType = "battery",
                                    itemId = battery.id
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
                        onBidClick = {
                            navController.navigate(Screen.VehicleAuction.createRoute(vehicleId))
                        },
                        onPaymentDashboard = { vehicle ->
                            navController.navigate(
                                Screen.Payment.createRoute(
                                    itemType = "vehicle",
                                    itemId = vehicle.id
                                )
                            )
                        }
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
                val accessToken = authViewModel.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    return@composable
                }

                val application = LocalContext.current.applicationContext as Application
                val factory = remember(accessToken) {
                    SellerCreateListingViewModel.provideFactory(application, accessToken)
                }
                val viewModel: SellerCreateListingViewModel = viewModel(factory = factory)

                SellerCreateListingScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.SellerDashboard.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.SellerDashboard.route) {
                val accessToken = authViewModel.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    return@composable
                }

                val factory = remember(accessToken) {
                    SellerDashboardViewModel.provideFactory(accessToken)
                }
                val viewModel: SellerDashboardViewModel = viewModel(factory = factory)

                SellerDashboardScreen(
                    viewModel = viewModel,
                    onBatteryClick = { batteryId ->
                        navController.navigate(Screen.BatteryDetail.createRoute(batteryId))
                    },
                    onBackClick = { navController.popBackStack() },
                    onAddListingClick = {
                        navController.navigate(Screen.AddPost.route) {
                            launchSingleTop = true
                        }
                    }
                )
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
                val accessToken = authViewModel.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    return@composable
                }

                val factory = remember(accessToken) {
                    PaymentViewModel.provideFactory(accessToken)
                }
                val paymentViewModel: PaymentViewModel = viewModel(factory = factory)

                val itemType = backStackEntry.arguments?.getString(Screen.Payment.ARG_ITEM_TYPE)
                val itemId = backStackEntry.arguments?.getString(Screen.Payment.ARG_ITEM_ID)

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

            composable(
                Screen.Wallet.route,
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "evmarket://wallet{?partnerCode,resultCode,orderId,message,amount,requestId,extraData,payType,transId}"
                    }
                )
            ) { backStackEntry ->
                val deepLinkUri by backStackEntry
                    .savedStateHandle
                    .getStateFlow<String?>(WALLET_DEEP_LINK_RESULT_KEY, null)
                    .collectAsState()

                WalletScreen(
                    deepLinkUri = deepLinkUri?.let(Uri::parse),
                    onConsumeDeepLink = {
                        backStackEntry.savedStateHandle.remove<String>(WALLET_DEEP_LINK_RESULT_KEY)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToPurchaseHistory = {
                        navController.navigate(Screen.PurchaseHistory.route)
                    }
                )
            }

            composable(Screen.PurchaseHistory.route) {
                PurchaseHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    if (currentRoute == null) {
        return true
    }

    if (currentRoute.startsWith(Screen.Payment.route)) {
        return false
    }

    return currentRoute !in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.AddPost.route,
        Screen.SellerDashboard.route
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
            val navigateToItem: () -> Unit = {
                if (currentRoute != item.route) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            NavigationBarItem(
                icon = {
                    if (item == BottomNavItem.AddPost) {
                        FloatingActionButton(
                            onClick = navigateToItem,
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
                onClick = navigateToItem,
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

private fun handleWalletDeepLink(
    navController: NavHostController,
    uri: Uri
): Boolean {
    val scheme = uri.scheme?.lowercase()
    if (scheme != "evmarket") {
        return false
    }

    val partnerCode = uri.getQueryParameter("partnerCode")?.lowercase()
    val host = uri.host?.lowercase()
    val isMoMoRedirect = partnerCode == "momo"
    val isWalletHost = host == "wallet"

    if (!isMoMoRedirect && !isWalletHost) {
        return false
    }

    navController.navigate(Screen.Wallet.route) {
        launchSingleTop = true
    }

    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.set(WALLET_DEEP_LINK_RESULT_KEY, uri.toString())

    android.util.Log.d(
        "AppNavigation",
        "Routing deep link to Wallet (partnerCode=$partnerCode, resultCode=${uri.getQueryParameter("resultCode")})"
    )

    return true
}

private fun handleGenericDeepLink(
    navController: NavHostController,
    uri: Uri?
): Boolean {
    val host = uri?.host?.lowercase() ?: return false
    return when (host) {
        "wallet" -> {
            navController.navigate(Screen.Wallet.route) {
                launchSingleTop = true
            }
            true
        }
        "app" -> {
            navController.navigate(Screen.Home.route) {
                launchSingleTop = true
            }
            true
        }
        else -> false
    }
}
