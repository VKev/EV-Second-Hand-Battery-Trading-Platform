package com.example.evsecondhand.data.zalopay

import android.app.Activity
import android.net.Uri
import android.util.Log

object ZaloPaySDKHelper {
    private const val TAG = "ZaloPaySDKHelper"

    // No-op; giữ API để MainActivity gọi không lỗi
    fun init() {
        Log.d(TAG, "Stub init: ZaloPay SDK is not integrated; using payUrl flow.")
    }

    // Stub để tránh crash nếu có chỗ còn gọi
    fun payWithZaloPay(
        activity: Activity,
        zpTransToken: String,
        onPaymentResult: (Int, String) -> Unit
    ) {
        Log.w(TAG, "Stub payWithZaloPay called without SDK. Use payUrl flow instead.")
        onPaymentResult(-2, "ZaloPay SDK not integrated. Use payUrl flow.")
    }

    fun handleDeepLink(data: Uri?) {
        Log.d(TAG, "Stub handleDeepLink: $data")
    }
}
