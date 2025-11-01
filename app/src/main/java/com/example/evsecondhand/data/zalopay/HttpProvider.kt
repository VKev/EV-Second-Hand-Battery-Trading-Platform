package com.example.evsecondhand.data.zalopay

import android.util.Log
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.TlsVersion
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object HttpProvider {
    private const val TAG = "ZaloPayHttpProvider"
    
    fun sendPost(url: String, formBody: FormBody): JSONObject? {
        return try {
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()
            
            val client = OkHttpClient.Builder()
                .connectionSpecs(listOf(spec))
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Bad request: ${response.body?.string()}")
                null
            } else {
                val responseBody = response.body?.string()
                JSONObject(responseBody ?: "{}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending POST request", e)
            null
        }
    }
}
