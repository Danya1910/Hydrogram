package com.example.hydrogram.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val createdAt: Long = 0L,
)
