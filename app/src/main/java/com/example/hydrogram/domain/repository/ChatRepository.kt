package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun sendMessage(
        senderId: String,
        chatId: String,
        message: Message,
    ) : Result<Unit>

    fun getChatHistory(chatId: String) : Flow<List<Message>>

    suspend fun changeMessageStatus(
        chatId: String,
        messageId: String,
        status: String,
    ) : Result<Unit>

    suspend fun toggleReaction(
        reaction: String?,
        chatId: String,
        messageId: String,
    ) : Result<Unit>

    suspend fun deleteMessage(
        chatId: String,
        messageId: String,
    ) : Result<Unit>


}