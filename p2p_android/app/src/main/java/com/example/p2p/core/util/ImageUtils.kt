package com.example.p2p.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null

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
