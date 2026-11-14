package com.smekhnyov.carts.ui.cards

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Оформление карты не зависит от темы приложения: цвета пресетов фиксированы,
 * поэтому смена светлой/тёмной темы, акцента или цветов обоев не перекрашивает
 * карты. Градиенты не используются — только сплошные поверхности.
 */
data class CardPalette(
    val container: Color,
    val content: Color,
    val accent: Color,
)

const val CardPaletteCount = 8

/** Фиксированные цвета пресетов, одинаковые в любой теме. */
private val PresetColors = listOf(
    Color(0xFFEADDFF), // лавандовый
    Color(0xFFE8DEF8), // сиреневый
    Color(0xFFFFD8E4), // розовый
    Color(0xFFE6E1E5), // нейтральный
    Color(0xFF6750A4), // фиолетовый
    Color(0xFF625B71), // серо-фиолетовый
    Color(0xFF7D5260), // винный
    Color(0xFF313033), // графитовый
)

/** Цвет карты берётся из БД: индекс пресета или пользовательский цвет. */
fun paletteFor(index: Int, customColor: Long? = null): CardPalette {
    if (customColor != null) {
        return customPalette(Color(customColor))
    }
    return customPalette(PresetColors[index.mod(PresetColors.size)])
}

/** Любой пользовательский цвет с автоматическим контрастным содержимым. */
fun customPalette(color: Color): CardPalette {
    // Порог 0.179 выбран по формуле контраста WCAG: он даёт максимальный
    // контраст чёрного или белого текста на произвольном фоне.
    val content = if (color.luminance() > 0.179f) {
        Color(0xFF1C1B1F)
    } else {
        Color(0xFFFFFFFF)
    }
    return CardPalette(
        container = color,
        content = content,
        accent = content,
    )
}

val EmblemPresets = listOf(
    "🛍️", "🛒", "☕", "⛽", "🍔", "🍕",
    "💊", "🎁", "✂️", "🎬", "👟", "📚",
    "🌿", "💡", "🏠", "🚌", "🎧", "🍩",
    "🍣", "🧸",
)

const val DefaultEmblem = "🛍️"
