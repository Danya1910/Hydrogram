package com.example.hydrogram.domain.repository

interface AuthRepository {

    suspend fun signIn(email: String, password: String) : Result<Unit>

    suspend fun signUp(email: String, password: String, name: String) : Result<Unit>

    fun getCurrentUserId() : String?

    fun isUserLoggedIn() : Boolean

    fun logout()

}