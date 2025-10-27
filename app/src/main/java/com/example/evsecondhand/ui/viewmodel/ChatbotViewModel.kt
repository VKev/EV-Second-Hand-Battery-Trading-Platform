package com.example.evsecondhand.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evsecondhand.data.model.ChatbotRequest
import com.example.evsecondhand.data.remote.RetrofitClient
import com.example.evsecondhand.ui.screen.chatbot.ChatMessage
import com.example.evsecondhand.utils.ChatbotFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatbotViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ChatbotViewModel"
    }
    
    private val chatbotApi = RetrofitClient.chatbotApi
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun sendMessage(question: String) {
        if (question.isBlank()) return
        
        // Add user message
        val userMessage = ChatMessage(text = question, isUser = true)
        _messages.value = _messages.value + userMessage
        
        // Send to API
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Sending question: $question")
                
                val response = chatbotApi.askQuestion(ChatbotRequest(question))
                Log.d(TAG, "Received answer: ${response.answer}")
                
                // Parse links from response
                val links = ChatbotFormatter.parseVehicleLinks(response.answer)
                
                // Clean text (remove URLs and format)
                val cleanText = ChatbotFormatter.getCleanText(response.answer)
                
                // Add bot response with links
                val botMessage = ChatMessage(
                    text = cleanText,
                    isUser = false,
                    links = links
                )
                _messages.value = _messages.value + botMessage
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                val errorMessage = ChatMessage(
                    text = "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChat() {
        _messages.value = emptyList()
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ChatbotViewModel cleared - cancelling all coroutines")
    }
}
