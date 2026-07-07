package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.model.User

data class ChatAndUserUiState (
    val chat: Chat,
    val user: User?,
)