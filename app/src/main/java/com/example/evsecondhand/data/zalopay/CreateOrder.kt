package com.example.evsecondhand.data.zalopay

import okhttp3.FormBody
import org.json.JSONObject
import java.util.Date

class CreateOrder {
    
    private data class CreateOrderData(
        val appId: String,
        val appUser: String,
        val appTime: String,
        val amount: String,
        val appTransId: String,
        val embedData: String,
        val item: String,
        val bankCode: String,
        val description: String,
        val mac: String,
        val callbackUrl: String
    ) {
        companion object {
            fun create(amount: String): CreateOrderData {
                val appTime = Date().time
                val appId = ZaloPayConfig.APP_ID.toString()
                val appUser = ZaloPayConfig.APP_USER
                val appTransId = ZaloPayHelper.getAppTransId()
                val embedData = "{}"
                val item = "[]"
                val bankCode = ZaloPayConfig.BANK_CODE
                val description = "EV Market - Nạp tiền vào ví #$appTransId"
                val callbackUrl = ZaloPayConfig.CALLBACK_URL
                
                // MAC for v2: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
                val inputHMac = String.format(
                    "%s|%s|%s|%s|%s|%s|%s",
                    appId,
                    appTransId,
                    appUser,
                    amount,
                    appTime.toString(),
                    embedData,
                    item
                )
                
                val mac = ZaloPayHelper.getMac(ZaloPayConfig.MAC_KEY, inputHMac)
                
                return CreateOrderData(
                    appId = appId,
                    appUser = appUser,
                    appTime = appTime.toString(),
                    amount = amount,
                    appTransId = appTransId,
                    embedData = embedData,
                    item = item,
                    bankCode = bankCode,
                    description = description,
                    mac = mac,
                    callbackUrl = callbackUrl
                )
            }
        }
    }
    
    fun createOrder(amount: String): JSONObject? {
        val orderData = CreateOrderData.create(amount)
        
        val formBody = FormBody.Builder()
            .add("app_id", orderData.appId)
            .add("app_user", orderData.appUser)
            .add("app_time", orderData.appTime)
            .add("amount", orderData.amount)
            .add("app_trans_id", orderData.appTransId)
            .add("embed_data", orderData.embedData)
            .add("item", orderData.item)
            .add("bank_code", orderData.bankCode)
            .add("description", orderData.description)
            .add("callback_url", orderData.callbackUrl)
            .add("mac", orderData.mac)
            .build()
        
        return HttpProvider.sendPost(ZaloPayConfig.CREATE_ORDER_URL, formBody)
    }
}
