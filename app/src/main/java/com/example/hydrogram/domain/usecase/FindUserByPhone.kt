package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindUserByPhone @Inject constructor(
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