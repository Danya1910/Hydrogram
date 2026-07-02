package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    operator fun invoke(
    ) : String? {
        return authRepository.getCurrentUserId()
    }

}