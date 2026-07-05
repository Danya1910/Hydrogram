package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.Message

sealed interface ChatScreenState {
    object Loading: ChatScreenState
    data class Success(val messages: List<Message>): ChatScreenState
    data class Error(val message: String) : ChatScreenState
}