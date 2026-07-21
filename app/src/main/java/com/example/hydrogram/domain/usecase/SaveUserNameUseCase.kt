package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        uid: String,
        userName: String,
    ) : Result<Unit> {
        return userRepository.saveUserName(
            uid = uid,
            userName = userName,
        )
    }

}