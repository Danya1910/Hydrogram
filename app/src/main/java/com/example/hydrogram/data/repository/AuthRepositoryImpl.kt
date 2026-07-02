package com.example.hydrogram.data.repository

import com.example.hydrogram.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<Unit> = runCatching{
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("User creation failed")
        val userMap = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "avatarUrl" to "",
            "isOnline" to true,
            "createdAt" to System.currentTimeMillis(),
        )
        firestore.collection("users").document(uid).set(userMap).await()
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override fun logout() = auth.signOut()

}