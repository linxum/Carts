package com.smekhnyov.carts.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import com.smekhnyov.carts.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
                ?: throw IllegalStateException(appContext.getString(R.string.error_read_emblem))
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

    /** Меняет карту местами с соседней. Направление: -1 — выше, +1 — ниже. */
    suspend fun move(cardId: Long, direction: Int): Boolean = withContext(Dispatchers.IO) {
        val moved = database.move(cardId, direction)
        if (moved) refreshNow()
        moved
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        val imagePaths = database.readAll().mapNotNull { it.emblemImagePath }
        database.deleteAll()
        imagePaths.forEach(::deleteEmblemFile)
        refreshNow()
    }

    /**
     * Экспортирует карты в JSON. Эмблемы из галереи кодируются в Base64,
     * чтобы резервная копия была самодостаточной и переносилась на другое устройство.
     */
    suspend fun exportTo(uri: Uri): Int = withContext(Dispatchers.IO) {
        val cards = database.readAll()
        val cardsJson = JSONArray()
        cards.forEach { card ->
            cardsJson.put(
                JSONObject().apply {
                    put("name", card.name)
                    put("code", card.code)
                    put("format", card.format.name)
                    put("palette", card.paletteIndex)
                    card.emblemEmoji?.let { put("emblemEmoji", it) }
                    card.emblemImagePath?.let { path ->
                        readEmblemBase64(path)?.let { put("emblemImage", it) }
                    }
                },
            )
        }

        val root = JSONObject().apply {
            put("app", "Carts")
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("cards", cardsJson)
        }

        appContext.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException(appContext.getString(R.string.error_open_output))

        cards.size
    }

    /**
     * Импортирует карты из JSON-файла и добавляет их в конец списка.
     * Возвращает количество добавленных карт.
     */
    suspend fun importFrom(uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = appContext.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: throw IllegalStateException(appContext.getString(R.string.error_read_file))

        val root = JSONObject(text)
        val cardsJson = root.optJSONArray("cards")
            ?: throw IllegalArgumentException(appContext.getString(R.string.error_no_card_data))

        var imported = 0
        for (index in 0 until cardsJson.length()) {
            val item = cardsJson.optJSONObject(index) ?: continue
            val name = item.optString("name").trim()
            val code = item.optString("code").trim()
            if (name.isEmpty() || code.isEmpty()) continue

            val format = CardFormat.fromStorage(item.optString("format"))
            val imagePath = item.optString("emblemImage")
                .takeIf { it.isNotBlank() }
                ?.let(::importEmblemBase64)

            val draft = CardDraft(
                name = name,
                code = code,
                format = format,
                paletteIndex = item.optInt("palette", 0),
                emblemEmoji = item.optString("emblemEmoji").takeIf { it.isNotBlank() },
                emblemImagePath = imagePath,
            )
            database.insert(
                draft = draft,
                emblemImagePath = imagePath,
                createdAt = System.currentTimeMillis(),
            )
            imported += 1
        }

        refreshNow()
        imported
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

    private fun readEmblemBase64(path: String): String? = runCatching {
        Base64.encodeToString(File(path).readBytes(), Base64.NO_WRAP)
    }.getOrNull()

    private fun importEmblemBase64(encoded: String): String? = runCatching {
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        if (bytes.isEmpty()) return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        val file = File(emblemsDirectory, "emblem_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { output -> output.write(bytes) }
        file.absolutePath
    }.getOrNull()

    companion object {
        private const val BACKUP_VERSION = 1
    }
}
