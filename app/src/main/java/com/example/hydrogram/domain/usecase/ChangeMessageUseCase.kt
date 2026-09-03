package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject

class ChangeMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        chatId: String,
        messageId: String,
        currentMessageType: String,
        typeOfChange: String,
        change: String,
    ): Result<Unit> {
        return chatRepository.changeMessage(
            chatId = chatId,
            messageId = messageId,
            currentMessageType = currentMessageType,
            typeOfChange = typeOfChange,
            change = change,
        )
    }

}