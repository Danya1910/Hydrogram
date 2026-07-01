package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.UserRepository
import javax.inject.Inject

class SetUserOnlineStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        uid: String,
        isOnline: Boolean,
    ): Result<Unit> {
        return userRepository.setUserOnlineStats(uid = uid, isOnline = isOnline)
    }

}