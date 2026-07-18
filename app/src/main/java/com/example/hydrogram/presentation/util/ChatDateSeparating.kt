package com.example.hydrogram.presentation.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatHeaderDate(timestamp: Long): String {
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()

    return when {
        // Если это сегодня
        DateUtils.isToday(timestamp) -> "Сегодня"

        // Если это вчера
        DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> "Вчера"

        // Если это текущий год, пишем без года: "17 июля"
        messageTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
            SimpleDateFormat("d MMMM", Locale("ru")).format(messageTime.time)
        }

        // Если прошлые года, пишем с годом: "20 ноября 2025"
        else -> {
            SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(messageTime.time)
        }
    }
}

fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}