package com.example.hydrogram.presentation.util

fun formatPhoneNumber(rawInput: String): String {
    val digits = rawInput.filter { it.isDigit() }

    val mainNumber = when {
        digits.length == 10 -> digits
        digits.length == 11 && (digits.startsWith("7") || digits.startsWith("8")) -> digits.drop(1)
        else -> return rawInput
    }

    val part1 = mainNumber.substring(0, 3)
    val part2 = mainNumber.substring(3, 6)
    val part3 = mainNumber.substring(6, 10)

    return "+7 $part1 $part2 $part3"
}