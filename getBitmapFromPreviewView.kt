package com.example.lsmtraductor.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.camera.view.PreviewView
import androidx.core.graphics.createBitmap

// Captura la imagen actual mostrada en una PreviewView como un Bitmap.

fun getBitmapFromPreviewView(previewView: PreviewView): Bitmap? {
    return try {
        // Se crea un bitmap con las dimensiones exactas de la vista de cámara
        val bitmap = createBitmap(previewView.width, previewView.height)

        // Se crea un lienzo sobre el bitmap para dibujar la vista
        val canvas = Canvas(bitmap)
        // Se dibuja el contenido actual del PreviewView en el bitmap
        previewView.draw(canvas)

        // Se retorna el bitmap generado
        bitmap
    } catch (e: Exception) {
        // Si ocurre un error se imprime en consola y se retorna null
        e.printStackTrace()
        null
    }
}
