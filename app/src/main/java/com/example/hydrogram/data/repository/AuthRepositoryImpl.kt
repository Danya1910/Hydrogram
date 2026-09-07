package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.data.Util.generateAvatarBitmap
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw Exception("User ID is null")

        val token = FirebaseMessaging.getInstance().token.await()
        saveFcmToken(userId, token)

        Log.d("AuthRepository", "Пользователь вошел по email, токен сохранен")
    }

    override suspend fun signInWithPhoneAndPassword(phone: String, password: String): Result<Unit> = runCatching {
        val result = firestore.collection("users")
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .await()

        val userDoc = result.documents.firstOrNull()
            ?: throw Exception("Пользователь с таким телефоном не найден")

        val email = userDoc.getString("email")
            ?: throw Exception("Email пользователя не найден")

        auth.signInWithEmailAndPassword(email, password).await()

        val userId = auth.currentUser?.uid
            ?: throw Exception("Не удалось получить ID пользователя")

        val token = FirebaseMessaging.getInstance().token.await()
        saveFcmToken(userId, token)

        Log.d("AuthRepository", "Пользователь вошел по телефону, токен сохранен: $token")
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String
    ): Result<Unit> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("User creation failed")

        val generatedAvatar = generateAvatarBitmap(name = name)
        Log.d("AuthRepository", "Аватар сгенерирован: $generatedAvatar")

        val userMap = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "avatarUrl" to generatedAvatar,
            "isOnline" to true,
            "createdAt" to System.currentTimeMillis(),
            "phone" to phone,
            "fcmTokens" to emptyMap<String, Boolean>()
        )
        firestore.collection("users").document(uid).set(userMap).await()

        val token = FirebaseMessaging.getInstance().token.await()
        saveFcmToken(uid, token)

        Log.d("AuthRepository", "Пользователь зарегистрирован, токен сохранен: $token")
    }

    override suspend fun checkPhoneRegistration(phone: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("phone", phone)
                .limit(1)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            Log.d("AuthRepository", "Ошибка проверки телефона: ${e.message}")
            false
        }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        val userId = getCurrentUserId()
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .update("fcmTokens", emptyMap<String, Boolean>())
                .await()
            Log.d("AuthRepository", "Токены пользователя удалены при выходе")
        }
        auth.signOut()
        Log.d("AuthRepository", "Пользователь вышел")
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = runCatching {
        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("User not logged in"))

        saveFcmToken(userId, token)
        Log.d("AuthRepository", "Токен обновлен: $token")
    }

    private suspend fun saveFcmToken(userId: String, token: String) {
        firestore.collection("users")
            .document(userId)
            .update("fcmTokens.$token", true)
            .await()
        Log.d("AuthRepository", "Токен сохранен для пользователя $userId")
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null
}