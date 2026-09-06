package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import javax.inject.Inject

class CheckPhoneRegistrationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        phone: String
    ): Boolean {
        return authRepository.checkPhoneRegistration(
            phone = phone,
        )
    }

}