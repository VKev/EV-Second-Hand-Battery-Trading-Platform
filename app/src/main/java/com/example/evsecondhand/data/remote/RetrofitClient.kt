package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.remote.AuctionApiService
import com.example.evsecondhand.data.remote.AuthApiService
import com.example.evsecondhand.data.remote.ChatbotApiService
import com.example.evsecondhand.data.remote.CheckoutApiService
import com.example.evsecondhand.data.remote.ProductApiService
import com.example.evsecondhand.data.remote.PurchaseApiService
import com.example.evsecondhand.data.remote.WalletApiService
import com.example.evsecondhand.data.remote.SellerApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    const val BASE_URL = "https://beevmarket-production.up.railway.app/api/v1/"
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var tokenProvider: (() -> String?)? = null

    @Volatile
    private var cachedToken: String? = null

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
        cachedToken = provider()?.takeUnless { it.isNullOrBlank() }
    }

    fun updateAccessToken(token: String?) {
        cachedToken = token?.takeUnless { it.isBlank() }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = cachedToken ?: tokenProvider?.invoke()
        val authorisedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        chain.proceed(authorisedRequest)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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
    val auctionApi: AuctionApiService = retrofit.create(AuctionApiService::class.java)
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val chatbotApi: ChatbotApiService = retrofit.create(ChatbotApiService::class.java)
    val checkoutApi: CheckoutApiService = retrofit.create(CheckoutApiService::class.java)
    val walletApi: WalletApiService = retrofit.create(WalletApiService::class.java)
    val purchaseApi: PurchaseApiService = retrofit.create(PurchaseApiService::class.java)
    val sellerApi: SellerApiService = retrofit.create(SellerApiService::class.java)
}
