package com.smekhnyov.carts.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToInt

class CardRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = CardsDatabase(appContext)
    private val emblemsDirectory = File(appContext.filesDir, "emblems").apply { mkdirs() }

    private val _cards = MutableStateFlow<List<LoyaltyCard>>(emptyList())
    val cards: StateFlow<List<LoyaltyCard>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        try {
            _cards.value = database.readAll()
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Сохраняет карту. Эмблема из галереи копируется в приватное хранилище приложения,
     * чтобы карта не зависела от временного доступа к выбранному файлу.
     */
    suspend fun save(draft: CardDraft): Long = withContext(Dispatchers.IO) {
        val oldCard = draft.id?.let(database::readById)
        var imagePath = if (draft.removeEmblemImage) null else oldCard?.emblemImagePath

        if (draft.newEmblemUri != null) {
            imagePath = importEmblem(draft.newEmblemUri)
                ?: throw IllegalStateException("Не удалось прочитать изображение эмблемы")
        }

        val cardId = if (oldCard == null) {
            database.insert(
                draft = draft,
                emblemImagePath = imagePath,
                createdAt = System.currentTimeMillis(),
            )
        } else {
            database.update(oldCard.id, draft, imagePath)
            oldCard.id
        }

        if (!oldCard?.emblemImagePath.isNullOrBlank() && oldCard.emblemImagePath != imagePath) {
            deleteEmblemFile(oldCard.emblemImagePath)
        }

        refreshNow()
        cardId
    }

    suspend fun delete(card: LoyaltyCard) = withContext(Dispatchers.IO) {
        val imagePath = database.readById(card.id)?.emblemImagePath
        database.delete(card.id)
        deleteEmblemFile(imagePath)
        refreshNow()
    }

    fun close() {
        database.close()
    }

    private fun refreshNow() {
        _cards.value = database.readAll()
        _isLoading.value = false
    }

    private fun importEmblem(uri: Uri): String? {
        return try {
            val source = ImageDecoder.createSource(appContext.contentResolver, uri)
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val target = 720
                val scale = min(
                    target.toFloat() / info.size.width.coerceAtLeast(1),
                    target.toFloat() / info.size.height.coerceAtLeast(1),
                ).coerceAtMost(1f)
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1),
                    )
                }
            }

            val side = min(decoded.width, decoded.height)
            val square = if (decoded.width == side && decoded.height == side) {
                decoded
            } else {
                Bitmap.createBitmap(
                    decoded,
                    (decoded.width - side) / 2,
                    (decoded.height - side) / 2,
                    side,
                    side,
                ).also { decoded.recycle() }
            }

            val file = File(emblemsDirectory, "emblem_${UUID.randomUUID()}.png")
            FileOutputStream(file).use { output ->
                square.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            square.recycle()
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun deleteEmblemFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            val emblemRoot = emblemsDirectory.canonicalPath + File.separator
            if (file.canonicalPath.startsWith(emblemRoot) && file.exists()) {
                file.delete()
            }
        }
    }
}
