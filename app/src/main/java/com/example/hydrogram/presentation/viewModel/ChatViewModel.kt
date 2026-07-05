package com.example.hydrogram.presentation.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.usecase.GetChatHistoryUseCase
import com.example.hydrogram.domain.usecase.SendMessageUseCase
import com.example.hydrogram.presentation.states.ChatScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    chatId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatScreenState>(ChatScreenState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSending = mutableStateOf(false)
    val isSending = _isSending

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    init{
        observeChatHistory(
            chatId = chatId
        )
    }

    fun sendMessage(
        senderId: String,
        chatId: String,
        text: String,
        type: String,
    ) {
        if(text.isBlank()) {
            _errorMessage.value = "Пустое сообщение"
            return
        }
        if(_isSending.value) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                text = text,
                type = type,
            )
            _isSending.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    private fun observeChatHistory(
        chatId: String,
    ) {
        if(chatId.isBlank())
        {
            _uiState.value = ChatScreenState.Error("Чат не найден")
            return
        }
        viewModelScope.launch {
            getChatHistoryUseCase(
                chatId = chatId
            ).catch { exception ->
                _uiState.value = ChatScreenState.Error(
                    exception.localizedMessage ?: "Не удалось загрузить сообщения"
                )
            }
                .collect { messages ->
                _uiState.value = ChatScreenState.Success(messages)
            }
        }
    }

}