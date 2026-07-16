package com.example.hydrogram.domain.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val email: String = "",
    @get:PropertyName("isOnline")
    val isOnline: Boolean = false,
    val createdAt: Long = 0L,
    val phone: String = "",
)
