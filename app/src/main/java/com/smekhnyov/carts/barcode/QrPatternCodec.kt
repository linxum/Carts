package com.smekhnyov.carts.barcode

import com.google.zxing.common.BitMatrix
import java.util.Base64

/**
 * Компактное представление рисунка QR-кода: размеры и упакованные по строкам модули.
 * Позволяет показать ровно тот код, который был изначально отсканирован,
 * включая версию, маску, уровень коррекции и кодировку.
 */
object QrPatternCodec {

    private const val FORMAT_VERSION = 1
    private const val HEADER_SIZE = 3
    private const val MIN_SIZE = 21
    private const val MAX_SIZE = 177

    fun encode(matrix: BitMatrix): String {
        val width = matrix.width
        val height = matrix.height
        val rowBytes = (width + 7) / 8
        val bytes = ByteArray(HEADER_SIZE + rowBytes * height)
        bytes[0] = FORMAT_VERSION.toByte()
        bytes[1] = width.toByte()
        bytes[2] = height.toByte()

        var offset = HEADER_SIZE
        for (y in 0 until height) {
            for (rowByte in 0 until rowBytes) {
                var value = 0
                for (bit in 0 until 8) {
                    val x = rowByte * 8 + bit
                    if (x < width && matrix.get(x, y)) {
                        value = value or (0x80 ushr bit)
                    }
                }
                bytes[offset++] = value.toByte()
            }
        }
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decode(encoded: String?): BitMatrix? {
        if (encoded.isNullOrBlank()) return null
        val bytes = try {
            Base64.getDecoder().decode(encoded)
        } catch (_: IllegalArgumentException) {
            return null
        }
        if (bytes.size < HEADER_SIZE || bytes[0].toInt() != FORMAT_VERSION) return null

        val width = bytes[1].toInt() and 0xFF
        val height = bytes[2].toInt() and 0xFF
        if (width != height || width !in MIN_SIZE..MAX_SIZE) return null

        val rowBytes = (width + 7) / 8
        if (bytes.size != HEADER_SIZE + rowBytes * height) return null

        val matrix = BitMatrix(width, height)
        var offset = HEADER_SIZE
        for (y in 0 until height) {
            for (rowByte in 0 until rowBytes) {
                val value = bytes[offset++].toInt() and 0xFF
                for (bit in 0 until 8) {
                    val x = rowByte * 8 + bit
                    if (x < width && (value and (0x80 ushr bit)) != 0) {
                        matrix.set(x, y)
                    }
                }
            }
        }
        return matrix
    }
}
