package com.example.hydrogram.domain.model

data class Notification (
    val targetUserId: String? = null,
    val chatId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val message: String? = null,
    val messageType: String? = null,
    val timeStamp: String? = null,
    val status: String? = null,

)