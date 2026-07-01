package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInboxChats @Inject constructor(
    private val inboxRepository: InboxRepository,
) {

    operator fun invoke(
        userId: String
    ): Flow<List<Chat>> {
        return inboxRepository.getInboxChats(userId = userId)
    }

}