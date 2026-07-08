package com.example.hydrogram.presentation.util

data class MenuRowItem(
    val title: String,
    val icon: Int,
    val onClick: () -> Unit,
)
