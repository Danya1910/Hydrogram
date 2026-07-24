package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject

class ChangeMessageStatusUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        chatId: String,
        messageId: String,
        status: String,
    ) : Result<Unit> {
        return chatRepository.changeMessageState(
            chatId = chatId,
            messageId = messageId,
            status = status,
        )
    }

}