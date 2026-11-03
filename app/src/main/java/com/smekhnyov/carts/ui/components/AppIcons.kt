package com.smekhnyov.carts.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Небольшие собственные иконки, чтобы не подключать тяжёлый material-icons-extended. */
object AppIcons {

    val Scan: ImageVector by lazy {
        ImageVector.Builder(
            name = "Scan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f)
                verticalLineTo(5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                horizontalLineTo(9f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(9f)
                close()
                moveTo(21f, 9f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                horizontalLineTo(15f)
                verticalLineTo(5f)
                horizontalLineTo(19f)
                verticalLineTo(9f)
                close()
                moveTo(3f, 15f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                horizontalLineTo(9f)
                verticalLineTo(19f)
                horizontalLineTo(5f)
                verticalLineTo(15f)
                close()
                moveTo(21f, 15f)
                verticalLineTo(19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                horizontalLineTo(15f)
                verticalLineTo(19f)
                horizontalLineTo(19f)
                verticalLineTo(15f)
                close()
                moveTo(7f, 11f)
                horizontalLineTo(17f)
                verticalLineTo(13f)
                horizontalLineTo(7f)
                close()
            }
        }.build()
    }

    val Flashlight: ImageVector by lazy {
        ImageVector.Builder(
            name = "Flashlight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11f, 21f)
                horizontalLineTo(10f)
                lineTo(11f, 14f)
                horizontalLineTo(7.5f)
                curveTo(6.92f, 14f, 6.93f, 13.68f, 7.12f, 13.34f)
                curveTo(7.31f, 13f, 7.17f, 13.26f, 7.19f, 13.22f)
                curveTo(8.48f, 10.94f, 10.42f, 7.54f, 13f, 3f)
                horizontalLineTo(14f)
                lineTo(13f, 10f)
                horizontalLineTo(16.5f)
                curveTo(16.99f, 10f, 17.06f, 10.33f, 16.97f, 10.51f)
                lineTo(16.9f, 10.66f)
                curveTo(12.96f, 17.55f, 11f, 21f, 11f, 21f)
                close()
            }
        }.build()
    }

    val Photo: ImageVector by lazy {
        ImageVector.Builder(
            name = "Photo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 19f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                close()
                moveTo(8.5f, 13.5f)
                lineTo(11f, 16.51f)
                lineTo(14.5f, 12f)
                lineTo(19f, 18f)
                horizontalLineTo(5f)
                close()
                moveTo(8.5f, 9.5f)
                curveTo(8.5f, 8.67f, 9.17f, 8f, 10f, 8f)
                curveTo(10.83f, 8f, 11.5f, 8.67f, 11.5f, 9.5f)
                curveTo(11.5f, 10.33f, 10.83f, 11f, 10f, 11f)
                curveTo(9.17f, 11f, 8.5f, 10.33f, 8.5f, 9.5f)
                close()
            }
        }.build()
    }

    val Copy: ImageVector by lazy {
        ImageVector.Builder(
            name = "Copy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f)
                horizontalLineTo(4f)
                curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
                verticalLineTo(17f)
                horizontalLineTo(4f)
                verticalLineTo(3f)
                horizontalLineTo(16f)
                close()
                moveTo(19f, 5f)
                horizontalLineTo(8f)
                curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
                verticalLineTo(21f)
                curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
                horizontalLineTo(19f)
                curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
                verticalLineTo(7f)
                curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(19f, 21f)
                horizontalLineTo(8f)
                verticalLineTo(7f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
    }

    val Barcode: ImageVector by lazy {
        ImageVector.Builder(
            name = "Barcode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2f, 5f)
                horizontalLineTo(4f)
                verticalLineTo(19f)
                horizontalLineTo(2f)
                close()
                moveTo(6f, 5f)
                horizontalLineTo(9f)
                verticalLineTo(19f)
                horizontalLineTo(6f)
                close()
                moveTo(11f, 5f)
                horizontalLineTo(12.5f)
                verticalLineTo(19f)
                horizontalLineTo(11f)
                close()
                moveTo(15f, 5f)
                horizontalLineTo(17f)
                verticalLineTo(19f)
                horizontalLineTo(15f)
                close()
                moveTo(19f, 5f)
                horizontalLineTo(22f)
                verticalLineTo(19f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
    }

    val QrCode: ImageVector by lazy {
        ImageVector.Builder(
            name = "QrCode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 3f)
                horizontalLineTo(10f)
                verticalLineTo(10f)
                horizontalLineTo(3f)
                close()
                moveTo(5f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(8f)
                verticalLineTo(5f)
                close()
                moveTo(14f, 3f)
                horizontalLineTo(21f)
                verticalLineTo(10f)
                horizontalLineTo(14f)
                close()
                moveTo(16f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(19f)
                verticalLineTo(5f)
                close()
                moveTo(3f, 14f)
                horizontalLineTo(10f)
                verticalLineTo(21f)
                horizontalLineTo(3f)
                close()
                moveTo(5f, 16f)
                verticalLineTo(19f)
                horizontalLineTo(8f)
                verticalLineTo(16f)
                close()
                moveTo(12f, 12f)
                horizontalLineTo(14f)
                verticalLineTo(14f)
                horizontalLineTo(12f)
                close()
                moveTo(16f, 12f)
                horizontalLineTo(21f)
                verticalLineTo(14f)
                horizontalLineTo(16f)
                close()
                moveTo(12f, 16f)
                horizontalLineTo(14f)
                verticalLineTo(21f)
                horizontalLineTo(12f)
                close()
                moveTo(16f, 16f)
                horizontalLineTo(18f)
                verticalLineTo(18f)
                horizontalLineTo(16f)
                close()
                moveTo(19f, 19f)
                horizontalLineTo(21f)
                verticalLineTo(21f)
                horizontalLineTo(19f)
                close()
                moveTo(18f, 14f)
                horizontalLineTo(21f)
                verticalLineTo(16f)
                horizontalLineTo(18f)
                close()
            }
        }.build()
    }

    val DataMatrix: ImageVector by lazy {
        ImageVector.Builder(
            name = "DataMatrix",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 3f)
                horizontalLineTo(21f)
                verticalLineTo(21f)
                horizontalLineTo(3f)
                close()
                moveTo(5f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(8f)
                verticalLineTo(5f)
                close()
                moveTo(9.5f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(12.5f)
                verticalLineTo(5f)
                close()
                moveTo(14f, 5f)
                verticalLineTo(8f)
                horizontalLineTo(17f)
                verticalLineTo(5f)
                close()
                moveTo(5f, 9.5f)
                verticalLineTo(12.5f)
                horizontalLineTo(8f)
                verticalLineTo(9.5f)
                close()
                moveTo(9.5f, 9.5f)
                verticalLineTo(12.5f)
                horizontalLineTo(12.5f)
                verticalLineTo(9.5f)
                close()
                moveTo(14f, 9.5f)
                verticalLineTo(12.5f)
                horizontalLineTo(17f)
                verticalLineTo(9.5f)
                close()
                moveTo(5f, 14f)
                verticalLineTo(17f)
                horizontalLineTo(8f)
                verticalLineTo(14f)
                close()
                moveTo(9.5f, 14f)
                verticalLineTo(17f)
                horizontalLineTo(12.5f)
                verticalLineTo(14f)
                close()
                moveTo(14f, 14f)
                verticalLineTo(17f)
                horizontalLineTo(17f)
                verticalLineTo(14f)
                close()
            }
        }.build()
    }
}
