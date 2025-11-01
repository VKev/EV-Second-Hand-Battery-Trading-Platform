package com.example.evsecondhand.data.zalopay

import android.app.Activity
import android.util.Log
import org.json.JSONObject
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

object ZaloPaySDKHelper {
    private const val TAG = "ZaloPaySDKHelper"
    
    /**
     * Initialize ZaloPay SDK - call this in Application onCreate or MainActivity onCreate
     */
    fun init() {
        ZaloPaySDK.init(ZaloPayConfig.APP_ID, Environment.SANDBOX)
        Log.d(TAG, "ZaloPay SDK initialized with APP_ID: ${ZaloPayConfig.APP_ID}")
    }
    
    /**
     * Open ZaloPay app to make payment using zp_trans_token
     * This will automatically open ZaloPay app (not browser)
     */
    fun payWithZaloPay(
        activity: Activity,
        zpTransToken: String,
        onPaymentResult: (Int, String) -> Unit
    ) {
        Log.d(TAG, "Initiating ZaloPay payment with token: $zpTransToken")
        
        ZaloPaySDK.getInstance().payOrder(
            activity,
            zpTransToken,
            ZaloPayConfig.DEEP_LINK, // Use deep link for returning to app
            object : PayOrderListener {
                override fun onPaymentSucceeded(
                    transactionId: String,
                    transToken: String,
                    appTransID: String
                ) {
                    Log.d(TAG, "Payment succeeded - transactionId: $transactionId, appTransID: $appTransID")
                    onPaymentResult(1, "Payment successful")
                }
                
                override fun onPaymentCanceled(zpTransToken: String, appTransID: String) {
                    Log.d(TAG, "Payment canceled - appTransID: $appTransID")
                    onPaymentResult(4, "Payment canceled by user")
                }
                
                override fun onPaymentError(
                    zaloPayError: ZaloPayError,
                    zpTransToken: String,
                    appTransID: String
                ) {
                    Log.e(TAG, "Payment error - ${zaloPayError.toString()}, appTransID: $appTransID")
                    onPaymentResult(-1, "Payment error: ${zaloPayError.toString()}")
                }
            }
        )
    }
    
    /**
     * Handle deep link when returning from ZaloPay
     * Call this in MainActivity's onNewIntent
     */
    fun handleDeepLink(data: android.net.Uri?) {
        if (data != null) {
            Log.d(TAG, "Handling deep link: $data")
            // ZaloPay SDK will automatically handle the deep link
        }
    }
}
