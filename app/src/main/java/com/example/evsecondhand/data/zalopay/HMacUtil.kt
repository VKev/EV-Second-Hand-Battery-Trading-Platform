package com.example.evsecondhand.data.zalopay

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMacUtil {
    const val HMAC_SHA256 = "HmacSHA256"
    
    private fun hmacEncode(algorithm: String, key: String, data: String): ByteArray? {
        return try {
            val mac = Mac.getInstance(algorithm)
            val signingKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), algorithm)
            mac.init(signingKey)
            mac.doFinal(data.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun hmacHexStringEncode(algorithm: String, key: String, data: String): String? {
        val hmacBytes = hmacEncode(algorithm, key, data) ?: return null
        return byteArrayToHexString(hmacBytes)
    }
    
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            result.append(hexChars[value ushr 4])
            result.append(hexChars[value and 0x0F])
        }
        return result.toString()
    }
}
