package com.example.hydrogram.presentation.widgets.video

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText

@Composable
fun MineVideoMessage(
    isMine: Boolean,
) {
    Row(
        horizontalArrangement = if(isMine) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(200.dp)
        ) {
            CircleVideoPlayer()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter,
                    )
            ) {

            }
        }
    }
}

@Composable
private fun CircleVideoPlayer() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(
                shape = CircleShape,
            )
            .background(
                color = Green,
            )
    ) {
       //воспроизведение сообщения ExoPlayer
    }
}

@Composable
private fun VideoInfo(
    text: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(20.dp)
            .clip(
                shape = CircleShape,
            )
            .background(
                color = DateSeparatorGreen.copy(alpha = 0.65f),
            )
            .padding(horizontal = 5.dp)
    ) {
        Text(
            text = text,
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.White,
        )
    }
}