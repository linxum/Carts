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
import com.google.zxing.aztec.decoder.Decoder as AztecDecoder
import com.google.zxing.aztec.detector.Detector as AztecDetector
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.datamatrix.decoder.Decoder as DataMatrixDecoder
import com.google.zxing.datamatrix.detector.Detector as DataMatrixDetector
import com.google.zxing.qrcode.decoder.Decoder as QrDecoder
import com.google.zxing.qrcode.detector.Detector as QrDetector
import com.smekhnyov.carts.data.CardFormat

/**
 * Достаёт из кадра или фотографии не только текст 2D-кода, но и точный рисунок модулей.
 * Рисунок сохраняется отдельно и используется при показе, чтобы код выглядел
 * ровно так же, как исходный. Поддерживаются QR, Data Matrix и Aztec.
 */
object CodePatternExtractor {

    data class Captured(val text: String, val encoded: String)

    private val hints = mapOf(
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8",
    )

    fun fromImageProxy(proxy: ImageProxy, format: CardFormat): Captured? {
        if (!format.supportsExactPattern) return null
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
        return read(BinaryBitmap(HybridBinarizer(source)), format)
    }

    fun fromUri(context: Context, uri: Uri, format: CardFormat): Captured? {
        if (!format.supportsExactPattern) return null
        val bitmap = decodeSampledBitmap(context, uri) ?: return null
        return try {
            val width = bitmap.width
            val height = bitmap.height
            if (width <= 0 || height <= 0) return null
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            read(BinaryBitmap(HybridBinarizer(source)), format)
        } finally {
            bitmap.recycle()
        }
    }

    private fun read(bitmap: BinaryBitmap, format: CardFormat): Captured? = runCatching {
        when (format) {
            CardFormat.DATA_MATRIX -> readDataMatrix(bitmap)
            CardFormat.AZTEC -> readAztec(bitmap)
            else -> readQr(bitmap)
        }
    }.getOrNull()

    private fun readQr(bitmap: BinaryBitmap): Captured? {
        val detected = QrDetector(bitmap.blackMatrix).detect(hints)
        val bits = detected.bits
        // ZXing Decoder снимает маску прямо в переданной матрице, поэтому рисунок
        // кодируем до декодирования текста — иначе сохранится уже изменённый код.
        val encoded = CodePatternCodec.encode(bits)
        val text = QrDecoder().decode(bits, hints).text ?: return null
        return Captured(text = text, encoded = encoded)
    }

    private fun readDataMatrix(bitmap: BinaryBitmap): Captured? {
        val bits = DataMatrixDetector(bitmap.blackMatrix).detect().bits
        val encoded = CodePatternCodec.encode(bits)
        val text = DataMatrixDecoder().decode(bits).text ?: return null
        return Captured(text = text, encoded = encoded)
    }

    private fun readAztec(bitmap: BinaryBitmap): Captured? {
        val detected = AztecDetector(bitmap.blackMatrix).detect()
        val encoded = CodePatternCodec.encode(detected.bits)
        val text = AztecDecoder().decode(detected).text ?: return null
        return Captured(text = text, encoded = encoded)
    }

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
