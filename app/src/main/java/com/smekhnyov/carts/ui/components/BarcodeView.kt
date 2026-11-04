package com.smekhnyov.carts.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smekhnyov.carts.R
import com.smekhnyov.carts.barcode.BarcodeRenderer
import com.smekhnyov.carts.data.CardFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface BarcodeState {
    data object Loading : BarcodeState
    data class Ready(val bitmap: ImageBitmap) : BarcodeState
    data object Error : BarcodeState
}

/**
 * Каждый раз отрисовывает код из строки [content] в битмап. Ключ [regenerationKey]
 * позволяет принудительно перегенерировать изображение (кнопка «Обновить»).
 */
@Composable
fun BarcodeView(
    content: String,
    format: CardFormat,
    modifier: Modifier = Modifier,
    regenerationKey: Int = 0,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx().toInt() }.coerceAtLeast(1)
        val maxHeightPx = with(density) { maxHeight.toPx().toInt() }.coerceAtLeast(1)
        val targetWidth = if (format.isSquare) minOf(maxWidthPx, maxHeightPx) else maxWidthPx
        val targetHeight = if (format.isSquare) minOf(maxWidthPx, maxHeightPx) else maxHeightPx

        val state by produceState<BarcodeState>(
            initialValue = BarcodeState.Loading,
            content,
            format,
            regenerationKey,
            targetWidth,
            targetHeight,
        ) {
            value = BarcodeState.Loading
            val bitmap = withContext(Dispatchers.Default) {
                BarcodeRenderer.render(content, format, targetWidth, targetHeight)
            }
            value = bitmap?.let { BarcodeState.Ready(it.asImageBitmap()) } ?: BarcodeState.Error
        }

        when (val current = state) {
            BarcodeState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            is BarcodeState.Ready -> {
                Image(
                    bitmap = current.bitmap,
                    contentDescription = stringResource(format.labelRes),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                )
            }

            BarcodeState.Error -> {
                Text(
                    text = stringResource(R.string.barcode_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
