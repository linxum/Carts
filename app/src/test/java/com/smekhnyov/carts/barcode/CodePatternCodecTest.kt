package com.smekhnyov.carts.barcode

import com.google.zxing.common.BitMatrix
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodePatternCodecTest {

    @Test
    fun encodeAndDecode_roundTripsModules() {
        val matrix = patternedMatrix(21, 21)

        val decoded = CodePatternCodec.decode(CodePatternCodec.encode(matrix))

        assertArrayEquals(matrix.toRows(), decoded?.toRows())
    }

    @Test
    fun encodeAndDecode_handlesWideQrVersion() {
        val matrix = BitMatrix(177, 177)
        matrix.set(0, 0)
        matrix.set(176, 0)
        matrix.set(0, 176)
        matrix.set(176, 176)

        val decoded = CodePatternCodec.decode(CodePatternCodec.encode(matrix))

        assertArrayEquals(matrix.toRows(), decoded?.toRows())
    }

    @Test
    fun encodeAndDecode_handlesRectangularDataMatrix() {
        val matrix = patternedMatrix(18, 8)

        val decoded = CodePatternCodec.decode(CodePatternCodec.encode(matrix))

        assertEquals(18, decoded?.width)
        assertEquals(8, decoded?.height)
        assertArrayEquals(matrix.toRows(), decoded?.toRows())
    }

    @Test
    fun decode_readsLegacyVersionOnePattern() {
        val width = 21
        val height = 21
        val rowBytes = (width + 7) / 8
        val bytes = ByteArray(3 + rowBytes * height)
        bytes[0] = 1
        bytes[1] = width.toByte()
        bytes[2] = height.toByte()
        // Тот же рисунок, что и в encodeAndDecode_roundTripsModules.
        var offset = 3
        for (y in 0 until height) {
            for (rowByte in 0 until rowBytes) {
                var value = 0
                for (bit in 0 until 8) {
                    val x = rowByte * 8 + bit
                    if (x < width && (x * 7 + y * 3) % 5 == 0) {
                        value = value or (0x80 ushr bit)
                    }
                }
                bytes[offset++] = value.toByte()
            }
        }

        val decoded = CodePatternCodec.decode(Base64.getEncoder().encodeToString(bytes))

        assertArrayEquals(patternedMatrix(width, height).toRows(), decoded?.toRows())
    }

    @Test
    fun decode_returnsNullForInvalidInput() {
        assertNull(CodePatternCodec.decode(null))
        assertNull(CodePatternCodec.decode(""))
        assertNull(CodePatternCodec.decode("not base64!"))
        assertNull(CodePatternCodec.decode("AAAA"))
    }

    private fun patternedMatrix(width: Int, height: Int): BitMatrix =
        BitMatrix(width, height).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if ((x * 7 + y * 3) % 5 == 0) set(x, y)
                }
            }
        }

    private fun BitMatrix.toRows(): Array<BooleanArray> =
        Array(height) { y -> BooleanArray(width) { x -> get(x, y) } }
}
