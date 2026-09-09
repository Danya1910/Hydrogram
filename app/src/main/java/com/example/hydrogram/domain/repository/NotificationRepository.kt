package com.example.hydrogram.domain.repository

interface NotificationRepository {
    suspend fun sendPushNotification(
        targetUserId: String,
        senderName: String,
        messageText: String,
        chatId: String,
        avatarBase64: String
    ): Result<Unit>
}