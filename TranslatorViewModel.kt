package com.example.lsmtraductor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lsmtraductor.utils.TensorFlowLiteHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// ViewModel que maneja la lógica del traductor
// Se encarga de procesar los puntos de la mano, predecir letras y gestionar el texto detectado.

class TranslatorViewModel(
    private val tfliteHelper: TensorFlowLiteHelper // Ayudante que usa el modelo de TensorFlow
) : ViewModel() {

    // Estado observable del texto detectado (letras acumuladas)
    private val _textoDetectado = MutableStateFlow("")
    val textoDetectado: StateFlow<String> = _textoDetectado

    // Estado observable para mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Última letra detectada, para evitar repeticiones
    private var lastLetter: String? = null

    // Procesa los puntos (landmarks) detectados en la mano y predice la letra correspondiente.
    fun procesarLandmarks(landmarks: List<NormalizedLandmark>) {
        try {
            // Tomamos como referencia el primer punto para normalizar
            val base = landmarks.first()

            // Restamos la posición base a cada punto y lo convertimos en pares (x, y)
            val puntos = landmarks.flatMap {
                listOf(it.x() - base.x(), it.y() - base.y())
            }

            // Se normaliza dividiendo entre el valor absoluto máximo
            val maxAbs = puntos.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(1e-6f) ?: 1f
            val normalized = puntos.map { it / maxAbs }

            Log.d("LANDMARKS", "🔍 Coordenadas normalizadas: ${normalized.size}")
            Log.d("TFLiteInput", normalized.joinToString())

            // Verificamos que haya exactamente 42 valores (21 puntos * 2 coordenadas)
            if (normalized.size == 42) {
                viewModelScope.launch {
                    val predIndex = tfliteHelper.predict(normalized)
                    val letra = tfliteHelper.getEtiqueta(predIndex)

                    if (!letra.isNullOrBlank() && letra != lastLetter) {
                        _textoDetectado.value += letra
                        lastLetter = letra
                        Log.d("TranslatorViewModel", "🔠 Letra detectada: $letra")
                    } else {
                        Log.d("TranslatorViewModel", "ℹ️ Letra repetida o inválida")
                    }
                }
            }
            else {
                Log.d("TranslatorViewModel", "❌ Coordenadas incompletas (${normalized.size})")
            }

        } catch (e: Exception) {
            Log.e("TranslatorViewModel", "❌ Error en procesarLandmarks", e)
        }
    }

    // Reinicia todo: borra texto y errores.
    fun reiniciar() {
        _textoDetectado.value = ""
        _errorMessage.value = null
        lastLetter = null
    }

    // Libera los recursos del modelo al destruirse el ViewModel.
    override fun onCleared() {
        tfliteHelper.close()
        super.onCleared()
    }
}