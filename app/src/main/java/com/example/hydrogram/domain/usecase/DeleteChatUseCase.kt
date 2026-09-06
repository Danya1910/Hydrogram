package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.InboxRepository
import javax.inject.Inject

class DeleteChatUseCase @Inject constructor(
    private val inboxRepository: InboxRepository,
){

    suspend operator fun invoke(
        chatId: String,
    ): Result<Unit> {
        return inboxRepository.deleteChat(
            chatId = chatId,
        )
    }

}