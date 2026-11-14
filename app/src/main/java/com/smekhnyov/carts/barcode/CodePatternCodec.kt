package com.smekhnyov.carts.barcode

import com.google.zxing.common.BitMatrix
import java.util.Base64

/**
 * Компактное представление рисунка 2D-кода: размеры и упакованные по строкам модули.
 * Позволяет показать ровно тот код, который был изначально отсканирован,
 * включая версию, маску, уровень коррекции, кодировку и форму (Data Matrix бывает
 * прямоугольным).
 *
 * Формат 1 (старые QR-рисунки): [1, ширина, высота] и далее модули.
 * Формат 2: [2, ширина(2 байта), высота(2 байта)] и далее модули.
 */
object CodePatternCodec {

    private const val LEGACY_FORMAT_VERSION = 1
    private const val FORMAT_VERSION = 2
    private const val LEGACY_HEADER_SIZE = 3
    private const val HEADER_SIZE = 5

    /** Верхняя граница на всякий случай: самые большие поддерживаемые коды — 177 модулей. */
    private const val MAX_DIMENSION = 1024

    fun encode(matrix: BitMatrix): String {
        val width = matrix.width
        val height = matrix.height
        val rowBytes = (width + 7) / 8
        val bytes = ByteArray(HEADER_SIZE + rowBytes * height)
        bytes[0] = FORMAT_VERSION.toByte()
        bytes[1] = (width ushr 8).toByte()
        bytes[2] = width.toByte()
        bytes[3] = (height ushr 8).toByte()
        bytes[4] = height.toByte()

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
        if (bytes.isEmpty()) return null

        val width: Int
        val height: Int
        val headerSize: Int
        when (bytes[0].toInt()) {
            LEGACY_FORMAT_VERSION -> {
                if (bytes.size < LEGACY_HEADER_SIZE) return null
                width = bytes[1].toInt() and 0xFF
                height = bytes[2].toInt() and 0xFF
                headerSize = LEGACY_HEADER_SIZE
            }

            FORMAT_VERSION -> {
                if (bytes.size < HEADER_SIZE) return null
                width = ((bytes[1].toInt() and 0xFF) shl 8) or (bytes[2].toInt() and 0xFF)
                height = ((bytes[3].toInt() and 0xFF) shl 8) or (bytes[4].toInt() and 0xFF)
                headerSize = HEADER_SIZE
            }

            else -> return null
        }
        if (width !in 1..MAX_DIMENSION || height !in 1..MAX_DIMENSION) return null

        val rowBytes = (width + 7) / 8
        if (bytes.size != headerSize + rowBytes * height) return null

        val matrix = BitMatrix(width, height)
        var offset = headerSize
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
