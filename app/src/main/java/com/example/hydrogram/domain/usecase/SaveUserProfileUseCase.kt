package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        uid: String,
        name: String,
        avatarUrl: String,
        email: String,
        isOnline: Boolean,
        createdAt: Long,
    ) : Result<Unit> {
        val user = User(
            uid = uid,
            name = name,
            avatarUrl = avatarUrl,
            email = email,
            isOnline = isOnline,
            createdAt = createdAt,
        )
        return userRepository.saveUserProfile(user = user)
    }

}