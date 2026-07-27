package com.example.hydrogram.presentation.viewModel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.model.UserPresence
import com.example.hydrogram.domain.usecase.ChangeAvatarUseCase
import com.example.hydrogram.domain.usecase.GetCurrentUserIdUseCase
import com.example.hydrogram.domain.usecase.GetUserByIdUseCase
import com.example.hydrogram.domain.usecase.ObserveUserPresenceUseCase
import com.example.hydrogram.domain.usecase.SaveUserNameUseCase
import com.example.hydrogram.domain.usecase.SaveUserProfileUseCase
import com.example.hydrogram.domain.usecase.SetUserOnlineStatsUseCase
import com.example.hydrogram.presentation.states.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val setUserOnlineStatsUseCase: SetUserOnlineStatsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val saveUserNameUseCase: SaveUserNameUseCase,
    private val observeUserPresenceUseCase: ObserveUserPresenceUseCase,
    private val changeAvatarUseCase: ChangeAvatarUseCase,
) : ViewModel() {

    private val _targetUserId = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    val userState: StateFlow<UserState> = _targetUserId
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(UserState.Loading)
            } else {
                getUserByIdUseCase(uid = uid)
                    .map { user -> UserState.Success(user = user) as UserState }
                    .catch { emit(UserState.Error(it.localizedMessage ?: "Ошибка")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserState.Loading
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSaving = mutableStateOf(false)
    val isSaving = _isSaving.value

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _currentId = MutableStateFlow("")
    val currentId = _currentId.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<Unit>?>(null)
    val saveResult = _saveResult.asStateFlow()

    fun setTargetUserId(uid: String) {
        if (_targetUserId.value != uid) {
            _targetUserId.value = uid
        }
    }

    fun getCurrentUserId() {
        viewModelScope.launch {
            val result = getCurrentUserIdUseCase()
            if (!result.isNullOrEmpty()) {
                _currentId.value = result
            } else return@launch
        }
    }

    fun saveProfile(
        uid: String,
        name: String,
        avatarUrl: String,
        email: String,
        isOnline: Boolean,
        createdAt: Long,
        aboutUser: String,
        birthdayDate: String,
        userName: String,
        phone: String,
    ) {
        if (uid.isBlank()) {
            _errorMessage.value = "Пользователь не найден"
            return
        }
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _isLoading.value = true
            _isSuccess.value = false
            val result = saveUserProfileUseCase(
                uid = uid,
                name = name,
                avatarUrl = avatarUrl,
                email = email,
                isOnline = isOnline,
                createdAt = System.currentTimeMillis(),
                birthdayDate = birthdayDate,
                aboutUser = aboutUser,
                userName = userName,
                phone = phone,
            )
            _isSaving.value = false
            _isLoading.value = false

            _saveResult.value = result

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = "Ошибка обновления данных пользователя" }
        }
    }

    fun saveUserName(
        uid: String,
        userName: String,
    ) {
        if (uid.isBlank()) {
            _errorMessage.value = "Пользователь не найден"
            return
        }
        if(_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            _isLoading.value = true
            _isSuccess.value = false
            val result = saveUserNameUseCase(
                uid = uid,
                userName = userName,
            )
            _isSaving.value = false
            _isLoading.value = false

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = "Ошибка обновления userName" }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val opponentPresenceState: StateFlow<UserPresence> = _targetUserId
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                kotlinx.coroutines.flow.flowOf(UserPresence(isOnline = false, lastSeen = 0L))
            } else {
                observeUserPresenceUseCase(userId = uid)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPresence(isOnline = false, lastSeen = 0L)
        )

    fun changeAvatar(
        uid: String,
        imageUri: Uri,
    ) {
        if(uid.isBlank()) {
            _errorMessage.value = "Пользователь не найден"
            return
        }
        if(_isSaving.value) return
        viewModelScope.launch {

            _isSaving.value = true
            _isLoading.value = true
            _isSuccess.value = false

            val result = changeAvatarUseCase(
                uid = uid,
                imageUri = imageUri
            )

            _isSaving.value = false
            _isLoading.value = false

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = "Ошибка обновления аватара" }
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

    fun resetSaveResult() {
        _saveResult.value = null
    }

}