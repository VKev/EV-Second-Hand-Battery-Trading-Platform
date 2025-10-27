package com.example.evsecondhand.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Products : Screen("products")
    object AddPost : Screen("add_post")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object PurchaseHistory : Screen("purchase_history")
    object BatteryDetail : Screen("battery_detail/{batteryId}") {
        fun createRoute(batteryId: String) = "battery_detail/$batteryId"
    }
    object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: String) = "vehicle_detail/$vehicleId"
    }
}