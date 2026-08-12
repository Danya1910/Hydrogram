package com.example.hydrogram.data.dto

data class MessageDto(
    val messageId: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: String = "",
    val type: String = "",
    val text: String? = null,
    val stickerPath: String? = null
)
