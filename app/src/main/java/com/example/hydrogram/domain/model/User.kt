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
    val aboutUser: String = "",
    val birthdayDate: String = "",
    val userName: String = "",
    val userNameLowercase: String = "",
)

data class UserPresence(
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
)

data class RegisteredContact(
    val user: User = User(),
    val contactName: String = "",
)