package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        phoneNumbers: List<String>,
    ): List<User> {
        return userRepository.syncContacts(
            phoneNumbers = phoneNumbers,
        )
    }

}