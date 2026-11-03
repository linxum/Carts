package com.smekhnyov.carts.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.smekhnyov.carts.data.CardFormat

/**
 * Рисует код заново из строки. В базе и в файлах приложения нет изображений кодов —
 * только текст и формат, поэтому штрихкод всегда генерируется при показе.
 */
object BarcodeRenderer {

    fun render(
        content: String,
        format: CardFormat,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap? {
        if (content.isBlank() || widthPx <= 0 || heightPx <= 0) return null
        return try {
            val matrix = MultiFormatWriter().encode(
                content.trim(),
                format.zxingFormat,
                widthPx,
                heightPx,
                hints(format),
            )
            val pixels = IntArray(matrix.width * matrix.height) { index ->
                if (matrix[index % matrix.width, index / matrix.width]) Color.BLACK else Color.WHITE
            }
            Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isValid(content: String, format: CardFormat): Boolean {
        if (content.isBlank()) return false
        return try {
            MultiFormatWriter().encode(
                content.trim(),
                format.zxingFormat,
                240,
                240,
                hints(format),
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun hints(format: CardFormat): Map<EncodeHintType, Any> = buildMap {
        put(EncodeHintType.MARGIN, 2)
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
        if (format.zxingFormat == BarcodeFormat.QR_CODE) {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
        }
    }
}
