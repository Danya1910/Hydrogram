package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface InboxRepository {

    fun getInboxChats(
        userId: String
    ) : Flow<List<Chat>>

}