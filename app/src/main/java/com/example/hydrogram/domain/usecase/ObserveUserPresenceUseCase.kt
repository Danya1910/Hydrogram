package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.UserPresence
import com.example.hydrogram.domain.repository.PresenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserPresenceUseCase @Inject constructor(
    private val presenceRepository: PresenceRepository,
) {

    operator fun invoke(
        userId: String
    ): Flow<UserPresence> {
        return presenceRepository.observeUserPresence(userId = userId)
    }

}