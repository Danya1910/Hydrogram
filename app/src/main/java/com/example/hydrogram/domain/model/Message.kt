package com.example.hydrogram.domain.model

data class Message (
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = "text",
    val timestamp: Long = 0L,
    val status: String = "sent"
)