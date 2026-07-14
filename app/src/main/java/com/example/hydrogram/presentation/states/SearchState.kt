package com.example.hydrogram.presentation.states

import com.example.hydrogram.domain.model.User

sealed interface SearchState {
    object Loading : SearchState
    data class Success(val user: User?) : SearchState
    data class Error(val message: String) : SearchState
}