package com.example.lsmtraductor.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream


// Extensión para convertir una imagen en formato ImageProxy a Bitmap.
// ImageProxy es usado por CameraX como formato de imagen intermedio.

fun ImageProxy.toBitmap(): Bitmap {
    // Convertimos la imagen YUV_420_888 a NV21 (formato comprimido usado comúnmente)
    val nv21 = yuv420888ToNv21(this)

    // Creamos una imagen YUV a partir del arreglo NV21
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)

    // Stream para almacenar la imagen comprimida
    val out = ByteArrayOutputStream()

    // Comprimimos a formato JPEG con calidad 100
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)

    // Obtenemos el arreglo de bytes JPEG
    val imageBytes = out.toByteArray()

    // Decodificamos los bytes en un objeto Bitmap
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

//Convierte un objeto ImageProxy (formato YUV_420_888) al formato NV21.

fun yuv420888ToNv21(image: ImageProxy): ByteArray {
    // Obtenemos los buffers de cada plano de color: Y, U y V
    val yBuffer = image.planes[0].buffer // Luminancia
    val uBuffer = image.planes[1].buffer // Crominancia U
    val vBuffer = image.planes[2].buffer // Crominancia V

    // Calculamos el tamaño de cada buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    // Creamos el arreglo final NV21 con espacio suficiente
    val nv21 = ByteArray(ySize + uSize + vSize)

    // Copiamos los bytes del buffer Y (luminancia) al inicio del arreglo
    yBuffer.get(nv21, 0, ySize)

    // En NV21, el orden es: Y + V + U
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    // Retornamos el arreglo NV21 completo
    return nv21
}
