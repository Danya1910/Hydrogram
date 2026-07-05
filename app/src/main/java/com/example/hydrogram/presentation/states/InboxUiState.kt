package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.Chat

sealed interface InboxUiState {
    object Loading: InboxUiState
    data class Success(val chats: List<Chat>): InboxUiState
    data class Error(val message: String) : InboxUiState
}