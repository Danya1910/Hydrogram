package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.repository.InboxRepository
import com.example.hydrogram.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetInboxChatsUseCase @Inject constructor(
    private val inboxRepository: InboxRepository,
    private val userRepository: UserRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        userId: String
    ): Flow<List<Chat>> {
        return inboxRepository.getInboxChats(userId = userId)
            .flatMapLatest { chats ->
                if (chats.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows: List<Flow<Chat>> = chats.map { chat ->
                        val penpalId = penpalIdOf(chat = chat, mineId = userId)
                        if (penpalId.isBlank()) {
                            flowOf(chat)
                        } else {
                            userRepository.getUserById(uid = penpalId)
                                .map { user -> chat.copy(user = user) }
                        }
                    }
                    combine(flows) { it.toList() }
                }
            }
            .distinctUntilChanged()
    }

    private fun penpalIdOf(chat: Chat, mineId: String): String {
        val parts = chat.chatId.split("_")
        return parts.firstOrNull { it != mineId }
            ?: parts.firstOrNull()
            ?: ""
    }

}