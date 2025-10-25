package com.example.evsecondhand.ui.screen.chatbot

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evsecondhand.ui.theme.PrimaryGreen
import com.example.evsecondhand.ui.viewmodel.ChatbotViewModel

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatbotWidget(
    viewModel: ChatbotViewModel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Chat window
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Card(
                modifier = Modifier
                    .width(350.dp)
                    .height(500.dp)
                    .padding(bottom = 70.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Surface(
                        color = PrimaryGreen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = "Chatbot",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "EV Assistant",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Trợ lý ảo của bạn",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            IconButton(onClick = { isExpanded = false }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.SmartToy,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = PrimaryGreen.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Xin chào! Tôi có thể giúp gì cho bạn?",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Hỏi tôi về xe điện, pin EV...",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        items(messages) { message ->
                            ChatMessageBubble(message)
                        }

                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF0F0F0)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = PrimaryGreen
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Đang trả lời...",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Nhập câu hỏi...") },
                                enabled = !isLoading,
                                maxLines = 3,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText.trim())
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = PrimaryGreen,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.size(56.dp),
            containerColor = PrimaryGreen,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Chat,
                contentDescription = if (isExpanded) "Close Chat" else "Open Chat",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (message.isUser) 12.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 12.dp
            ),
            color = if (message.isUser) PrimaryGreen else Color(0xFFF0F0F0)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = if (message.isUser) Color.White else Color.Black
            )
        }
    }
}
