package com.example.hydrogram.presentation.widgets.messages.video

import android.text.format.DateFormat
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CircleVideoMessage(
    isMine: Boolean,
    message: Message,
    globalIndex: Int?,
    lazyListState: LazyListState,
    bottomPaddingPx: Int,
    onMessageClick: (String) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }

    val widthExpand by animateFloatAsState(
        targetValue = if (isExpanded) 0.9f else 0.5f,
    )

    val durationSeconds = (message as Message.CircleVideo).durationSeconds ?: 0

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    val videoDuration = String.format("%02d:%02d", minutes, seconds)

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    val density = LocalDensity.current

    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(widthExpand)
                .aspectRatio(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onMessageClick(message.messageId)
                    isExpanded = !isExpanded

                    if (isExpanded && globalIndex != null) {
                        coroutineScope.launch {
                            delay(100)

                            val extraMargin = with(density) { 32.dp.roundToPx() }
                            val scrollOffset = -bottomPaddingPx - extraMargin

                            lazyListState.animateScrollToItem(
                                index = globalIndex,
                                scrollOffset = scrollOffset
                            )
                        }
                    }
                }
        ) {
            CircleVideoPlayer(
                message = message,
                isExpanded = isExpanded,
                onCycleEnded = {
                    isExpanded = false
                }
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
    isExpanded: Boolean,
    onCycleEnded: () -> Unit,
) {
    val context = LocalContext.current

    val localExoPlayer = remember(message.messageId) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(message.videoUrl ?: "")
            setMediaItem(mediaItem)
            prepare()

            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            localExoPlayer.seekTo(0)
        }
    }

    DisposableEffect(localExoPlayer) {
        val listener = object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    onCycleEnded()
                }
            }
        }
        localExoPlayer.addListener(listener)
        onDispose {
            localExoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(isExpanded) {
        localExoPlayer.volume = if (isExpanded) 1f else 0f
        onDispose { }
    }

    DisposableEffect(message.messageId) {
        onDispose {
            localExoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .border(width = 1.dp, color = Green, shape = CircleShape)
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )

                    localExoPlayer.setVideoTextureView(this)
                }
            },
            update = { textureView ->
                localExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                localExoPlayer.setVideoTextureView(textureView)
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