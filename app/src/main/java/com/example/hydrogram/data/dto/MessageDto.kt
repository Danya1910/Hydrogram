package com.example.hydrogram.data.dto

import com.example.hydrogram.domain.model.ReplyData

data class MessageDto(
    val messageId: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: String = "",
    val type: String = "",
    val replyData: ReplyData? = null,
    val text: String? = null,
    val stickerPath: String? = null,
    val image: String? = null,
)
