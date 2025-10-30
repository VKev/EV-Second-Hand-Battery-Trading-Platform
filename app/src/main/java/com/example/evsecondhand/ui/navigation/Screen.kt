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
    object Checkout : Screen("checkout/{listingId}/{listingType}/{listingName}/{listingPrice}/{listingImage}") {
        fun createRoute(
            listingId: String,
            listingType: String,
            listingName: String,
            listingPrice: Int,
            listingImage: String?
        ) = "checkout/$listingId/$listingType/${java.net.URLEncoder.encode(listingName, "UTF-8")}/$listingPrice/${java.net.URLEncoder.encode(listingImage ?: "null", "UTF-8")}"
    }
    object CheckoutSuccess : Screen("checkout_success/{transactionId}/{listingName}/{amount}/{paymentMethod}") {
        fun createRoute(
            transactionId: String,
            listingName: String,
            amount: Int,
            paymentMethod: String
        ) = "checkout_success/$transactionId/${java.net.URLEncoder.encode(listingName, "UTF-8")}/$amount/${java.net.URLEncoder.encode(paymentMethod, "UTF-8")}"
    }
    object CheckoutFailure : Screen("checkout_failure/{transactionId}/{listingName}/{amount}/{paymentMethod}/{errorMessage}") {
        fun createRoute(
            transactionId: String,
            listingName: String,
            amount: Int,
            paymentMethod: String,
            errorMessage: String
        ) = "checkout_failure/$transactionId/${java.net.URLEncoder.encode(listingName, "UTF-8")}/$amount/$paymentMethod/${java.net.URLEncoder.encode(errorMessage, "UTF-8")}"
    }
}