package com.example.hydrogram.presentation.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


val GlassBackground = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.45f),
    ),
)

val GlassBorder = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.20f),
        Color.White.copy(alpha = 0.40f),
    )
)