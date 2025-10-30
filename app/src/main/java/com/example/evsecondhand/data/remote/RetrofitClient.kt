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
        prettyPrint = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Add headers interceptor to match web app behavior
    private val headersInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Origin", "https://ev-market-0209.vercel.app")
            .header("Referer", "https://ev-market-0209.vercel.app/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
        
        val request = requestBuilder.build()
        chain.proceed(request)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headersInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(okhttp3.ConnectionPool(5, 30, TimeUnit.SECONDS))
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
    val checkoutApi: CheckoutApiService = retrofit.create(CheckoutApiService::class.java)
}
