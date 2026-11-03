package com.smekhnyov.carts.data

import android.net.Uri

data class LoyaltyCard(
    val id: Long,
    val name: String,
    val code: String,
    val format: CardFormat,
    val paletteIndex: Int,
    val emblemEmoji: String?,
    val emblemImagePath: String?,
    val createdAt: Long,
    val position: Int = 0,
) {
    val shortCode: String
        get() = if (code.length > 4) "•••• ${code.takeLast(4)}" else code
}

/** Данные формы добавления/редактирования, которые ещё не попали в базу. */
data class CardDraft(
    val id: Long? = null,
    val name: String,
    val code: String,
    val format: CardFormat,
    val paletteIndex: Int,
    val emblemEmoji: String?,
    val emblemImagePath: String?,
    /** Новый файл из системного выбора фото. Копируется в приватное хранилище при сохранении. */
    val newEmblemUri: Uri? = null,
    val removeEmblemImage: Boolean = false,
)
