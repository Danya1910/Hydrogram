package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.UserRepository
import com.example.hydrogram.presentation.util.normalizePhoneNumber
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    override suspend fun findUsersByPhoneOrUserName(query: String): List<User> {
        return try {
            val trimmed = query.trim().removePrefix("@")
            val normalizedQuery = trimmed.lowercase()

            if (normalizedQuery.isEmpty()) return emptyList()

            Log.d("FIRESTORE", "Поиск пользователя: '$normalizedQuery'")

            val foundUsers = mutableListOf<User>()

            // 1. Поиск по телефону (нормализуем ввод так же, как храним)
            val phoneQuery = trimmed.filter { it.isDigit() }
                .removePrefix("8")
                .removePrefix("7")
                .takeLast(10)

            if (phoneQuery.isNotEmpty()) {
                val phoneSnapshot = firestore.collection("users")
                    .whereEqualTo("phone", phoneQuery)
                    .limit(10)
                    .get()
                    .await()

                Log.d("FIRESTORE", "По телефону ($phoneQuery) найдено: ${phoneSnapshot.size()}")
                for (document in phoneSnapshot.documents) {
                    document.toObject(User::class.java)?.let { foundUsers.add(it) }
                }
            }

            // 2. Поиск по userNameLowercase
            val userNameSnapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("userNameLowercase", normalizedQuery)
                .whereLessThanOrEqualTo("userNameLowercase", normalizedQuery + "\uf8ff")
                .limit(10)
                .get()
                .await()

            Log.d("FIRESTORE", "По userNameLowercase найдено: ${userNameSnapshot.size()}")
            for (document in userNameSnapshot.documents) {
                document.toObject(User::class.java)?.let { foundUsers.add(it) }
            }

            // 3. Поиск по nameLowercase
            val nameSnapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("nameLowercase", normalizedQuery)
                .whereLessThanOrEqualTo("nameLowercase", normalizedQuery + "\uf8ff")
                .limit(10)
                .get()
                .await()

            Log.d("FIRESTORE", "По nameLowercase найдено: ${nameSnapshot.size()}")
            for (document in nameSnapshot.documents) {
                document.toObject(User::class.java)?.let { foundUsers.add(it) }
            }

            val distinctUsers = foundUsers.distinctBy { it.uid }

            Log.d("FIRESTORE", "Всего уникальных: ${distinctUsers.size}")
            distinctUsers

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FIRESTORE", "Ошибка поиска", e)
            emptyList()
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

    override suspend fun logOut() {
        val auth = Firebase.auth
        val currentUser = auth.currentUser

        val userId = currentUser?.uid
        if (userId != null) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("fcmTokens", emptyMap<String, Boolean>())
                    .await()
                Log.d("AuthRepository", "✅ Токены удалены для $userId")
            } catch (e: Exception) {
                Log.e("AuthRepository", "⚠️ Не удалось удалить токены: ${e.message}")
            }
        }

        auth.signOut()
        Log.d("AuthRepository", "✅ Пользователь вышел из системы")

    }

}