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
 * Рисует код заново из строки. Для QR, Data Matrix и Aztec, добавленных сканированием,
 * используется сохранённый рисунок модулей — он повторяет исходный код вплоть до
 * версии, маски и формы. В базе и в файлах приложения нет фотографий кодов.
 */
object BarcodeRenderer {

    fun render(
        content: String,
        format: CardFormat,
        widthPx: Int,
        heightPx: Int,
        codePattern: String? = null,
    ): Bitmap? {
        if (content.isBlank() || widthPx <= 0 || heightPx <= 0) return null
        return try {
            val stored = if (format.supportsExactPattern) {
                CodePatternCodec.decode(codePattern)?.takeIf { isValidPattern(it, format) }
            } else {
                null
            }
            val matrix = stored?.let { renderStoredPattern(it, widthPx, heightPx) }
                ?: generatedMatrix(content, format, widthPx, heightPx)
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
     * Пропорции сохраняются, поэтому прямоугольный Data Matrix не растягивается.
     */
    private fun renderStoredPattern(pattern: BitMatrix, widthPx: Int, heightPx: Int): BitMatrix? {
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

    /** Проверяет, что сохранённая матрица действительно соответствует формату кода. */
    private fun isValidPattern(pattern: BitMatrix, format: CardFormat): Boolean = when (format) {
        CardFormat.QR_CODE -> pattern.width == pattern.height && pattern.width in QR_SIZES
        CardFormat.DATA_MATRIX ->
            DATA_MATRIX_SIZES.contains(pattern.width to pattern.height)

        CardFormat.AZTEC -> pattern.width == pattern.height && pattern.width in AZTEC_SIZES
        else -> false
    }

    /** Допустимые размеры символов QR. */
    private val QR_SIZES: Set<Int> = (21..177 step 4).toSet()

    /**
     * Допустимые размеры символов Data Matrix. Прямоугольные символы встречаются
     * в обеих ориентациях, потому что рисунок мог быть считан повёрнутым.
     */
    private val DATA_MATRIX_SIZES: Set<Pair<Int, Int>> = buildSet {
        val squares = listOf(
            10, 12, 14, 16, 18, 20, 22, 24, 26, 32, 36, 40, 44, 48, 52, 64, 72, 80, 88,
            96, 104, 120, 132, 144,
        )
        squares.forEach { add(it to it) }
        val rectangles = listOf(
            8 to 18, 8 to 32, 8 to 48, 8 to 64, 8 to 80, 8 to 96, 8 to 120, 8 to 144,
            12 to 26, 12 to 36, 12 to 64, 12 to 88,
            16 to 36, 16 to 48, 16 to 64,
            20 to 36, 20 to 44, 20 to 64,
            22 to 48, 24 to 48, 24 to 64, 26 to 40, 26 to 48, 26 to 64,
        )
        rectangles.forEach {
            add(it)
            add(it.second to it.first)
        }
    }

    /** Допустимые размеры символов Aztec (компактные и полные). */
    private val AZTEC_SIZES: Set<Int> = setOf(
        15, 19, 23, 27, 31, 37, 41, 45, 49, 53, 57, 61, 67, 71, 75, 79, 83, 87, 91,
        95, 101, 105, 109, 113, 117, 121, 125, 131, 135, 139, 143, 147, 151,
    )

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
