package com.smekhnyov.carts.barcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.Decoder
import com.google.zxing.qrcode.detector.Detector

/**
 * Достаёт из кадра или фотографии не только текст QR, но и точный рисунок модулей.
 * Рисунок сохраняется отдельно и используется при показе, чтобы код выглядел
 * ровно так же, как исходный.
 */
object QrPatternExtractor {

    data class Captured(val text: String, val encoded: String)

    private val hints = mapOf(
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8",
    )

    fun fromImageProxy(proxy: ImageProxy): Captured? {
        val plane = proxy.planes.firstOrNull() ?: return null
        val width = proxy.width
        val height = proxy.height
        if (width <= 0 || height <= 0) return null

        val luminance = ByteArray(width * height)
        val buffer = plane.buffer
        buffer.rewind()
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        if (pixelStride == 1 && rowStride == width) {
            buffer.get(luminance, 0, minOf(buffer.remaining(), luminance.size))
        } else {
            val row = ByteArray(rowStride)
            for (y in 0 until height) {
                val remaining = buffer.remaining()
                if (remaining <= 0) break
                val read = minOf(rowStride, remaining)
                buffer.get(row, 0, read)
                for (x in 0 until width) {
                    val sourceIndex = x * pixelStride
                    if (sourceIndex < read) {
                        luminance[y * width + x] = row[sourceIndex]
                    }
                }
            }
        }

        val source = PlanarYUVLuminanceSource(
            luminance,
            width,
            height,
            0,
            0,
            width,
            height,
            false,
        )
        return read(BinaryBitmap(HybridBinarizer(source)))
    }

    fun fromUri(context: Context, uri: Uri): Captured? {
        val bitmap = decodeSampledBitmap(context, uri) ?: return null
        return try {
            val width = bitmap.width
            val height = bitmap.height
            if (width <= 0 || height <= 0) return null
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            read(BinaryBitmap(HybridBinarizer(source)))
        } finally {
            bitmap.recycle()
        }
    }

    private fun read(bitmap: BinaryBitmap): Captured? = runCatching {
        val detected = Detector(bitmap.blackMatrix).detect(hints)
        val bits = detected.bits
        // ZXing Decoder снимает маску прямо в переданной матрице, поэтому рисунок
        // кодируем до декодирования текста — иначе сохранится уже изменённый код.
        val encoded = QrPatternCodec.encode(bits)
        val decoded = Decoder().decode(bits, hints)
        val text = decoded.text ?: return@runCatching null
        Captured(text = text, encoded = encoded)
    }.getOrNull()

    private fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // В режиме inJustDecodeBounds decodeStream всегда возвращает null,
        // поэтому проверяем только то, что поток удалось открыть.
        val boundsStream = resolver.openInputStream(uri) ?: return null
        boundsStream.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
        while (maxSide / sampleSize > MAX_PHOTO_SIDE) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private const val MAX_PHOTO_SIDE = 1600
}
