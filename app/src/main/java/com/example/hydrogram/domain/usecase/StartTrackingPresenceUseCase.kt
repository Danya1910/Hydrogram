package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.PresenceRepository
import javax.inject.Inject

class StartTrackingPresenceUseCase @Inject constructor(
    private val presenceRepository: PresenceRepository,
) {

    operator fun invoke(
        uid: String
    ) {
        return presenceRepository.startTrackingPresence(uid = uid)
    }

}