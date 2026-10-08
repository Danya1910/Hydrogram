package com.example.hydrogram.presentation.util

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
    val nowInstant = Instant.ofEpochMilli(now)
    val lastSeenInstant = Instant.ofEpochMilli(lastSeenTimestamp)

    val minutesAgo = ChronoUnit.MINUTES.between(lastSeenInstant, nowInstant)

    if (minutesAgo < 1) return "был(а) только что"
    if (minutesAgo < 60) return "был(а) $minutesAgo мин. назад"

    val userZone = ZoneId.systemDefault()
    val today = LocalDate.now(userZone)
    val lastSeenDate = lastSeenInstant.atZone(userZone).toLocalDate()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeStr = lastSeenInstant.atZone(userZone).format(timeFormatter)

    return when (lastSeenDate) {
        today -> "был(а) сегодня в $timeStr"
        today.minusDays(1) -> "был(а) вчера в $timeStr"
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM в HH:mm", Locale("ru"))
            "был(а) ${lastSeenInstant.atZone(userZone).format(dateFormatter)}"
        }
    }
}