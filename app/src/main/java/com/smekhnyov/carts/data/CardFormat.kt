package com.smekhnyov.carts.data

import androidx.annotation.StringRes
import com.google.zxing.BarcodeFormat
import com.smekhnyov.carts.R

/**
 * Типы кодов, которые можно хранить и заново отрисовывать на карте.
 * В базе сохраняется только [name] формата и строка кода — никаких фотографий.
 */
enum class CardFormat(
    @param:StringRes val labelRes: Int,
    val shortLabel: String,
    val zxingFormat: BarcodeFormat,
    val isSquare: Boolean = false,
    val isTwoDimensional: Boolean = false,
    @param:StringRes val placeholderRes: Int = 0,
) {
    QR_CODE(
        labelRes = R.string.format_qr_code,
        shortLabel = "QR",
        zxingFormat = BarcodeFormat.QR_CODE,
        isSquare = true,
        isTwoDimensional = true,
        placeholderRes = R.string.placeholder_qr,
    ),
    DATA_MATRIX(
        labelRes = R.string.format_data_matrix,
        shortLabel = "Data Matrix",
        zxingFormat = BarcodeFormat.DATA_MATRIX,
        isSquare = true,
        isTwoDimensional = true,
        placeholderRes = R.string.placeholder_code_data,
    ),
    AZTEC(
        labelRes = R.string.format_aztec,
        shortLabel = "Aztec",
        zxingFormat = BarcodeFormat.AZTEC,
        isSquare = true,
        isTwoDimensional = true,
        placeholderRes = R.string.placeholder_code_data,
    ),
    PDF_417(
        labelRes = R.string.format_pdf417,
        shortLabel = "PDF417",
        zxingFormat = BarcodeFormat.PDF_417,
        isTwoDimensional = true,
        placeholderRes = R.string.placeholder_code_data,
    ),
    CODE_128(
        labelRes = R.string.format_code_128,
        shortLabel = "Code 128",
        zxingFormat = BarcodeFormat.CODE_128,
        placeholderRes = R.string.placeholder_code_128,
    ),
    CODE_39(
        labelRes = R.string.format_code_39,
        shortLabel = "Code 39",
        zxingFormat = BarcodeFormat.CODE_39,
        placeholderRes = R.string.placeholder_code_39,
    ),
    CODE_93(
        labelRes = R.string.format_code_93,
        shortLabel = "Code 93",
        zxingFormat = BarcodeFormat.CODE_93,
        placeholderRes = R.string.placeholder_code_93,
    ),
    EAN_13(
        labelRes = R.string.format_ean_13,
        shortLabel = "EAN-13",
        zxingFormat = BarcodeFormat.EAN_13,
        placeholderRes = R.string.placeholder_ean_13,
    ),
    EAN_8(
        labelRes = R.string.format_ean_8,
        shortLabel = "EAN-8",
        zxingFormat = BarcodeFormat.EAN_8,
        placeholderRes = R.string.placeholder_ean_8,
    ),
    UPC_A(
        labelRes = R.string.format_upc_a,
        shortLabel = "UPC-A",
        zxingFormat = BarcodeFormat.UPC_A,
        placeholderRes = R.string.placeholder_upc_a,
    ),
    UPC_E(
        labelRes = R.string.format_upc_e,
        shortLabel = "UPC-E",
        zxingFormat = BarcodeFormat.UPC_E,
        placeholderRes = R.string.placeholder_upc_e,
    ),
    ITF(
        labelRes = R.string.format_itf,
        shortLabel = "ITF",
        zxingFormat = BarcodeFormat.ITF,
        placeholderRes = R.string.placeholder_itf,
    ),
    CODABAR(
        labelRes = R.string.format_codabar,
        shortLabel = "Codabar",
        zxingFormat = BarcodeFormat.CODABAR,
        placeholderRes = R.string.placeholder_codabar,
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
