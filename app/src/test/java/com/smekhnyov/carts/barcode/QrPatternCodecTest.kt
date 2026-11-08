package com.smekhnyov.carts.barcode

import com.google.zxing.common.BitMatrix
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrPatternCodecTest {

    @Test
    fun encodeAndDecode_roundTripsModules() {
        val matrix = BitMatrix(21, 21)
        for (y in 0 until 21) {
            for (x in 0 until 21) {
                if ((x * 7 + y * 3) % 5 == 0) matrix.set(x, y)
            }
        }

        val decoded = QrPatternCodec.decode(QrPatternCodec.encode(matrix))

        assertArrayEquals(matrix.toRows(), decoded?.toRows())
    }

    @Test
    fun encodeAndDecode_handlesWideVersion() {
        val matrix = BitMatrix(177, 177)
        matrix.set(0, 0)
        matrix.set(176, 0)
        matrix.set(0, 176)
        matrix.set(176, 176)

        val decoded = QrPatternCodec.decode(QrPatternCodec.encode(matrix))

        assertArrayEquals(matrix.toRows(), decoded?.toRows())
    }

    @Test
    fun decode_returnsNullForInvalidInput() {
        assertNull(QrPatternCodec.decode(null))
        assertNull(QrPatternCodec.decode(""))
        assertNull(QrPatternCodec.decode("not base64!"))
        assertNull(QrPatternCodec.decode("AAAA"))
    }

    private fun BitMatrix.toRows(): Array<BooleanArray> =
        Array(height) { y -> BooleanArray(width) { x -> get(x, y) } }
}
