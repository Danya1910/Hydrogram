package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class FindUsersByPhoneOrUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository,
){

    suspend operator fun invoke(
        query: String
    ) : List<User> {
        return userRepository.findUsersByPhoneOrUserName(
            query = query,
        )
    }

}