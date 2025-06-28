package com.example.lsmtraductor.view

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lsmtraductor.utils.HandLandmarkerHelper
import com.google.mediapipe.framework.image.BitmapImageBuilder
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    useBackCamera: Boolean = true,
    flashEnabled: Boolean = false,
    isCameraActive: Boolean = true,
    useGpu: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current // Obtén el ciclo de vida actual
    val cameraExecutor = Executors.newSingleThreadExecutor()

    // Vista nativa de cámara de CameraX
    val previewView = PreviewView(context)

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    LaunchedEffect(isCameraActive, useBackCamera, flashEnabled) {
        if (!isCameraActive) return@LaunchedEffect

        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720)) // Resolución
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Solo el frame más reciente
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    // Usar la función de extensión toBitmap()
                    val bitmap = imageProxy.toBitmap()

                    // Pasar la imagen procesada al HandLandmarker
                    val mpImage = BitmapImageBuilder(bitmap).build()

                    // Llamada al detector de manos
                    HandLandmarkerHelper.liveLandmarker?.detectAsync(mpImage, System.nanoTime())

                    // Cerrar el ImageProxy después de procesarlo
                    imageProxy.close()
                }
            }

        val cameraSelector = if (useBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        try {
            cameraProvider.unbindAll() // Desvincula todas las cámaras activas
            cameraProvider.bindToLifecycle(
                lifecycleOwner, // Usamos el ciclo de vida actual aquí
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            exc.printStackTrace() // Maneja errores de vinculación de la cámara
        }
    }

    // Vista nativa mostrada dentro de Compose
    AndroidView(factory = { previewView }, modifier = modifier)
}
