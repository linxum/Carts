package com.smekhnyov.carts.data

import androidx.annotation.StringRes
import com.smekhnyov.carts.R

/** Режим оформления приложения. */
enum class ThemeMode(@param:StringRes val labelRes: Int) {
    SYSTEM(R.string.settings_theme_system),
    LIGHT(R.string.settings_theme_light),
    DARK(R.string.settings_theme_dark),
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/**
 * Цветовые акценты Material 3. Значения хранятся в ARGB, чтобы настройки
 * не зависели от Compose и могли читаться до создания темы.
 */
enum class AppAccent(
    @param:StringRes val labelRes: Int,
    val lightPrimary: Long,
    val lightOnPrimary: Long,
    val lightContainer: Long,
    val lightOnContainer: Long,
    val darkPrimary: Long,
    val darkOnPrimary: Long,
    val darkContainer: Long,
    val darkOnContainer: Long,
) {
    VIOLET(
        labelRes = R.string.accent_violet,
        lightPrimary = 0xFF6A3DE8,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFFE9DDFF,
        lightOnContainer = 0xFF25005A,
        darkPrimary = 0xFFCFBCFF,
        darkOnPrimary = 0xFF381E72,
        darkContainer = 0xFF4F378B,
        darkOnContainer = 0xFFEADDFF,
    ),
    BLUE(
        labelRes = R.string.accent_blue,
        lightPrimary = 0xFF0B57D0,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFFD3E3FD,
        lightOnContainer = 0xFF041E49,
        darkPrimary = 0xFFA8C7FA,
        darkOnPrimary = 0xFF062E6F,
        darkContainer = 0xFF0842A0,
        darkOnContainer = 0xFFD3E3FD,
    ),
    GREEN(
        labelRes = R.string.accent_green,
        lightPrimary = 0xFF146C2E,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFFC4EED0,
        lightOnContainer = 0xFF072711,
        darkPrimary = 0xFF6DD58C,
        darkOnPrimary = 0xFF0A3818,
        darkContainer = 0xFF0F5223,
        darkOnContainer = 0xFFC4EED0,
    ),
    ORANGE(
        labelRes = R.string.accent_orange,
        lightPrimary = 0xFF8B5000,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFFFFDCC0,
        lightOnContainer = 0xFF2D1600,
        darkPrimary = 0xFFFFB870,
        darkOnPrimary = 0xFF4A2800,
        darkContainer = 0xFF6A3B00,
        darkOnContainer = 0xFFFFDCC0,
    ),
    PINK(
        labelRes = R.string.accent_pink,
        lightPrimary = 0xFF9C3F66,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFFFFD9E2,
        lightOnContainer = 0xFF3D0022,
        darkPrimary = 0xFFFFB0C8,
        darkOnPrimary = 0xFF5E1137,
        darkContainer = 0xFF7E284E,
        darkOnContainer = 0xFFFFD9E2,
    ),
    TEAL(
        labelRes = R.string.accent_teal,
        lightPrimary = 0xFF006A6A,
        lightOnPrimary = 0xFFFFFFFF,
        lightContainer = 0xFF9CF1EF,
        lightOnContainer = 0xFF002020,
        darkPrimary = 0xFF80D5D4,
        darkOnPrimary = 0xFF003737,
        darkContainer = 0xFF005050,
        darkOnContainer = 0xFF9CF1EF,
    ),
    ;

    companion object {
        fun fromStorage(value: String?): AppAccent =
            entries.firstOrNull { it.name == value } ?: VIOLET
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val accent: AppAccent = AppAccent.VIOLET,
)
