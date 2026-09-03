package com.example.hydrogram.presentation.widgets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.Red
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun MessageActionMenu(
    onReactionClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
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
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(238.dp)
                .clip(
                    shape = RoundedCornerShape(34.dp),
                )
                .background(
                    brush = GlassBackground
                )
                .border(
                    width = 1.dp,
                    brush = GlassBorder,
                    shape = RoundedCornerShape(34.dp),
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                RowMessageAction(
                    item = RowMessageActionItem(
                        icon = R.drawable.ic_trashbox,
                        title = "Удалить",
                        onDeleteClick = {
                            onDeleteClick()
                        },
                        color = Red,
                    )
                )
            }
        }
    }
}

@Composable
private fun RowMessageAction(
    item: RowMessageActionItem,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(40.dp)
            .clickable{
                item.onDeleteClick()
                Log.d("MessaageActionMenu", "delete button clicked")
            }
            .padding(horizontal = 27.dp)

    ) {
        Icon(
            painter = painterResource(
                item.icon
            ),
            contentDescription = null,
            tint = item.color,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = item.color,
            letterSpacing = -(0.43).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

data class RowMessageActionItem(
    val icon: Int,
    val title: String,
    val onDeleteClick: () -> Unit,
    val color: Color,
)