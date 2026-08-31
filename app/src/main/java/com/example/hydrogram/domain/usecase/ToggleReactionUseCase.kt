package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject

class ToggleReactionUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        reaction: String?,
        chatId: String,
        messageId: String,
    ): Result<Unit> {
        return chatRepository.toggleReaction(
            reaction = reaction,
            chatId = chatId,
            messageId = messageId,
        )
    }

}