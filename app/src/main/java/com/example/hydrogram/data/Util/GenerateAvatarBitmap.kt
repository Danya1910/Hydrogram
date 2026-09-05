package com.example.hydrogram.data.Util

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Base64
import android.graphics.Canvas
import androidx.core.graphics.toColorInt
import java.io.ByteArrayOutputStream


fun generateAvatarBitmap(
    name: String,
) : String {

    val AVATAR_COLORS = listOf(
        "#FF6B6B".toColorInt(),
        "#FF9F43".toColorInt(),
        "#FECA57".toColorInt(),
        "#54A0FF".toColorInt(),
        "#5F27CD".toColorInt(),
        "#1DD1A1".toColorInt(),
        "#10AC84".toColorInt(),
        "#EE5A24".toColorInt(),
        "#0ABDE3".toColorInt(),
        "#A29BFE".toColorInt(),
        "#FD79A8".toColorInt(),
        "#00B894".toColorInt(),
        "#E17055".toColorInt(),
        "#6C5CE7".toColorInt(),
        "#FDCB6E".toColorInt(),
        "#00CEC9".toColorInt(),
    )

    val pixelSize = 100

    val bitmap = createBitmap(pixelSize, pixelSize)
    val canvas = Canvas(bitmap)

    val backgroundColor = AVATAR_COLORS.random()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    canvas.drawRect(
        0f,
        0f,
        pixelSize.toFloat(),
        pixelSize.toFloat(),
        paint,
    )

    val initials = getInitials(name)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = pixelSize * 0.45f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        typeface = Typeface.DEFAULT_BOLD
    }

    val x = pixelSize / 2f
    val y = pixelSize / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

    canvas.drawText(initials, x, y, textPaint)

    return bitmapToBase64(bitmap)
}

fun getInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"

    val parts = trimmed.split(" ")
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> {
            val first = parts[0].take(1)
            val last = parts.last().take(1)
            (first + last).uppercase()
        }
    }
}


fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    val byteArray = stream.toByteArray()
    val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}

