package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun sendMessage(
        senderId: String,
        chatId: String,
        text: String,
        type: String,
    ) : Result<Unit>

    suspend fun getChat(chatId: String) : Flow<List<Message>>

}