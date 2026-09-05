package com.example.hydrogram.presentation.util

import android.content.ClipData
import android.content.Context
import android.content.ClipboardManager

fun CopyTextToClipboard(
    context: Context,
    text: String,
) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboardManager.setPrimaryClip(clip)

}