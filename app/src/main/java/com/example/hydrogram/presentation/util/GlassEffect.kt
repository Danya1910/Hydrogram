package com.example.hydrogram.presentation.util

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

fun Modifier.glassEffect(
    cornerRadius: Dp = 24.dp,       // Corner radius: 24
    opacity: Float = 1f,            // Opacity: 100%
    lightAngle: Float = -45f,       // Light angle: -45°
    lightIntensity: Float = 0.8f,   // Light intensity: 80%
    refraction: Float = 100f,       // Refraction: 100 (смещение/блик)
    depth: Float = 16f,             // Depth: 16 (имитируем внутренней тенью/обводкой)
    frost: Float = 7f               // Frost: 7 (радиус размытия)
): Modifier = this
    // 1. Применяем скругление углов
    .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
    // 2. Управляем общей непрозрачностью слоя
    .graphicsLayer {
        alpha = opacity
        // На Android 12+ (API 31+) создаем нативный эффект и конвертируем его в Compose
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && frost > 0f) {
            val nativeBlur = android.graphics.RenderEffect.createBlurEffect(
                frost * 2f, // Сила размытия по X (масштабируем под пиксели)
                frost * 2f, // Сила размытия по Y
                android.graphics.Shader.TileMode.CLAMP
            )
            renderEffect = nativeBlur.asComposeRenderEffect()
        }
    }
    // Для Android 11 и ниже добавляем стандартный фолбек размытия содержимого
    .let {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && frost > 0f) {
            it.blur(frost.dp)
        } else it
    }
    // 3. Отрисовка физических свойств: Light (Свет), Refraction (Преломление) и Depth (Глубина)
    .drawWithCache {
        // Получаем центр и размеры из свойства size класса CacheDrawScope
        val center = size.center
        val width = size.width
        val height = size.height

        // Переводим угол света в радианы для расчета направления блика
        val angleRad = Math.toRadians(lightAngle.toDouble())
        val startX = center.x - (width / 2) * cos(angleRad).toFloat()
        val startY = center.y - (height / 2) * sin(angleRad).toFloat()
        val endX = center.x + (width / 2) * cos(angleRad).toFloat()
        val endY = center.y + (height / 2) * sin(angleRad).toFloat()

        // Создаем градиент для имитации падения света (Light 80% + Refraction)
        val lightBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = lightIntensity * 0.3f), // Источник света
                Color.White.copy(alpha = 0.05f),                // Тело стекла
                Color.Black.copy(alpha = (refraction / 100f) * 0.1f) // Преломление/затемнение на выходе
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        )

        // Имитация глубины (Depth) с помощью тонкой светящейся обводки по контуру стекла
        val strokeWidth = (depth / 8f).coerceAtLeast(1f)

        onDrawBehind {
            // Рисуем подложку стекла, которая собирает свет
            drawRect(
                brush = lightBrush,
                blendMode = BlendMode.SrcOver
            )

            // Рисуем глянцевый ободок (catch light на гранях стекла)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.40f),
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
    }
