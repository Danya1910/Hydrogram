package com.example.hydrogram.presentation.util

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraHolder(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null

    var videoCapture: VideoCapture<Recorder>? = null
        private set

    // ✅ PreviewView создаётся СРАЗУ, а не в warmUp
    val previewView: PreviewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }

    var isReady by mutableStateOf(false)
        private set

    private var isWarmingUp = false

    fun warmUp(
        lifecycleOwner: LifecycleOwner,
        onReady: () -> Unit = {},
        onError: (Exception) -> Unit = {},
    ) {
        if (isReady) {
            onReady()
            return
        }
        if (isWarmingUp) return
        isWarmingUp = true

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                    .build()
                val capture = VideoCapture.withOutput(recorder)
                videoCapture = capture

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    capture,
                )

                isReady = true
                isWarmingUp = false
                onReady()
            } catch (e: Exception) {
                Log.e("CameraHolder", "Ошибка warmUp", e)
                isWarmingUp = false
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun release() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e("CameraHolder", "Ошибка release", e)
        }
        cameraProvider = null
        videoCapture = null
        isReady = false
        isWarmingUp = false
        // ✅ PreviewView НЕ пересоздаём, он живёт всё время
    }
}