package com.example.lsmtraductor.utils

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

// Clase que gestiona el modelo de inferencia de TensorFlow Lite.
// Se encarga de cargar el modelo .tflite, hacer predicciones y obtener etiquetas.

class TensorFlowLiteHelper(context: Context, useGpu: Boolean = false) {

    private val interpreter: Interpreter
    private val etiquetas: List<String>

    init {
        // Accede al modelo "model.tflite" en assets
        val assetFileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        val mappedByteBuffer: MappedByteBuffer =
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        // Crea el intérprete de TFLite con el modelo cargado, con opción de usar GPU
        val options = Interpreter.Options().apply {
            if (useGpu) {
                this.addDelegate(GpuDelegate()) // Usa el delegado GPU si está habilitado
            }
        }
        interpreter = Interpreter(mappedByteBuffer, options)

        // Carga las etiquetas
        etiquetas = FileUtil.loadLabels(context, "etiquetas_xy.txt")
    }

    // Ejecuta una predicción usando el modelo de TFLite con 42 valores de entrada.

    fun predict(landmarks: List<Float>): Int {
        // Validación de entrada
        if (landmarks.size != 42) {
            Log.e("TFLiteHelper", "❌ Entrada inválida: se esperaban 42 valores, se recibieron ${landmarks.size}")
            return -1
        }

        // Formato de entrada: array bidimensional [1][42]
        val input = arrayOf(landmarks.toFloatArray())
        // Arreglo de salida: un array con tantas posiciones como clases (etiquetas)
        val output = Array(1) { FloatArray(etiquetas.size) }

        // Log para depuración (entrada)
        Log.d("TFLiteInput", "👉 Entrada: ${landmarks.joinToString()}")

        // Se ejecuta la inferencia del modelo
        interpreter.run(input, output)

        // Log para depuración (salida)
        Log.d("TFLiteOutput", "🔍 Salida: ${output[0].joinToString()}")

        // Se busca el índice con la probabilidad más alta
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Log para depuración del resultado
        Log.d("TFLiteOutput", "✅ Predicción índice: $maxIndex -> ${etiquetas.getOrNull(maxIndex) ?: "Desconocida"}")

        return maxIndex
    }

    // Devuelve la etiqueta (letra) correspondiente a un índice.
    fun getEtiqueta(index: Int): String? {
        return etiquetas.getOrNull(index)
    }


    // Libera recursos del intérprete cuando ya no se usa.
        fun close() {
        interpreter.close()
    }
}
