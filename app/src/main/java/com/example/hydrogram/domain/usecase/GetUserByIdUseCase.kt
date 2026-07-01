package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    operator fun invoke(
        uid: String,
    ) : Flow<User?> {
        return  userRepository.getUserById(uid = uid)
    }

}