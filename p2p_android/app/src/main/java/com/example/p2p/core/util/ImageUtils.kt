package com.example.p2p.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import java.io.ByteArrayOutputStream

private const val MAX_DIMENSION = 1600
private const val JPEG_QUALITY = 80

/**
 * Decodifica una imagen desde [uri] con downsampling (sin cargar la resolución completa
 * de la cámara a memoria) y la re-codifica como JPEG comprimido. Evita OutOfMemoryError
 * con fotos de 10+ MB en KYC/vouchers.
 */
fun compressImageFromUri(
    context: Context,
    uri: Uri,
    maxDimension: Int = MAX_DIMENSION,
    quality: Int = JPEG_QUALITY,
): ByteArray? {
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(uri) ?: return null
    // decodeStream con inJustDecodeBounds=true SIEMPRE devuelve null por diseño
    // (solo rellena bounds.outWidth/outHeight); no es una señal de error.
    boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
        sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, decodeOptions)
    } ?: return null

    return try {
        ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            output.toByteArray()
        }
    } finally {
        bitmap.recycle()
    }
}

/**
 * Renderiza los trazos de la firma electrónica (capturados como puntos en pantalla)
 * a un bitmap con fondo blanco, para poder subirla como imagen JPEG igual que el resto
 * de documentos KYC.
 */
fun signaturePathsToBytes(
    paths: List<List<Offset>>,
    width: Int = 600,
    height: Int = 300,
    quality: Int = 90,
): ByteArray? {
    if (paths.isEmpty()) return null

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        color = Color.rgb(0x1A, 0x23, 0x32)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    for (points in paths) {
        if (points.size < 2) continue
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        canvas.drawPath(path, paint)
    }

    return try {
        ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            output.toByteArray()
        }
    } finally {
        bitmap.recycle()
    }
}
