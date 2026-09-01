package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun MessageActionMenu(
    onReactionClick: (String) -> Unit,
) {

    val rowGradient = Brush.horizontalGradient(
        colors = listOf(
            Color.White,
            Color.White,
            Color.White,
            Color(0xFFE5E4E4)
        )
    )

    val listOfReactions = listOf(
        "\u2764\uFE0F",
        "\uD83D\uDC4D",
        "\uD83D\uDC4E",
        "\uD83D\uDD25",
    )

    Column(

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .height(47.dp)
                .clip(
                    shape = CircleShape
                )
                .background(
                    brush = rowGradient
                )
                .padding(horizontal = 10.dp)
        ) {
            listOfReactions.forEach { reaction ->
                Text(
                    text = reaction,
                    fontFamily = SfProText,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable {
                            onReactionClick(reaction)
                        }
                )
            }
        }
    }
}