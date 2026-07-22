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
        Blue.copy(alpha = 0.55f),
        Blue.copy(alpha = 0.25f),
        Blue.copy(alpha = 0.45f),
    ),
)

val BlueGlassBorder = Brush.linearGradient(
    colors = listOf(
        Blue.copy(alpha = 0.55f),
        Blue.copy(alpha = 0.20f),
        Blue.copy(alpha = 0.40f),
    )
)