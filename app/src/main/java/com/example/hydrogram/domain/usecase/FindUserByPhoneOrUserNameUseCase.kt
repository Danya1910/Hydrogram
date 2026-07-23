package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class FindUserByPhoneOrUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository,
){

    suspend operator fun invoke(
        query: String
    ) : User? {
        return userRepository.findUserByPhoneOrUserName(
            query = query,
        )
    }

}