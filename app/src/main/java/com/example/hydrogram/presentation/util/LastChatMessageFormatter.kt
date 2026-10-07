package com.example.hydrogram.presentation.util


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ChatTimeFormatter {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val weekDaysShort = mapOf(
        Calendar.MONDAY to "Пн",
        Calendar.TUESDAY to "Вт",
        Calendar.WEDNESDAY to "Ср",
        Calendar.THURSDAY to "Чт",
        Calendar.FRIDAY to "Пт",
        Calendar.SATURDAY to "Сб",
        Calendar.SUNDAY to "Вс",
    )

    fun format(timestamp: Long): String {
        if (timestamp <= 0L) return ""

        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 1. Сегодня
        if (isSameDay(now, then)) {
            return timeFormat.format(Date(timestamp))
        }

        // 2. Вчера (опционально — можно убрать, если не нужно)
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (isSameDay(yesterday, then)) {
            return "Вчера"
        }

        // 3. На этой неделе (от начала текущей недели до сейчас)
        val startOfWeek = (now.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // откатываемся к понедельнику
            add(Calendar.DAY_OF_YEAR, -(get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7)
        }

        if (then.timeInMillis >= startOfWeek.timeInMillis && then.timeInMillis < now.timeInMillis) {
            val day = then.get(Calendar.DAY_OF_WEEK)
            return weekDaysShort[day] ?: ""
        }

        // 4. Всё остальное — дата
        return dateFormat.format(Date(timestamp))
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}