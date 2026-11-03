package com.smekhnyov.carts.ui.cards

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Восемь выразительных градиентов для карт. В базе хранится только индекс,
 * поэтому оформление можно менять без миграций.
 */
data class CardPalette(
    val start: Color,
    val end: Color,
    val accent: Color,
    val onColor: Color,
    val glow: Color,
) {
    val brush: Brush
        get() = Brush.linearGradient(listOf(start, end))
}

val CardPalettes = listOf(
    CardPalette(
        start = Color(0xFF7C4DFF),
        end = Color(0xFF4318A8),
        accent = Color(0xFFD9C8FF),
        onColor = Color.White,
        glow = Color(0xFF9B7BFF),
    ),
    CardPalette(
        start = Color(0xFFFF7A7A),
        end = Color(0xFFC9184A),
        accent = Color(0xFFFFD6D9),
        onColor = Color.White,
        glow = Color(0xFFFF9AA2),
    ),
    CardPalette(
        start = Color(0xFFFFC24B),
        end = Color(0xFFF25C05),
        accent = Color(0xFFFFE7B8),
        onColor = Color(0xFF2B1500),
        glow = Color(0xFFFFD27A),
    ),
    CardPalette(
        start = Color(0xFF35DFA7),
        end = Color(0xFF007A5E),
        accent = Color(0xFFB9FFE9),
        onColor = Color.White,
        glow = Color(0xFF77F0CC),
    ),
    CardPalette(
        start = Color(0xFF55AFFF),
        end = Color(0xFF0B48B5),
        accent = Color(0xFFC9E4FF),
        onColor = Color.White,
        glow = Color(0xFF8AC8FF),
    ),
    CardPalette(
        start = Color(0xFFF472B6),
        end = Color(0xFF8E0F45),
        accent = Color(0xFFFFD1E8),
        onColor = Color.White,
        glow = Color(0xFFFF9CCF),
    ),
    CardPalette(
        start = Color(0xFF5B6472),
        end = Color(0xFF111827),
        accent = Color(0xFFD8DEE9),
        onColor = Color.White,
        glow = Color(0xFF94A3B8),
    ),
    CardPalette(
        start = Color(0xFFD9F99D),
        end = Color(0xFF4D7C0F),
        accent = Color(0xFFF2FFC8),
        onColor = Color(0xFF172000),
        glow = Color(0xFFBEF264),
    ),
)

fun paletteFor(index: Int): CardPalette = CardPalettes[index.mod(CardPalettes.size)]

val EmblemPresets = listOf(
    "🛍️", "🛒", "☕", "⛽", "🍔", "🍕",
    "💊", "🎁", "✂️", "🎬", "👟", "📚",
    "🌿", "💡", "🏠", "🚌", "🎧", "🍩",
    "🍣", "🧸",
)

const val DefaultEmblem = "🛍️"
