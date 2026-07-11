package com.example.hydrogram.presentation.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


val GlassBackground = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.25f),
    ),
)

val GlassBorder = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.65f),
        Color.White.copy(alpha = 0.10f),
    )
)