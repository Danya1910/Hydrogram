package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        phone: String,
    ) : Result<Unit> {
        return authRepository.signUp(
            email = email,
            password = password,
            name = name,
            phone = phone,
        )
    }

}