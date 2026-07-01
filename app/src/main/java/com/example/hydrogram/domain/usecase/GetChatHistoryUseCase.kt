package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    operator fun invoke(
        chatId: String,
    ) : Flow<List<Message>> {
        return chatRepository.getChatHistory(chatId = chatId)
    }

}