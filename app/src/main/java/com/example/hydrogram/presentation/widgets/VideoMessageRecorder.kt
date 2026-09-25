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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hydrogram.ui.theme.LightBlack
import java.io.File


@Composable
fun VideoMessageRecorder(
    isRecordingTriggered: Boolean,
    isCanceled: Boolean,
    onVideoRecorded: (File, Long) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val videoCaptureState = remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var currentRecording by remember { mutableStateOf<Recording?>(null) }

    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    var wasCanceledByProp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProviderProvider = ProcessCameraProvider.getInstance(context)
        cameraProviderProvider.addListener({
            val cameraProvider = cameraProviderProvider.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)
            videoCaptureState.value = videoCapture

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    val videoCapture = videoCaptureState.value

    @SuppressLint("MissingPermission")
    LaunchedEffect(isRecordingTriggered, isCanceled, videoCapture) {
        if (videoCapture == null) return@LaunchedEffect

        if (isCanceled && currentRecording != null) {
            wasCanceledByProp = true
            currentRecording?.stop()
            return@LaunchedEffect
        }

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

            var pending = videoCapture.output
                .prepareRecording(context, outputOptions)

            if (hasAudio) {
                pending = pending.withAudioEnabled()
            }

            currentRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Status) {
                    val durationMillis = event.recordingStats.recordedDurationNanos / 1_000_000
                    //
                    //
                    // передвать текущее время записи
                    //
                    //
                }
                if (event is VideoRecordEvent.Finalize) {

                    currentRecording = null

                    if (wasCanceledByProp) {
                        currentOutputFile?.delete()
                        currentOutputFile = null
                        Log.d("VideoRecorder", "Запись отменена пользователем, файл удален.")
                    } else if (!event.hasError()) {
                        val finalDurationMillis = event.recordingStats.recordedDurationNanos / 1_000_000
                        onVideoRecorded(outputFile, finalDurationMillis)
                    } else {
                        Log.e("VideoRecorder", "Ошибка записи: ${event.error}")
                        currentOutputFile?.delete()
                        currentOutputFile = null
                    }
                }
            }
        }
        else if (!isRecordingTriggered && currentRecording != null) {
            currentRecording?.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentRecording?.stop()
            currentRecording = null
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = LightBlack)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
