package com.example.hydrogram.presentation.widgets.messages

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val mineAvatarBitmap = remember(mineAvatar) {
        if (!mineAvatar.isNullOrBlank()) {
            val base64String = mineAvatar.substringAfter("base64,")
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } else {
            null
        }
    }
    Log.d("ReactionWidget", "mineAvatarBitmap: $mineAvatarBitmap")

    val penpalAvatarBitmap = remember(penpalAvatar) {
        if (!penpalAvatar.isNullOrBlank()) {
            val base64String = penpalAvatar.substringAfter("base64,")
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } else {
            null
        }
    }

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
                fontSize = 18.sp,
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
            )
            if (mineAvatarBitmap != null || penpalAvatarBitmap != null) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .height(25.dp)
                        .width(
                            when {
                                mineAvatarBitmap != null && penpalAvatarBitmap != null -> 40.dp
                                else -> 25.dp
                            }
                        )
                ) {
                    if (penpalAvatarBitmap != null) {
                        Image(
                            bitmap = penpalAvatarBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(25.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (mineAvatarBitmap != null) {
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
                            Image(
                                bitmap = mineAvatarBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(25.dp)
                                    .clip(CircleShape)
                                    .let {
                                        if (penpalAvatarBitmap != null) {
                                            it.background(Color.White, CircleShape)
                                                .padding(1.dp)
                                                .clip(CircleShape)
                                        } else it
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
