package com.example.hydrogram.presentation.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.hydrogram.ui.theme.Blue


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

val BlueGlassBackground = Brush.linearGradient(
    colors = listOf(
        Blue.copy(alpha = 0.95f),
        Blue.copy(alpha = 0.75f),
        Blue.copy(alpha = 0.88f),
    ),
)

val BlueGlassBorder = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 1f),
        Color.White.copy(alpha = 0.8f),
        Blue.copy(alpha = 0.60f),
        Blue.copy(alpha = 0.35f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 1f),
    )
)