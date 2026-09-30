package com.example.hydrogram.presentation.widgets

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.ui.theme.LightBlack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun VideoMessageRecorder(
    videoCapture: VideoCapture<Recorder>?,
    isRecordingTriggered: Boolean,
    isCanceled: Boolean,
    onVideoRecorded: (File, Long) -> Unit,
    currentDuration: (Long) -> Unit,
) {
    val context = LocalContext.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var currentRecording by remember { mutableStateOf<Recording?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    var wasCanceledByProp by remember { mutableStateOf(false) }

    var timerJob by remember { mutableStateOf<Job?>(null) }
    val animationScope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    @SuppressLint("MissingPermission")
    LaunchedEffect(isRecordingTriggered, isCanceled, videoCapture) {
        val vc = videoCapture ?: return@LaunchedEffect

        // Отмена
        if (isCanceled && currentRecording != null) {
            wasCanceledByProp = true
            currentRecording?.stop()
            return@LaunchedEffect
        }

        // Стоп
        if (!isRecordingTriggered && currentRecording != null) {
            currentRecording?.stop()
            return@LaunchedEffect
        }

        // Старт
        if (isRecordingTriggered && currentRecording == null) {
            wasCanceledByProp = false

            val outputFile = File(
                context.cacheDir,
                "circle_video_${System.currentTimeMillis()}.mp4"
            )
            currentOutputFile = outputFile

            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            val hasAudio = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            var pending = vc.output.prepareRecording(context, outputOptions)
            if (hasAudio) pending = pending.withAudioEnabled()

            currentRecording = pending.start(mainExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        animationScope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(60_000, easing = LinearEasing)
                            )
                        }
                        timerJob?.cancel()
                        val startTime = System.currentTimeMillis()
                        timerJob = animationScope.launch {
                            while (isActive) {
                                currentDuration(System.currentTimeMillis() - startTime)
                                delay(33)
                            }
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        timerJob?.cancel()
                        timerJob = null
                        animationScope.launch { progress.stop() }
                        currentRecording = null

                        if (wasCanceledByProp) {
                            currentOutputFile?.delete()
                            currentOutputFile = null
                            currentDuration(0L)
                        } else if (!event.hasError()) {
                            val ms = event.recordingStats.recordedDurationNanos / 1_000_000
                            currentDuration(ms)
                            onVideoRecorded(outputFile, ms)
                        } else {
                            Log.e("VideoRecorder", "Ошибка: ${event.error}")
                            currentOutputFile?.delete()
                            currentOutputFile = null
                            currentDuration(0L)
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentRecording?.stop()
            currentRecording = null
            timerJob?.cancel()
            timerJob = null
        }
    }

    // ✅ Рисуем ТОЛЬКО прогресс-бар. PreviewView снаружи.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(19.dp),   // 16 (padding превью) + 3 (толщина)
            color = Color.White,
            trackColor = Color.Transparent,
            strokeWidth = 3.dp,
        )
    }
}
