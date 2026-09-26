package com.example.hydrogram.presentation.widgets.messages.video

import android.text.format.DateFormat
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.LightGreen
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date

@Composable
fun CircleVideoMessage(
    isMine: Boolean,
    message: Message,
    onMessageClick: (String) -> Unit,
) {

    val videoDuration = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    Row(
        horizontalArrangement = if(isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .clickable{
                    onMessageClick(message.messageId)
                }
        ) {
            CircleVideoPlayer(
                message = message as Message.CircleVideo,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter,
                    )
                    .padding(horizontal = 5.dp)
            ) {
                VideoInfo(
                    text = videoDuration
                )
                Spacer(modifier = Modifier.weight(1f))
                VideoInfo(
                    text = formattedTime
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun CircleVideoPlayer(
    message: Message.CircleVideo,
) {

    val context = LocalContext.current

    val localExoPlayer = remember(message.messageId) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(message.videoUrl ?: "")
            setMediaItem(mediaItem)
            prepare()

            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    DisposableEffect(message.messageId) {
        onDispose {
            localExoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(
                shape = CircleShape,
            )

            .border(
                width = 1.dp,
                color = Green,
            )
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false

                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                    player = localExoPlayer
                }
            },
            update = { playerView ->
                if(playerView.player != localExoPlayer) {
                    playerView.player = localExoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )
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