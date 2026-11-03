package com.smekhnyov.carts.data

import com.google.zxing.BarcodeFormat

/**
 * Типы кодов, которые можно хранить и заново отрисовывать на карте.
 * В базе сохраняется только [name] формата и строка кода — никаких фотографий.
 */
enum class CardFormat(
    val label: String,
    val shortLabel: String,
    val zxingFormat: BarcodeFormat,
    val isSquare: Boolean = false,
    val isTwoDimensional: Boolean = false,
    val placeholder: String = "",
) {
    QR_CODE(
        label = "QR-код",
        shortLabel = "QR",
        zxingFormat = BarcodeFormat.QR_CODE,
        isSquare = true,
        isTwoDimensional = true,
        placeholder = "Текст или ссылка",
    ),
    DATA_MATRIX(
        label = "Data Matrix",
        shortLabel = "Data Matrix",
        zxingFormat = BarcodeFormat.DATA_MATRIX,
        isSquare = true,
        isTwoDimensional = true,
        placeholder = "Данные кода",
    ),
    AZTEC(
        label = "Aztec",
        shortLabel = "Aztec",
        zxingFormat = BarcodeFormat.AZTEC,
        isSquare = true,
        isTwoDimensional = true,
        placeholder = "Данные кода",
    ),
    PDF_417(
        label = "PDF417",
        shortLabel = "PDF417",
        zxingFormat = BarcodeFormat.PDF_417,
        isTwoDimensional = true,
        placeholder = "Данные кода",
    ),
    CODE_128(
        label = "Code 128",
        shortLabel = "Code 128",
        zxingFormat = BarcodeFormat.CODE_128,
        placeholder = "1234567890123",
    ),
    CODE_39(
        label = "Code 39",
        shortLabel = "Code 39",
        zxingFormat = BarcodeFormat.CODE_39,
        placeholder = "ABC-1234",
    ),
    CODE_93(
        label = "Code 93",
        shortLabel = "Code 93",
        zxingFormat = BarcodeFormat.CODE_93,
        placeholder = "ABC-1234",
    ),
    EAN_13(
        label = "EAN-13",
        shortLabel = "EAN-13",
        zxingFormat = BarcodeFormat.EAN_13,
        placeholder = "4600000000000",
    ),
    EAN_8(
        label = "EAN-8",
        shortLabel = "EAN-8",
        zxingFormat = BarcodeFormat.EAN_8,
        placeholder = "46000000",
    ),
    UPC_A(
        label = "UPC-A",
        shortLabel = "UPC-A",
        zxingFormat = BarcodeFormat.UPC_A,
        placeholder = "012345678905",
    ),
    UPC_E(
        label = "UPC-E",
        shortLabel = "UPC-E",
        zxingFormat = BarcodeFormat.UPC_E,
        placeholder = "01234565",
    ),
    ITF(
        label = "ITF",
        shortLabel = "ITF",
        zxingFormat = BarcodeFormat.ITF,
        placeholder = "12345678",
    ),
    CODABAR(
        label = "Codabar",
        shortLabel = "Codabar",
        zxingFormat = BarcodeFormat.CODABAR,
        placeholder = "A123456A",
    );

    companion object {
        fun fromStorage(value: String?): CardFormat =
            entries.firstOrNull { it.name == value } ?: QR_CODE

        /** Наиболее ходовые форматы показываем первыми в выборе. */
        val pickerOrder: List<CardFormat> = listOf(
            QR_CODE,
            DATA_MATRIX,
            CODE_128,
            EAN_13,
            PDF_417,
            AZTEC,
            CODE_39,
            CODE_93,
            EAN_8,
            UPC_A,
            UPC_E,
            ITF,
            CODABAR,
        )
    }
}
