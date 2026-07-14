package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.core.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserById(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if(error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun findUserByPhone(phone: String): User? {
        return try {
            Log.d("FIRESTORE", "Поиск пользователя: $phone")
            val snapshot = firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .await()

            Log.d("FIRESTORE", "Найдено документов: ${snapshot.size()}")

            if(!snapshot.isEmpty) {
                snapshot.documents.first().toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FIRESTORE", "Ошибка поиска", e)
            null
        }
    }

    override suspend fun setUserOnlineStats(uid: String, isOnline: Boolean): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("isOnline", isOnline)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}