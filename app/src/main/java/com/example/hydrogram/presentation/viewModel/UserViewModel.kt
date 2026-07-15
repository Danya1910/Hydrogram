package com.example.hydrogram.presentation.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.usecase.GetCurrentUserIdUseCase
import com.example.hydrogram.domain.usecase.GetUserByIdUseCase
import com.example.hydrogram.domain.usecase.SaveUserProfileUseCase
import com.example.hydrogram.domain.usecase.SetUserOnlineStatsUseCase
import com.example.hydrogram.presentation.states.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val setUserOnlineStatsUseCase: SetUserOnlineStatsUseCase,
    uid: String,
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSaving = mutableStateOf(false)
    val isSaving = _isSaving.value

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    init {
        observeUser(uid = uid)
    }

    private fun observeUser(
        uid: String,
    ) {
        if (uid.isBlank()) {
            _userState.value = UserState.Error("Пользователь не найден")
            return
        }
        viewModelScope.launch {
            getUserByIdUseCase(uid = uid)
                .catch { exception ->
                    _userState.value = UserState.Error(
                        exception.localizedMessage ?: "Ошибка получения данных пользователя"
                    )
                }
                .collect { user ->
                    _userState.value = UserState.Success(user = user)
                }
        }

    }


    fun saveProfile(
        uid: String,
        name: String,
        avatarUrl: String,
        email: String,
        isOnline: Boolean,
        createdAt: Long,
    ) {
        if(uid.isBlank()) {
            _errorMessage.value = "Пользователь не найден"
            return
        }
        if(_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _isLoading.value = true
            val result = saveUserProfileUseCase(
                uid = uid,
                name = name,
                avatarUrl = avatarUrl,
                email = email,
                isOnline = isOnline,
                createdAt = System.currentTimeMillis()
            )
            _isSaving.value = false
            _isLoading.value = false

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = "Ошибка обновления данных пользователя" }
        }
    }

    fun setUserOnlineStats(
        isOnline: Boolean,
        uid: String,
    ) {
        viewModelScope.launch {
            val result = setUserOnlineStatsUseCase(
                uid = uid,
                isOnline = isOnline,
            )
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = "Ошибка смены состояния в сети" }
        }
    }

}