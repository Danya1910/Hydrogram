package com.example.hydrogram.data.dto

import com.example.hydrogram.domain.model.ReplyData

data class MessageDto(
    val messageId: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: String = "",
    val type: String = "",
    val reactions: Map<String, String>? = null,
    val replyData: ReplyData? = null,
    val isEdited: Boolean = false,
    val text: String? = null,
    val stickerPath: String? = null,
    val image: String? = null,
)
