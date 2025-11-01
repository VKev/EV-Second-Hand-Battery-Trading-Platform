package com.example.evsecondhand.data.zalopay

object ZaloPayConfig {
    const val APP_ID = 554
    const val APP_USER = "EVSecondHand"
    const val MAC_KEY = "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn"
    const val CREATE_ORDER_URL = "https://sb-openapi.zalopay.vn/v2/create"
    
    // Backend callback URL - ZaloPay will send payment result here
    const val CALLBACK_URL = "https://beevmarket-production.up.railway.app/api/v1/wallet/zalopay/callback"
    
    // Deep link for returning user to app after payment
    const val DEEP_LINK = "evmarket://app"
    
    const val BANK_CODE = "zalopayapp"
}
