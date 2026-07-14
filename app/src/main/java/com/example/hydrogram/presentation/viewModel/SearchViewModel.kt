package com.example.hydrogram.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.usecase.FindUserByPhoneUseCase
import com.example.hydrogram.presentation.states.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel(
    private val findUserByPhoneUseCase: FindUserByPhoneUseCase,
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Loading)
    val searchState = _searchState.asStateFlow()

    fun searchByPhone(
        phone: String
    ) {
        if(phone.isBlank()) {
            _searchState.value = SearchState.Error("Нет номера")
        }
        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            try {
                val user = findUserByPhoneUseCase(phone = phone)
                if(user != null) {
                    _searchState.value = SearchState.Success(user = user)
                } else {
                    _searchState.value = SearchState.Error("Пользователь не найден")
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(
                    e.localizedMessage ?: "Ошибка при поиске"
                )
            }
        }
    }

    fun resetSearch() {
        _searchState.value = SearchState.Success(
             user = null
        )
        _searchState.value = SearchState.Error(
            message = ""
        )
    }

}