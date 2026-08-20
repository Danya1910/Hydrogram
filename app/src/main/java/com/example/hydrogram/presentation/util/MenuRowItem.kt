package com.example.hydrogram.presentation.util

import android.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush

@Immutable
data class MenuRowItem(
    val title: String,
    val icon: Int,
    val onClick: () -> Unit,
    val gradient: Brush? = null,
)
