package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        chatId: String,
        messageId: String,
    ) : Result<Unit> {
        return chatRepository.deleteMessage(
            chatId = chatId,
            messageId = messageId,
        )
    }

}