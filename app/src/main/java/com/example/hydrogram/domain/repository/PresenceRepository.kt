package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.UserPresence
import kotlinx.coroutines.flow.Flow

interface PresenceRepository {

    fun startTrackingPresence(uid: String)

    fun observeUserPresence(userId: String) : Flow<UserPresence>

}