package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hydrogram.ui.theme.Separator

@Composable
fun SeparatorLine(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                color = Separator
            )
    )
}