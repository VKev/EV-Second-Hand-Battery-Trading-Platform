package com.example.evsecondhand.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Products : Screen("products")
    object AddPost : Screen("add_post")
    object Wallet : Screen("wallet")
    object Payment : Screen("payment") {
        const val ARG_ITEM_TYPE = "itemType"
        const val ARG_ITEM_ID = "itemId"

        fun createRoute(itemType: String, itemId: String): String {
            val encodedId = android.net.Uri.encode(itemId)
            return "payment?$ARG_ITEM_TYPE=$itemType&$ARG_ITEM_ID=$encodedId"
        }
    }
    object Profile : Screen("profile")
    object PurchaseHistory : Screen("purchase_history")
    object SellerDashboard : Screen("seller_dashboard")
    object BatteryDetail : Screen("battery_detail/{batteryId}") {
        fun createRoute(batteryId: String) = "battery_detail/$batteryId"
    }
    object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: String) = "vehicle_detail/$vehicleId"
    }
}
