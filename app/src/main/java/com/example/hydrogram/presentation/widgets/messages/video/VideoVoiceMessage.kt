package com.example.hydrogram.presentation.widgets.messages.video

import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.presentation.util.MessageCallbacks
import com.example.hydrogram.presentation.util.MessageData
import com.example.hydrogram.presentation.widgets.messages.ReactionWidget
import com.example.hydrogram.presentation.widgets.messages.text.MessageReactions
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CircleVideoMessage(
    isMine: Boolean,
    message: Message,
    messageData: MessageData,
    messageCallbacks: MessageCallbacks,
    globalIndex: Int?,
    lazyListState: LazyListState,
    bottomPaddingPx: Int,
    setCurrentVideo: (String) -> Unit,
    currentVideoId: String,
) {

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    val validReactions = message.reactions
        ?.filterValues { true }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null
    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null

    message.reactions?.entries?.forEach { entry ->
        if (entry.key == messageData.mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        } else {
            penpalReactionId = entry.key
            penpalReactionEmoji = entry.value
        }
        reactions = MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
        Log.d("Reaction", "$mineReactionId reacted with $mineReactionEmoji")
        Log.d("Reaction", "$penpalReactionId reacted with $penpalReactionEmoji")
    }

    val coroutineScope = rememberCoroutineScope()

    val isExpanded = currentVideoId == message.messageId

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

    var currentExpandDuration by remember { mutableStateOf(0L) }

    val currentSeconds = currentExpandDuration / 1000
    val currentMinutes = currentSeconds / 60
    val currentRemainingSeconds = currentSeconds % 60
    val currentFormattedDuration =
        String.format("%02d:%02d", currentMinutes, currentRemainingSeconds)

    BoxWithConstraints(
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            messageCallbacks.onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            }
    ) {
        val maxBubbleWidth = maxWidth * 0.85f
        Column() {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(widthExpand)
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = {
                            val willExpand = !isExpanded

                            if (isExpanded) {
                                setCurrentVideo("")
                            } else {
                                setCurrentVideo(message.messageId)
                            }

                            if (willExpand && globalIndex != null) {
                                coroutineScope.launch {
                                    delay(100.milliseconds)
                                    val extraMargin = with(density) { 32.dp.roundToPx() }
                                    val scrollOffset = -bottomPaddingPx - extraMargin

                                    lazyListState.animateScrollToItem(
                                        index = globalIndex,
                                        scrollOffset = scrollOffset
                                    )
                                }
                            }
                        },
                        onDoubleClick = {
                            messageCallbacks.onDoubleClick(
                                message.reactions?.get(messageData.mineId) != null
                            )
                        },
                        onLongClick = {
                            messageCallbacks.onLongClick(false)
                        }
                    )
            ) {
                CircleVideoPlayer(
                    message = message,
                    isExpanded = isExpanded,
                    onCycleEnded = {
                        setCurrentVideo("")
                    },
                    getCurrentDuration = { duration ->
                        currentExpandDuration = duration
                    },
                    currentVideoId = currentVideoId,
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
                        text = if (isExpanded) currentFormattedDuration else videoDuration
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    VideoInfo(
                        text = formattedTime
                    )
                }
            }

            AnimatedVisibility(
                visible = haveReaction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val hasBothDifferentReactions = reactions?.mineReaction != null &&
                        reactions.penpalReaction != null &&
                        reactions.mineReaction != reactions.penpalReaction

                if (hasBothDifferentReactions) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReactionWidget(
                            reactions = MessageReactions(
                                mineReaction = reactions.mineReaction,
                                penpalReaction = null
                            ),
                            color = Blue,
                            onReactionClick = {
                                messageCallbacks.onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReactionWidget(
                            reactions = MessageReactions(
                                mineReaction = null,
                                penpalReaction = reactions!!.penpalReaction
                            ),
                            color = Color(0xFFCCE3F8),
                            onReactionClick = {
                                messageCallbacks.onReactionClick()
                            },
                            mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                        )
                    }
                } else {
                    if (reactions?.mineReaction == null && reactions?.penpalReaction != null) {
                        ReactionWidget(
                            reactions = reactions,
                            color = Color(0xFFCCE3F8),
                            onReactionClick = {
                                messageCallbacks.onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                            penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                        )
                    } else {
                        ReactionWidget(
                            reactions = reactions,
                            color = Blue,
                            onReactionClick = {
                                messageCallbacks.onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                            penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun CirclePreview(
    message: Message.CircleVideo
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = CircleShape)
            .background(
                color = DateSeparatorGreen.copy(alpha = 0.65f),
            )
    ) {
        PreviewGenerator(
            message
        )
    }
}

@Composable
private fun PreviewGenerator(
    message: Message.CircleVideo
) {

}

@OptIn(UnstableApi::class)
@Composable
private fun CircleVideoPlayer(
    message: Message.CircleVideo,
    isExpanded: Boolean,
    onCycleEnded: () -> Unit,
    getCurrentDuration: (Long) -> Unit,
    currentVideoId: String,
) {
    val context = LocalContext.current

    var progress by remember { mutableFloatStateOf(0f) }

    val isCurrentActive = currentVideoId == message.messageId

    val localExoPlayer = remember(message.messageId) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(message.videoUrl ?: "")
            setMediaItem(mediaItem)
            prepare()

            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    DisposableEffect(isCurrentActive) {
        localExoPlayer.playWhenReady = isCurrentActive
        localExoPlayer.volume = if (isCurrentActive) 1f else 0f

        if (isCurrentActive) {
            localExoPlayer.seekTo(0)
        } else {
            localExoPlayer.pause()
        }
        onDispose { }
    }

    LaunchedEffect(localExoPlayer, isExpanded) {
        if (isExpanded) {
            while (true) {
                val currentPos = localExoPlayer.currentPosition
                val duration = localExoPlayer.duration
                if (duration > 0) {
                    progress = localExoPlayer.currentPosition.toFloat() / duration
                }
                getCurrentDuration(currentPos)
                delay(100)
            }
        } else {
            progress = 0f
            getCurrentDuration(0L)
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
            .background(
                color = DateSeparatorGreen.copy(alpha = 0.65f),
            )
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
        if (isExpanded) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                color = Color.White,
                trackColor = Color.Transparent,
                strokeWidth = 3.dp,
            )
        }
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