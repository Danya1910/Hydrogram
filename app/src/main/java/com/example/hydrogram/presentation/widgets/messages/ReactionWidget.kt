package com.example.hydrogram.presentation.widgets.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hydrogram.presentation.widgets.messages.text.MessageReactions
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText

@Composable
fun ReactionWidget(
    reactions: MessageReactions?,
    color: Color,
    mineAvatar: String? = null,
    penpalAvatar: String? = null,
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
            .clickable {
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
                text = reactions?.mineReaction ?: reactions?.penpalReaction ?: "",
                fontSize = 22.sp,
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
            )
            if (mineAvatar != null || penpalAvatar != null) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .width(
                            when {
                                mineAvatar != null && penpalAvatar != null -> 40.dp
                                else -> 25.dp
                            }
                        )
                ) {
                    if (penpalAvatar != null) {
                        AsyncImage(
                            model = mineAvatar,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }


                    if (mineAvatar != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(
                                    shape = CircleShape
                                )
                                .background(
                                    color = Green
                                )
                        ) {
                            AsyncImage(
                                model = mineAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(25.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditedText() {
    Text(
        text = "изменено",
        fontFamily = SfProText,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = Color.Gray,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
