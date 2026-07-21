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
        phone: String,
        isOnline: Boolean,
        createdAt: Long,
        aboutUser: String,
        birthdayDate: String,
        userName: String,
    ) : Result<Unit> {
        val user = User(
            uid = uid,
            name = name,
            avatarUrl = avatarUrl,
            email = email,
            phone = phone,
            isOnline = isOnline,
            createdAt = createdAt,
            aboutUser = aboutUser,
            birthdayDate = birthdayDate,
            userName = userName,
        )
        return userRepository.saveUserProfile(user = user)
    }

}