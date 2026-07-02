package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    operator fun invoke() : Boolean {
        return authRepository.isUserLoggedIn()
    }

}