package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class FindUserByPhoneUseCase @Inject constructor(
    private val userRepository: UserRepository,
){

    suspend operator fun invoke(
        phone: String
    ) : User? {
        return userRepository.findUserByPhone(
            phone = phone,
        )
    }

}