package com.example.evsecondhand.data.remote

import com.example.evsecondhand.data.model.ChatbotRequest
import com.example.evsecondhand.data.model.ChatbotResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApiService {
    
    @POST("chatbot/")
    suspend fun askQuestion(@Body request: ChatbotRequest): ChatbotResponse
}
