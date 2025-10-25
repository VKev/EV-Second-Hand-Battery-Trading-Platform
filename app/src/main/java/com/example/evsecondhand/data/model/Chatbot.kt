package com.example.evsecondhand.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatbotRequest(
    val question: String
)

@Serializable
data class ChatbotResponse(
    val answer: String
)
