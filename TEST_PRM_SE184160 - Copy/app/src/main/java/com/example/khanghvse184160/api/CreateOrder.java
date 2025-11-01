package com.example.khanghvse184160.api;

import com.example.khanghvse184160.constant.AppInfo;
import com.example.khanghvse184160.helper.Helpers;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class CreateOrder {
    private class CreateOrderData {
        String app_id;
        String app_user;
        String app_time;
        String amount;
        String app_trans_id;
        String embed_data;
        String item;
        String bank_code;
        String description;
        String mac;
        String callback_url;

        private CreateOrderData(String amount) throws Exception {
            long appTime = new Date().getTime();
            app_id = String.valueOf(AppInfo.APP_ID);
            app_user = "Android_Demo";
            app_time = String.valueOf(appTime);
            this.amount = amount;
            app_trans_id = Helpers.getAppTransId();
            embed_data = "{}";
            item = "[]";
            bank_code = "zalopayapp";
            description = "Payment for order #" + Helpers.getAppTransId();
            callback_url = AppInfo.CALLBACK_URL;
            
            // MAC for v2: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                    this.app_id,
                    this.app_trans_id,
                    this.app_user,
                    this.amount,
                    this.app_time,
                    this.embed_data,
                    this.item);

            mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);
        }
    }

     public JSONObject createOrder(String amount) throws Exception {
        CreateOrderData input = new CreateOrderData(amount);

        RequestBody formBody = new FormBody.Builder()
                .add("app_id", input.app_id)
                .add("app_user", input.app_user)
                .add("app_time", input.app_time)
                .add("amount", input.amount)
                .add("app_trans_id", input.app_trans_id)
                .add("embed_data", input.embed_data)
                .add("item", input.item)
                .add("bank_code", input.bank_code)
                .add("description", input.description)
                .add("callback_url", input.callback_url)
                .add("mac", input.mac)
                .build();

        JSONObject data = HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, formBody);
        return data;
    }
}

