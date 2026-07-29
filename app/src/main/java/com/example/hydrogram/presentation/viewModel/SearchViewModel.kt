package com.example.hydrogram.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.model.RegisteredContact
import com.example.hydrogram.domain.usecase.FindUsersByPhoneOrUserNameUseCase
import com.example.hydrogram.domain.usecase.GetPhoneContactsUseCase
import com.example.hydrogram.domain.usecase.ObserveMultiplePresenceUseCase
import com.example.hydrogram.domain.usecase.SyncContactsUseCase
import com.example.hydrogram.presentation.states.SearchState
import com.example.hydrogram.presentation.util.normalizePhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val findUserByPhoneOrUserNameUseCase: FindUsersByPhoneOrUserNameUseCase,
    private val getPhoneContactsUseCase: GetPhoneContactsUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val observeMultiplePresenceUseCase: ObserveMultiplePresenceUseCase,
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Loading)
    val searchState = _searchState.asStateFlow()

    private val _registeredContacts = MutableStateFlow<List<RegisteredContact>>(emptyList())

    fun searchByPhoneOrUserName(
        query: String
    ) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Error("Нет запроса")
            return
        }
        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            try {
                val users = findUserByPhoneOrUserNameUseCase(query = query)
                if (users != null) {
                    _searchState.value = SearchState.Success(users = users)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val registeredContact: StateFlow<List<RegisteredContact>> = _registeredContacts
        .flatMapLatest { contacts ->
            if (contacts.isEmpty()) {
                flowOf(emptyList())
            } else {
                val uids = contacts.map { it.user.uid }

                combine(
                    flowOf(contacts),
                    observeMultiplePresenceUseCase(uids = uids)
                ) { baseList, onlineMap ->
                    baseList.map { contact ->
                        val livePresence = onlineMap[contact.user.uid]
                        contact.copy(
                            user = contact.user.copy(
                                isOnline = livePresence?.isOnline ?: false,
                                lastSeen = livePresence?.lastSeen ?: 0L
                            )
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun syncContacts() {
        if (_registeredContacts.value.isNotEmpty()) return

        viewModelScope.launch {
            val contacts = getPhoneContactsUseCase()
            Log.d("SearchVM", "contacts: $contacts")
            if (contacts.isEmpty()) return@launch

            val numberList = contacts.map { it.phone }

            val firebaseUsers = syncContactsUseCase(phoneNumbers = numberList)
            Log.d("SearchVM", "firebase users: $firebaseUsers")

            val finalSyncedList = firebaseUsers.mapNotNull { firebaseUser ->
                val matchingLocalContact = contacts.find {
                    normalizePhoneNumber(rawPhone = it.phone) == firebaseUser.phone
                }

                if (matchingLocalContact != null) {
                    RegisteredContact(
                        user = firebaseUser,
                        contactName = matchingLocalContact.name,
                    )
                } else {
                    null
                }
            }.sortedBy { it.contactName }

            Log.d("SearchVM", "final Synced list: $finalSyncedList")
            _registeredContacts.value = finalSyncedList
        }
    }

    fun resetSearch() {
        _searchState.value = SearchState.Success(
            users = emptyList()
        )
        _searchState.value = SearchState.Error(
            message = ""
        )
    }

}