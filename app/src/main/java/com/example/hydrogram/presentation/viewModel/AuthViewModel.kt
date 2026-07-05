package com.example.hydrogram.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.usecase.SignInUseCase
import com.example.hydrogram.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    fun signIn(
        email: String,
        password: String,
    ) {
        if(email.isBlank() || password.isBlank()) {
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

    fun signUp(
        email: String,
        password: String,
        name: String,
    ) {
        if(email.isBlank() || password.isBlank() || name.isBlank()) {
            _errorMessage.value = "Заполните поля"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = signUpUseCase(
                email = email,
                password = password,
                name = name,
            )
            _isLoading.value = false


            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка регистрации" }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }


}