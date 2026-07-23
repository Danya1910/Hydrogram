package com.example.hydrogram.domain.repository

import com.example.hydrogram.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun saveUserProfile(user: User) : Result<Unit>

    suspend fun saveUserName(uid: String, userName: String) : Result<Unit>

    fun getUserById(uid: String) : Flow<User?>

    suspend fun findUserByPhoneOrUserName(query: String) : User?

    suspend fun setUserOnlineStats(uid: String, isOnline: Boolean) : Result<Unit>

}