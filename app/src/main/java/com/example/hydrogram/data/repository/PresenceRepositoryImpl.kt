package com.example.hydrogram.data.repository

import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot
import com.example.hydrogram.domain.model.UserPresence
import com.example.hydrogram.domain.repository.PresenceRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PresenceRepositoryImpl @Inject constructor(
    private val rtdb: FirebaseDatabase
) : PresenceRepository {

    override fun startTrackingPresence(uid: String) {
        val userStatusRef = rtdb.getReference("/status/$uid")
        val connectRef = rtdb.getReference(".info/connected")

        connectRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false

                if (connected) {

                    userStatusRef.onDisconnect().setValue(
                        mapOf(
                            "isOnline" to false,
                            "lastSeen" to ServerValue.TIMESTAMP
                        )
                    )

                    userStatusRef.setValue(
                        mapOf(
                            "isOnline" to true,
                            "lastSeen" to ServerValue.TIMESTAMP
                        )
                    )

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("PresenceRepository", "error: ${error.message}")
            }

        })

    }

    override fun observeUserPresence(userId: String): Flow<UserPresence> {
        return callbackFlow {
            val userStatusRef = rtdb.getReference("/status/$userId")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                    val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L

                    trySend(UserPresence(isOnline = isOnline, lastSeen = lastSeen))
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            userStatusRef.addValueEventListener(listener)

            awaitClose { userStatusRef.removeEventListener(listener) }

        }
    }

    override fun observeMultiplePresence(uids: List<String>): Flow<Map<String, UserPresence>> =
        callbackFlow {

            if (uids.isEmpty()) {
                trySend(emptyMap())
                close()
                return@callbackFlow
            }

            val statusRef = rtdb.getReference("/status")

            val listener = statusRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val presenceMap = mutableMapOf<String, UserPresence>()

                    uids.forEach { uid ->
                        val userSnapshot = snapshot.child(uid)
                        if (userSnapshot.exists()) {
                            val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                            val lastSeen = userSnapshot.child("lastSeen").getValue(Long::class.java) ?: 0L

                            presenceMap[uid] = UserPresence(isOnline = isOnline, lastSeen = lastSeen)
                        } else {
                            presenceMap[uid] = UserPresence(isOnline = false, lastSeen = 0L)
                        }
                    }
                    trySend(presenceMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }

            })
            awaitClose { statusRef.removeEventListener(listener) }

        }

}