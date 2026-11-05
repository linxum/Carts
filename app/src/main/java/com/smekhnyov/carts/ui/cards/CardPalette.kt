package com.smekhnyov.carts.ui.cards

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.smekhnyov.carts.ui.theme.LocalCardColorScheme

/**
 * Оформление карты строится на цветовых ролях Material 3, поэтому карты
 * автоматически подстраиваются под светлую/тёмную тему и выбранный акцент.
 * Градиенты не используются — только сплошные поверхности.
 */
data class CardPalette(
    val container: Color,
    val content: Color,
    val accent: Color,
)

const val CardPaletteCount = 8

@Composable
fun paletteFor(index: Int, customColor: Long? = null): CardPalette {
    if (customColor != null) {
        return customPalette(Color(customColor))
    }
    val scheme = LocalCardColorScheme.current ?: MaterialTheme.colorScheme
    val palettes = listOf(
        CardPalette(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            accent = scheme.primary,
        ),
        CardPalette(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            accent = scheme.secondary,
        ),
        CardPalette(
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer,
            accent = scheme.tertiary,
        ),
        CardPalette(
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurface,
            accent = scheme.primary,
        ),
        CardPalette(
            container = scheme.primary,
            content = scheme.onPrimary,
            accent = scheme.onPrimary,
        ),
        CardPalette(
            container = scheme.secondary,
            content = scheme.onSecondary,
            accent = scheme.onSecondary,
        ),
        CardPalette(
            container = scheme.tertiary,
            content = scheme.onTertiary,
            accent = scheme.onTertiary,
        ),
        CardPalette(
            container = scheme.inverseSurface,
            content = scheme.inverseOnSurface,
            accent = scheme.inverseOnSurface,
        ),
    )
    return palettes[index.mod(palettes.size)]
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
