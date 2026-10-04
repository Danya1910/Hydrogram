package com.example.hydrogram.presentation.util

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


fun Modifier.glassEffect(
    cornerRadius: Dp = 24.dp,
    opacity: Float = 1f,
    lightAngle: Float = -45f,        // Figma: Light angle
    lightIntensity: Float = 0.6f,    // Figma: Light intensity (по умолчанию 60%)
    refraction: Float = 100f,        // Figma: Refraction
    depth: Float = 16f,              // Figma: Depth
    dispersion: Float = 0f,          // Figma: Dispersion
    frost: Float = 7f,               // Figma: Frost
    splay: Float = 6f                // Figma: Splay
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .graphicsLayer {
        alpha = opacity

        if (frost > 0f && size.width > 0f && size.height > 0f) {
            when {
                // API 33+ — шейдер + размытие одновременно
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    val shader = RuntimeShader(GLASS_AGSL)
                    shader.setFloatUniform("uSize", size.width, size.height)
                    shader.setFloatUniform("uRadius", cornerRadius.toPx())
                    shader.setFloatUniform("uRefraction", refraction)
                    shader.setFloatUniform("uDispersion", dispersion)
                    shader.setFloatUniform("uSplay", splay)
                    shader.setFloatUniform("uLightAngle", lightAngle)
                    shader.setFloatUniform("uLightIntensity", lightIntensity)
                    shader.setFloatUniform("uDepth", depth)

                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "uContent")
                        .asComposeRenderEffect()
                }
                // API 31..32 — только blur, без преломления
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    renderEffect = RenderEffect
                        .createBlurEffect(frost * 2f, frost * 2f, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
                else -> {
                    renderEffect = null
                }
            }
        } else {
            renderEffect = null
        }
    }
    // API < 31: Modifier.blur как фолбек для Frost
    .let {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && frost > 0f) {
            it.blur(frost.dp)
        } else it
    }
    // Свет + Depth-обводка (без шейдера, работает на всех API)
    .drawWithCache {
        val center = size.center
        val width = size.width
        val height = size.height

        val angleRad = Math.toRadians(lightAngle.toDouble())
        val startX = center.x - (width / 2) * cos(angleRad).toFloat()
        val startY = center.y - (height / 2) * sin(angleRad).toFloat()
        val endX = center.x + (width / 2) * cos(angleRad).toFloat()
        val endY = center.y + (height / 2) * sin(angleRad).toFloat()

        val lightBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = lightIntensity * 0.3f),
                Color.White.copy(alpha = 0.05f),
                Color.Black.copy(alpha = (refraction / 100f) * 0.1f)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        )

        val strokeWidth = (depth / 8f).coerceAtLeast(0f)

        onDrawBehind {
            drawRect(brush = lightBrush)
            if (strokeWidth > 0f) {
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
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }

/**
 * AGSL-шейдер для Refraction / Dispersion / Splay / Light / Depth.
 *
 * ВАЖНО: в AGSL границы `for` должны быть константными.
 * Поэтому blur (Frost) здесь НЕ реализован — он вынесен в RenderEffect.createBlurEffect.
 * Здесь только арифметика, никаких циклов с uniform-границами.
 */
private const val GLASS_AGSL = """
uniform shader uContent;
uniform float2 uSize;
uniform float  uRadius;
uniform float  uRefraction;    // 0..100
uniform float  uDispersion;    // 0..100
uniform float  uSplay;         // 0..20
uniform float  uLightAngle;    // °
uniform float  uLightIntensity;// 0..1
uniform float  uDepth;         // 0..16

// 3 фиксированных сэмпла для dispersion (R/G/B сдвиг) — без циклов.
half4 sampleAt(float2 uv) {
    return uContent.eval(uv * uSize);
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uSize;

    // --- Refraction: псевдо-нормаль к скруглённому прямоугольнику ---
    float2 d = abs(uv - 0.5) * uSize;
    float2 halfSize = uSize * 0.5 - uRadius;
    float2 corner = max(d - halfSize, 0.0);
    float2 normal = normalize(corner + 1e-6);
    float offset = (uRefraction / 100.0) * 0.06 * uSize.x;

    // --- Splay: радиальное рассеивание от направления света ---
    float2 lightDir = float2(cos(radians(uLightAngle)), sin(radians(uLightAngle)));
    float splay = (uSplay / 20.0) * 0.01;
    float2 splayOffset = lightDir * splay * uSize.x;

    // --- Refraction + Splay смещение UV ---
    float2 refracted = uv + (normal * offset + splayOffset) / uSize;

    // --- Dispersion: сдвиг R/G/B по X ---
    float disp = (uDispersion / 100.0) * 0.01;
    half4 cR = sampleAt(refracted + float2( disp, 0.0));
    half4 cG = sampleAt(refracted);
    half4 cB = sampleAt(refracted - float2( disp, 0.0));
    half4 col = half4(cR.r, cG.g, cB.b, cG.a);

    // --- Light: подсветка со стороны uLightAngle ---
    float2 lp = uv - 0.5;
    float lightDot = dot(normalize(lp + 1e-6), lightDir);
    float lightMask = smoothstep(0.0, 1.0, lightDot * 0.5 + 0.5);
    col.rgb += half3(1.0) * half(lightMask * uLightIntensity * 0.25);

    // --- Depth: усиление яркости к центру ---
    float depthMask = smoothstep(1.0 - uDepth / 16.0, 1.0, length(lp) * 2.0);
    col.rgb *= mix(1.0, 1.1, depthMask);

    return col;
}
"""