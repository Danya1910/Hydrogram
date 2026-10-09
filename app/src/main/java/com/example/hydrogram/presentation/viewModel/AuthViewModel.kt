package com.example.hydrogram.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.usecase.CheckPhoneRegistrationUseCase
import com.example.hydrogram.domain.usecase.SignInUseCase
import com.example.hydrogram.domain.usecase.SignInWithPhoneAndPasswordUseCase
import com.example.hydrogram.domain.usecase.SignUpUseCase
import com.example.hydrogram.presentation.util.AuthData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithPhoneAndPasswordUseCase: SignInWithPhoneAndPasswordUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val checkPhoneRegistrationUseCase: CheckPhoneRegistrationUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRegistered = MutableStateFlow<Boolean?>(null)
    val isRegistered = _isRegistered.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _authData = MutableStateFlow(AuthData())
    val authData = _authData.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun signIn(
        email: String,
        password: String,
    ) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Заполните поля"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = signInUseCase(email = email, password = password)
            _isLoading.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка входа" }
        }
    }

    fun signInWithPhoneAndPassword(
        phone: String,
        password: String,
    ) {
        if(_isLoading.value) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = signInWithPhoneAndPasswordUseCase(
                phone = phone,
                password = password,
            )
            _isLoading.value = false

            result
                .onSuccess {
                    _isSuccess.value = true
                    _passwordError.value = null
                }
                .onFailure { throwable ->
                    _passwordError.value = mapAuthError(throwable)
                    Log.d("Auth", _passwordError.toString())
                }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _errorMessage.value = "Заполните поля"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = signUpUseCase(
                email = email,
                password = password,
                name = name,
                phone = phone,
            )
            _isLoading.value = false


            result
                .onSuccess {
                    _isSuccess.value = true
                    _passwordError.value = null
                }
                .onFailure { throwable ->
                    _passwordError.value = mapAuthError(throwable)
                    Log.d("Auth", _passwordError.toString())
                }
        }
    }

    fun checkPhoneRegister(
        phone: String,
    ) {
        if(_isLoading.value) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true

            val result = checkPhoneRegistrationUseCase(
                phone = phone,
            )

            _isLoading.value = false

            _isRegistered.value = result
        }
    }

    fun savePhone(
        phone: String
    ) {
        _authData.update {
            it.copy(
                phone = phone,
            )
        }
    }

    fun saveEmail(
        email: String
    ) {
        _authData.update {
            it.copy(
                email = email,
            )
        }
    }

    fun saveName(
        name: String
    ) {
        _authData.update {
            it.copy(
                name = name,
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearPasswordError() {
        _passwordError.value = null
    }

    private fun mapAuthError(throwable: Throwable): String {
        if (throwable is FirebaseAuthException) {
            Log.d("Auth", "errorCode = ${throwable.errorCode}, msg = ${throwable.message}")
        }

        return when (throwable) {

            is FirebaseAuthInvalidCredentialsException -> {
                when (throwable.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Некорректный email"
                    "ERROR_INVALID_CREDENTIAL" -> "Неверный пароль"
                    "ERROR_WRONG_PASSWORD" -> "Неверный пароль"
                    "ERROR_INVALID_PHONE_NUMBER" -> "Некорректный номер телефона"
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Неверный код подтверждения"
                    else -> "Неверные данные для входа"
                }
            }

            is FirebaseAuthInvalidUserException -> {
                when (throwable.errorCode) {
                    "ERROR_USER_DISABLED" -> "Аккаунт заблокирован"
                    "ERROR_USER_NOT_FOUND" -> "Пользователь не найден"
                    "ERROR_USER_TOKEN_EXPIRED" -> "Сессия истекла. Войдите заново"
                    else -> "Ошибка аккаунта"
                }
            }

            is FirebaseAuthWeakPasswordException ->
                "Пароль слишком простой. Минимум 6 символов"

            is FirebaseAuthUserCollisionException ->
                "Этот email уже используется"

            is FirebaseNetworkException ->
                "Проверьте подключение к интернету"

            else ->
                throwable.localizedMessage ?: "Не удалось войти. Попробуйте позже"
        }
    }

}