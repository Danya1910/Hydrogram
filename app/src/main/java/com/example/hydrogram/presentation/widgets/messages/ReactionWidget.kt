package com.example.hydrogram.presentation.widgets.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText

@Composable
fun ReactionWidget(
    reaction: String,
    color: Color,
    onReactionClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(31.dp)
            .clip(
                shape = CircleShape
            )
            .background(
                color = color
            )
            .clickable{
                onReactionClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "\u2764\uFE0F",
                fontSize = 18.sp,
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ReactWidgetPreview() {
    ReactionWidget(
        reaction = "1",
        color = Green,
        onReactionClick = {}
    )
}