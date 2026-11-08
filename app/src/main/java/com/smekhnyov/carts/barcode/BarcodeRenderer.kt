package com.smekhnyov.carts.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.smekhnyov.carts.data.CardFormat
import kotlin.math.min

/**
 * Рисует код заново из строки. Для QR, добавленных сканированием, используется
 * сохранённый рисунок модулей — он повторяет исходный код вплоть до маски и версии.
 * В базе и в файлах приложения нет фотографий кодов.
 */
object BarcodeRenderer {

    fun render(
        content: String,
        format: CardFormat,
        widthPx: Int,
        heightPx: Int,
        qrPattern: String? = null,
    ): Bitmap? {
        if (content.isBlank() || widthPx <= 0 || heightPx <= 0) return null
        return try {
            val matrix = if (format.zxingFormat == BarcodeFormat.QR_CODE) {
                QrPatternCodec.decode(qrPattern)
                    ?.let { renderQrPattern(it, widthPx, heightPx) }
                    ?: generatedMatrix(content, format, widthPx, heightPx)
            } else {
                generatedMatrix(content, format, widthPx, heightPx)
            }
            matrix.toBitmap()
        } catch (_: Exception) {
            null
        }
    }

    fun isValid(content: String, format: CardFormat): Boolean {
        if (content.isBlank()) return false
        return try {
            generatedMatrix(content, format, 240, 240)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun generatedMatrix(
        content: String,
        format: CardFormat,
        widthPx: Int,
        heightPx: Int,
    ): BitMatrix = MultiFormatWriter().encode(
        content.trim(),
        format.zxingFormat,
        widthPx,
        heightPx,
        hints(format),
    )

    /**
     * Раскладывает сохранённые модули по пикселям с целым масштабом и полями в 2 модуля.
     * Итоговый размер может быть меньше целевого — изображение вписывается без сглаживания.
     */
    private fun renderQrPattern(pattern: BitMatrix, widthPx: Int, heightPx: Int): BitMatrix? {
        val margin = 2
        val columns = pattern.width + margin * 2
        val rows = pattern.height + margin * 2
        val module = min(widthPx / columns, heightPx / rows)
        if (module < 1) return null

        val output = BitMatrix(columns * module, rows * module)
        for (y in 0 until pattern.height) {
            for (x in 0 until pattern.width) {
                if (pattern.get(x, y)) {
                    output.setRegion(
                        (x + margin) * module,
                        (y + margin) * module,
                        module,
                        module,
                    )
                }
            }
        }
        return output
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val pixels = IntArray(width * height) { index ->
            if (get(index % width, index / width)) Color.BLACK else Color.WHITE
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
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
