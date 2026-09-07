package com.example.hydrogram.domain.repository

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signInWithPhoneAndPassword(phone: String, password: String) : Result<Unit>

    suspend fun signUp(email: String, password: String, name: String, phone: String): Result<Unit>

    suspend fun checkPhoneRegistration(phone: String): Boolean

    suspend fun signOut(): Result<Unit>

    fun getCurrentUserId(): String?

    fun isUserLoggedIn(): Boolean

    suspend fun updateFcmToken(token: String): Result<Unit>

}