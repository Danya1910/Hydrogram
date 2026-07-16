package com.example.hydrogram.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.usecase.GetCurrentUserIdUseCase
import com.example.hydrogram.domain.usecase.GetInboxChatsUseCase
import com.example.hydrogram.presentation.states.InboxUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getInboxChatsUseCase: GetInboxChatsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
) : ViewModel()
{

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentId = MutableStateFlow("")
    val currentId = _currentId.asStateFlow()


    fun getCurrentUserId() {
        viewModelScope.launch {
            val result = getCurrentUserIdUseCase()
            if(!result.isNullOrEmpty()) {
                _currentId.value = result
            }
            else return@launch
        }
    }

    fun observeInboxChats(
        userId: String,
    ) {
        if (userId.isBlank()) {
            _uiState.value = InboxUiState.Error("Пользователь не авторизирован")
            return
        }
        viewModelScope.launch {
            getInboxChatsUseCase(userId = userId)
                .catch { exception ->
                    _uiState.value = InboxUiState.Error(
                        exception.localizedMessage ?: "Не удалось загрузить список чатов"
                    )
                }
                .collect { chats ->
                    _uiState.value = InboxUiState.Success(chats = chats)
                }
        }
    }
}