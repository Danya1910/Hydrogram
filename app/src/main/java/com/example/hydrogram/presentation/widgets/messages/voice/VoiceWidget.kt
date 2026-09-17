package com.example.hydrogram.presentation.widgets.messages.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.times
import com.example.hydrogram.presentation.util.MessageCallbacks
import com.example.hydrogram.presentation.widgets.messages.text.MessageReactions
import com.example.hydrogram.ui.theme.Blue
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun VoiceWidget(
    message: Message,
    isMine: Boolean,
    context: Context,
    messageCallbacks: MessageCallbacks,
    mineId: String,
) {

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri((message as Message.Voice).audioUrl ?: "")
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    val validReactions = message.reactions
        ?.filterValues { it != null }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null
    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null


    message.reactions?.entries?.forEach { entry ->
        if (entry.key == mineId) {
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


    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }

    val totalDurationMs = remember((message as Message.Voice).durationSeconds) {
        (message.durationSeconds ?: 0) * 1000L
    }

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPosition = 0L
                    exoPlayer.seekTo(0)
                    exoPlayer.pause()
                }
            }

            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            delay(100L)
        }
    }

    val configuration = LocalConfiguration.current
    val maxCardWidth = (configuration.screenWidthDp * 0.8f).dp

    BoxWithConstraints(
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
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
            },
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .height(63.dp)
                .widthIn(max = maxCardWidth)
                .clip(
                    shape = RoundedCornerShape(17.dp)
                )
                .background(
                    color = if (isMine) Color(0xFFE3FFC6) else Color.White
                )
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        messageCallbacks.onDoubleClick(
                            message.reactions?.get(mineId) != null
                        )
                    },
                    onLongClick = {
                        messageCallbacks.onLongClick(
                            false
                        )
                    }
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                )
        ) {
            PlayButton(
                isPlaying = isPlaying,
                onClick = {
                    exoPlayer.togglePlay()
                },
                isMine = isMine,
            )
            Spacer(modifier = Modifier.width(10.dp))
            message.recordingAmplitudes?.let { amplitudes ->

                val exactWaveformWidth = (amplitudes.size * 4).dp

                Box(
                    modifier = Modifier
                        .widthIn(min = 45.dp, max = exactWaveformWidth)
                        .wrapContentHeight()
                ) {
                    val spikeWidthDp = 2.dp
                    val spikePaddingDp = 2.dp
                    val minSpikeHeightDp = 2.dp
                    val maxSpikeHeightDp = 16.dp

                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val spikeWidthPx = with(density) { spikeWidthDp.toPx() }
                    val spikePaddingPx = with(density) { spikePaddingDp.toPx() }
                    val minSpikeHeightPx = with(density) { minSpikeHeightDp.toPx() }
                    val maxSpikeHeightPx = with(density) { maxSpikeHeightDp.toPx() }

                    val exactWaveformWidth = (amplitudes.size * (spikeWidthDp + spikePaddingDp))

                    val maxRawAmplitude = amplitudes.maxOrNull()?.toFloat() ?: 1f

                    Column(
                        modifier = Modifier
                            .width(exactWaveformWidth)
                            .wrapContentHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxSpikeHeightDp)
                        ) {
                            val canvasHeight = size.height
                            val progress =
                                if (totalDurationMs > 0) currentPosition.toFloat() / totalDurationMs else 0f

                            val totalWaveformWidthPx =
                                amplitudes.size * (spikeWidthPx + spikePaddingPx) - spikePaddingPx

                            val cornerRadiusPx = 1.dp.toPx()

                            val waveColor = if (isMine) Color(0xFF97D187) else Color.Gray
                            val playedColor = if (isMine) Color(0xFF42C23A) else Blue

                            val sharpProgressGradient = Brush.linearGradient(
                                colorStops = arrayOf(
                                    0.0f to playedColor,
                                    progress to playedColor,
                                    (progress + 0.001f).coerceAtMost(1f) to waveColor,
                                    1.0f to waveColor
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(totalWaveformWidthPx, 0f)
                            )

                            amplitudes.forEachIndexed { index, amplitude ->
                                val rawProgress = amplitude / maxRawAmplitude
                                val spikeHeight =
                                    minSpikeHeightPx + (rawProgress * (maxSpikeHeightPx - minSpikeHeightPx))

                                val spikeLeftX = index * (spikeWidthPx + spikePaddingPx)
                                val y = canvasHeight - spikeHeight

                                drawRoundRect(
                                    brush = sharpProgressGradient,
                                    topLeft = androidx.compose.ui.geometry.Offset(spikeLeftX, y),
                                    size = androidx.compose.ui.geometry.Size(
                                        spikeWidthPx,
                                        spikeHeight
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                        cornerRadiusPx
                                    )
                                )
                            }
                        }



                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayTimeMs = if (isPlaying) currentPosition else totalDurationMs
                            val minutes = (displayTimeMs / 1000) / 60
                            val seconds = (displayTimeMs / 1000) % 60

                            Text(
                                text = String.format(
                                    LocalLocale.current.platformLocale,
                                    "%02d:%02d",
                                    minutes,
                                    seconds
                                ),
                                fontFamily = SfProText,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                letterSpacing = -(0.43).sp,
                                color = if (isMine) Color(0xFF42C23A) else Color.Gray,
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            if (message.status != "read") {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(color = if (isMine) Color(0xFF42C23A) else Blue)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(5.dp))
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isMine) Color(0xFF42C23A) else Color.Gray,
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(3.dp))
                        if (message.status == "read") {
                            Icon(
                                painter = painterResource(R.drawable.ic_read_status),
                                contentDescription = null,
                                tint = Color(0xFF42C23A),
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_sent_status),
                                contentDescription = null,
                                tint = Color(0xFF42C23A),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun ExoPlayer.togglePlay() {
    if (isPlaying) {
        pause()
    } else {
        play()
    }
}

@Composable
private fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    isMine: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(45.dp)
            .clip(
                shape = CircleShape,
            )
            .background(
                color = if (isMine) Color(0xFF42C23A) else Blue,
            )
            .clickable {
                onClick()
            }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(start = 3.dp),
        )
    }
}