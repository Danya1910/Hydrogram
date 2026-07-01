package com.example.hydrogram.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
)
