package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        senderId: String,
        chatId: String,
        text: String,
        type: String = "text",
    ) : Result<Unit> {
        return chatRepository.sendMessage(
            senderId = senderId,
            chatId = chatId,
            text = text,
            type = type,
        )
    }

}