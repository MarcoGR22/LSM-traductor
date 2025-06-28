package com.example.lsmtraductor.utils

import android.content.Context
import android.util.Log
import com.example.lsmtraductor.viewmodel.TranslatorViewModel
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

//Objeto que gestiona el detector de manos de MediaPipe (HandLandmarker).

object HandLandmarkerHelper {

    var liveLandmarker: HandLandmarker? = null

    fun crearLandmarkerLive(
        context: Context,
        viewModel: TranslatorViewModel,
        useGpu: Boolean = false, // Nuevo parámetro para usar GPU
        minDetectionConfidence: Float = 0.9f, // Confianza mínima para la detección
        minTrackingConfidence: Float = 0.9f, // Confianza mínima para el seguimiento
        minPresenceConfidence: Float = 0.7f // Confianza mínima para la presencia de la mano
    ): HandLandmarker? {
        return try {
            // Configuración base del modelo con delegación a GPU si es necesario
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .setDelegate(if (useGpu) Delegate.GPU else Delegate.CPU) // Usa GPU si se indica
                .build()

            val options = HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMinHandDetectionConfidence(minDetectionConfidence)
                .setMinTrackingConfidence(minTrackingConfidence)
                .setMinHandPresenceConfidence(minPresenceConfidence)
                .setNumHands(1)
                .setResultListener { result: HandLandmarkerResult, _ ->
                    val handLandmarks: List<NormalizedLandmark>? = result.landmarks().firstOrNull()
                    if (handLandmarks != null && handLandmarks.size == 21) {
                        Log.d("HandLandmarkerHelper", "🖐️ Landmarks detectados: ${handLandmarks.size}")
                        viewModel.procesarLandmarks(handLandmarks)
                    } else {
                        Log.d("HandLandmarkerHelper", "❌ No se detectaron landmarks válidos")
                    }
                }
                .setErrorListener { e ->
                    Log.e("HandLandmarkerHelper", "⚠️ Error en LiveStream: ${e.message}")
                }
                .build()

            HandLandmarker.createFromOptions(context, options).also {
                liveLandmarker = it
            }
        } catch (e: Exception) {
            Log.e("HandLandmarkerHelper", "❌ Error al crear HandLandmarker (LIVE)", e)
            null
        }
    }
}
