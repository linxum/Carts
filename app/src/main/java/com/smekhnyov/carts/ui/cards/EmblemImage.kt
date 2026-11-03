package com.smekhnyov.carts.ui.cards

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun rememberEmblemImage(path: String?, targetSize: Int = 256): ImageBitmap? {
    val context = LocalContext.current
    val state by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = path,
        key2 = targetSize,
    ) {
        value = withContext(Dispatchers.IO) {
            loadEmblemFromPath(context, path, targetSize)?.asImageBitmap()
        }
    }
    return state
}

@Composable
fun rememberPickedImage(uri: Uri?, targetSize: Int = 256): ImageBitmap? {
    val context = LocalContext.current
    val state by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = uri,
        key2 = targetSize,
    ) {
        value = withContext(Dispatchers.IO) {
            uri?.let { loadEmblemFromUri(context, it, targetSize)?.asImageBitmap() }
        }
    }
    return state
}

private fun loadEmblemFromPath(context: Context, path: String?, targetSize: Int): Bitmap? {
    if (path.isNullOrBlank()) return null
    return runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        var sampleSize = 1
        while (
            options.outWidth / (sampleSize * 2) >= targetSize &&
            options.outHeight / (sampleSize * 2) >= targetSize
        ) {
            sampleSize *= 2
        }
        BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            },
        )
    }.getOrNull()
}

private fun loadEmblemFromUri(context: Context, uri: Uri, targetSize: Int): Bitmap? {
    return runCatching {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val scale = min(
                targetSize.toFloat() / info.size.width.coerceAtLeast(1),
                targetSize.toFloat() / info.size.height.coerceAtLeast(1),
            ).coerceAtMost(1f)
            if (scale < 1f) {
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1),
                )
            }
        }
    }.getOrNull()
}

fun File.isEmblemFile(context: Context): Boolean =
    runCatching {
        canonicalPath.startsWith(File(context.filesDir, "emblems").canonicalPath)
    }.getOrDefault(false)
