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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linc.audiowaveform.AudioWaveform
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.MineMessageTimeColor
import com.example.hydrogram.ui.theme.SfProText
import com.linc.audiowaveform.model.WaveformAlignment
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalLocale
import java.util.Date

@Composable
fun VoiceWidget(
    message: Message,
    isMine: Boolean,
    context: Context,
) {

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri((message as Message.Voice).audioUrl ?: "")
            setMediaItem(mediaItem)
            prepare()
        }
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

    Box(
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(63.dp)
                .widthIn(max = maxCardWidth)
                .clip(
                    shape = RoundedCornerShape(17.dp)
                )
                .background(
                    color = if (isMine) Green else Color.White
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                )
        ) {
            PlayButton(
                onClick = {},
            )
            Spacer(modifier = Modifier.width(10.dp))
            message.recordingAmplitudes?.let { amplitudes ->
                val exactWaveformWidth = (amplitudes.size * (2 + 2)).dp
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(exactWaveformWidth)
                ) {
                    Log.d("VoiceMessage", "amplitudes: ${message.recordingAmplitudes}")
                    AudioWaveform(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clipToBounds(),
                        amplitudes = amplitudes,
                        progress = if (totalDurationMs > 0) currentPosition.toFloat() / totalDurationMs else 0f,
                        onProgressChange = { progress ->
                            val seekToMs = (progress * totalDurationMs).toLong()
                            exoPlayer.seekTo(seekToMs)
                            currentPosition = seekToMs
                        },
                        waveformAlignment = WaveformAlignment.Center,
                        spikeWidth = 2.dp,
                        spikeRadius = 2.dp,
                        spikePadding = 2.dp,
                        progressBrush = SolidColor(Color.Gray),
                        waveformBrush = SolidColor(Color.White),
                    )
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
                            color = MineMessageTimeColor,
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(
                                    shape = CircleShape
                                )
                                .background(
                                    color = Color.Yellow,
                                )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(5.dp))
            Column(
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MineMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    if (message.status == "read") {
                        Icon(
                            painter = painterResource(R.drawable.ic_read_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_sent_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(45.dp)
            .clip(
                shape = CircleShape,
            )
            .clickable {
                onClick()
            }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null,
            tint = MineMessageTimeColor,
        )
    }
}