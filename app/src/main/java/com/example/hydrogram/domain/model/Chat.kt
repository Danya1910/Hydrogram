package com.example.hydrogram.domain.model

data class Chat(
    val senderId: String = "",
    val chatId: String = "",
    val lastMessage: String = "",
    val lastMessageType: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
)
