package com.example.hydrogram.presentation.util

import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun generateChatId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) {
        "${userId1}_${userId2}"
    } else {
        "${userId2}_${userId1}"
    }
}

fun formatLastSeen(
    lastSeenTimestamp: Long,
    now: Long = System.currentTimeMillis(),
): String {

    Log.d("Locale", "ru = ${Locale("ru")}")
    Log.d("Locale", "ru-RU = ${Locale("ru", "RU")}")
    Log.d("Locale", "forLanguageTag = ${Locale.forLanguageTag("ru-RU")}")

    val nowInstant = Instant.ofEpochMilli(now)
    val lastSeenInstant = Instant.ofEpochMilli(lastSeenTimestamp)

    val minutesAgo = ChronoUnit.MINUTES.between(lastSeenInstant, nowInstant)

    if (minutesAgo < 1) return "был(а) только что"
    if (minutesAgo < 60) return "был(а) $minutesAgo мин. назад"

    val userZone = ZoneId.systemDefault()
    val ruLocale = Locale.forLanguageTag("ru-RU")

    Log.d("Fmt", "ru: ${nowInstant.atZone(userZone).format(DateTimeFormatter.ofPattern("HH:mm", Locale("ru")))}")
    Log.d("Fmt", "ru-RU: ${nowInstant.atZone(userZone).format(DateTimeFormatter.ofPattern("HH:mm", Locale("ru", "RU")))}")
    Log.d("Fmt", "forLanguageTag: ${nowInstant.atZone(userZone).format(DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru-RU")))}")


    val today = LocalDate.now(userZone)
    val lastSeenDate = lastSeenInstant.atZone(userZone).toLocalDate()

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", ruLocale)

    val timeStr = lastSeenInstant.atZone(userZone).format(timeFormatter)

    return when (lastSeenDate) {
        today -> "был(а) сегодня в $timeStr"
        today.minusDays(1) -> "был(а) вчера в $timeStr"
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM в HH:mm", ruLocale)
            "был(а) ${lastSeenInstant.atZone(userZone).format(dateFormatter)}"
        }
    }
}