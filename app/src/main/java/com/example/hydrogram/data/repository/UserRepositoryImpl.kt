package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import com.example.hydrogram.presentation.util.normalizePhoneNumber
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.emptyList

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

    override suspend fun saveUserName(uid: String, userName: String): Result<Unit> {
        return try {
            val userNameLowercase = userName.trim().lowercase()
            Log.d("UserRepositoryImpl", "lowercase: $userNameLowercase")
            firestore.collection("users")
                .document(uid)
                .update(
                    "userName", userName,
                    "userNameLowercase", userNameLowercase,
                )
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
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun findUserByPhoneOrUserName(query: String): User? {
        return try {
            val normalizedQuery = query.trim().removePrefix("@").lowercase()

            Log.d("FIRESTORE", "Поиск пользователя (регистронезависимый): $normalizedQuery")

            val snapshot = firestore.collection("users")
                .where(
                    Filter.or(
                        Filter.equalTo("userNameLowercase", normalizedQuery),
                        Filter.equalTo("phone", normalizedQuery),
                    )
                )
                .get()
                .await()

            Log.d("FIRESTORE", "Найдено документов: ${snapshot.size()}")

            if (!snapshot.isEmpty) {
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

    override suspend fun changeAvatar(uid: String, avatarString: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("avatarUrl", avatarString)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Ошибка при смене аватара для $uid", e)
            Result.failure(e)
        }
    }

    override suspend fun syncContacts(phoneNumbers: List<String>): List<User> {
        if (phoneNumbers.isEmpty()) return emptyList()

        return try {
            val chunks = phoneNumbers.chunked(30)
            val foundUsers = mutableListOf<User>()

            for (chunk in chunks) {

                val normalizedChunk = chunk.map { normalizePhoneNumber(rawPhone = it) }
                val snapshot = firestore.collection("users")
                    .whereIn("phone", normalizedChunk)
                    .get()
                    .await()

                val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                foundUsers.addAll(users)
            }

            foundUsers
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

    }
}