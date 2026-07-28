package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.UserPresence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface PresenceRepository {

    fun startTrackingPresence(uid: String)

    fun observeUserPresence(userId: String) : Flow<UserPresence>

    fun observeMultiplePresence(uids: List<String>) : Flow<Map<String, UserPresence>>

}