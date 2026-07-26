package com.example.hydrogram.domain.usecase

import android.util.Log
import com.example.hydrogram.domain.repository.PresenceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartTrackingPresenceUseCase @Inject constructor(
    private val presenceRepository: PresenceRepository,
) {

    private var isTrackingActive = false
    private var currentTrackingUid: String? = null

    operator fun invoke(
        uid: String
    ) {
        if (isTrackingActive && currentTrackingUid == uid) {
            Log.d("PresenceUseCase", "Трекинг уже работает для $uid, игнорируем повторный вызов")
            return
        }

        if (currentTrackingUid != null && currentTrackingUid != uid) {
            Log.d("PresenceUseCase", "Пользователь сменился! Перезапускаем трекинг.")
        }

        isTrackingActive = true
        currentTrackingUid = uid

        Log.d("PresenceUseCase", "РЕАЛЬНЫЙ ЗАПУСК ТРЕКИНГА В FIREBASE для: $uid")
        presenceRepository.startTrackingPresence(uid = uid)
    }

}