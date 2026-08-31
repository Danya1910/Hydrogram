package com.example.hydrogram.presentation.viewModel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.domain.usecase.ChangeMessageStatusUseCase
import com.example.hydrogram.domain.usecase.GetChatHistoryUseCase
import com.example.hydrogram.domain.usecase.GetCurrentUserIdUseCase
import com.example.hydrogram.domain.usecase.SendMessageUseCase
import com.example.hydrogram.domain.usecase.ToggleReactionUseCase
import com.example.hydrogram.presentation.states.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.enums.enumEntries


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val changeMessageStatusUseCase: ChangeMessageStatusUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSending = mutableStateOf(false)
    val isSending = _isSending

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()


    private val _currentId = MutableStateFlow("")
    val currentId = _currentId.asStateFlow()

    private val updatingMessageIds = mutableSetOf<String>()

    fun sendText(
        senderId: String,
        chatId: String,
        text: String,
        replyData: ReplyData? = null,
    ) {
        if (text.isBlank()) {
            _errorMessage.value = "Пустое сообщение"
            return
        }
        if (_isSending.value) {
            return
        }

        Log.d("ChatVM", "sent text message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                content = text,
                messageType = "text",
                replyData = replyData,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent text message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun sendSticker(
        senderId: String,
        chatId: String,
        stickerPath: String,
        replyData: ReplyData? = null,
    ) {
        if(stickerPath.isBlank()) {
            _errorMessage.value = "Пустой Стикер"
            return
        }
        if(_isSending.value) {
            return
        }
        Log.d("ChatVM", "relay data : $replyData")
        Log.d("ChatVM", "sent sticker message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                content = stickerPath,
                messageType = "sticker",
                replyData = replyData,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent sticker message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun sendImage(
        senderId: String,
        chatId: String,
        imageUri: Uri,
        replyData: ReplyData? = null,
    ) {
        if(_isSending.value) {
            return
        }
        Log.d("ChatVM", "sent image message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                messageType = "image",
                imageUri = imageUri,
                replyData = replyData,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent image message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun toggleReaction(
        reaction: String?,
        chatId: String,
        messageId: String,
    ) {

        if(chatId.isEmpty() || messageId.isEmpty()) {
            return
        }

        if(_isSending.value) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true

            val result = toggleReactionUseCase(
                reaction = reaction,
                chatId = chatId,
                messageId = messageId,
            )

            _isSending.value = false

            Log.d("ChatVM", "toggle reaction result: $result")

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка изменения реакции" }

        }
    }

    fun changeMessageStatus(
        chatId: String,
        messageId: String,
        status: String,
    ) {
        if (updatingMessageIds.contains(messageId)) {
            return
        }
        if (chatId.isBlank() || messageId.isBlank()) {
            _errorMessage.value = "Такого сообщения нет"
            return
        }
        updatingMessageIds.add(messageId)
        viewModelScope.launch {
            _isSending.value = true
            val result = changeMessageStatusUseCase(
                chatId = chatId,
                messageId = messageId,
                status = status,
            )
            _isSending.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure {
                    _errorMessage.value = it.localizedMessage ?: ("Ошибка изменения" +
                            " статуса сообщения")
                    updatingMessageIds.remove(messageId)
                }
        }
    }

    fun resetSendStatus() {
        _isSuccess.value = false
    }

    fun getCurrentUserId() {
        viewModelScope.launch {
            val result = getCurrentUserIdUseCase()
            if (!result.isNullOrEmpty()) {
                _currentId.value = result
            } else return@launch
        }
    }

    fun observeChatHistory(
        chatId: String,
    ) {
        if (chatId.isBlank()) {
            _uiState.value = ChatUiState.Error("Чат не найден")
            return
        }
        viewModelScope.launch {
            getChatHistoryUseCase(
                chatId = chatId
            ).catch { exception ->
                _uiState.value = ChatUiState.Error(
                    exception.localizedMessage ?: "Не удалось загрузить сообщения"
                )
            }
                .collect { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
        }
    }

}