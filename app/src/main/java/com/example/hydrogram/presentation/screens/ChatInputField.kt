package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
private fun ChatInputField(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
) {

}


@Composable
@Preview(showBackground = true)
private fun ChatInputFieldPreview() {

}

@Composable
private fun AttachButton(
    onAttachClick: () -> Unit,
) {
    val glassBackground = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.25f),
        ),
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.10f),
        )
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                brush = glassBackground
            )
    )
}