package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.Message

sealed interface ChatUiState {
    object Loading: ChatUiState
    data class Success(val messages: List<Message>): ChatUiState
    data class Error(val message: String) : ChatUiState
}