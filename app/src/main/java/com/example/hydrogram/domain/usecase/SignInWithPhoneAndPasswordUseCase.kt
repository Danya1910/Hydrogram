package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithPhoneAndPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    suspend operator fun invoke(
        phone: String,
        password: String,
    ): Result<Unit> {
        return authRepository.signInWithPhoneAndPassword(
            phone = phone,
            password = password,
        )
    }

}