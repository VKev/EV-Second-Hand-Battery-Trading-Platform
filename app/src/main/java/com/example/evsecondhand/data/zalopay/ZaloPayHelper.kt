package com.example.evsecondhand.data.zalopay

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

object ZaloPayHelper {
    private var transIdCounter = 1
    
    @SuppressLint("SimpleDateFormat")
    fun getAppTransId(): String {
        if (transIdCounter >= 100000) {
            transIdCounter = 1
        }
        transIdCounter++
        
        val formatDateTime = SimpleDateFormat("yyMMdd_hhmmss")
        val timeString = formatDateTime.format(Date())
        return String.format("%s%06d", timeString, transIdCounter)
    }
    
    fun getMac(key: String, data: String): String {
        return HMacUtil.hmacHexStringEncode(HMacUtil.HMAC_SHA256, key, data) 
            ?: throw Exception("Failed to generate MAC")
    }
}
