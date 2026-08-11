package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke() {
        userRepository.logOut()
    }

}