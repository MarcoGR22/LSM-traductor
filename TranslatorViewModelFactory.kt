package com.example.lsmtraductor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lsmtraductor.utils.HandLandmarkerHelper
import com.example.lsmtraductor.utils.TensorFlowLiteHelper

class TranslatorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Se crea una instancia del ayudante de TensorFlow
        val tfliteHelper = TensorFlowLiteHelper(context)

        // Se crea el ViewModel y se le pasa el ayudante
        val viewModel = TranslatorViewModel(tfliteHelper)

        // Se inicializa el detector de manos y se conecta al ViewModel
        HandLandmarkerHelper.crearLandmarkerLive(context, viewModel)

        // Se retorna el ViewModel ya configurado y casteado correctamente
        return viewModel as T
    }
}
