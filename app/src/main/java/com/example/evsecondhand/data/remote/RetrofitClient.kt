package com.example.evsecondhand.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val BASE_URL = "https://evmarket-api-staging-backup.onrender.com/api/v1/"
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    val productApi: ProductApiService = retrofit.create(ProductApiService::class.java)
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val chatbotApi: ChatbotApiService = retrofit.create(ChatbotApiService::class.java)
    val walletApi: WalletApiService = retrofit.create(WalletApiService::class.java)
    val purchaseApi: PurchaseApiService = retrofit.create(PurchaseApiService::class.java)
}
