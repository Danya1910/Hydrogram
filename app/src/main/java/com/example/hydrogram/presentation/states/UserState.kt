package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.User

sealed interface UserState {
    object Loading : UserState
    data class Success(val user: User?) : UserState
    data class Error(val message: String) : UserState
}

sealed interface MineState {
    object Loading : MineState
    data class Success(val user: User?) : MineState
    data class Error(val message: String) : MineState
}