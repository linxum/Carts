package com.smekhnyov.carts.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Хранит небольшие настройки оформления в SharedPreferences. */
class SettingsRepository(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        update(_settings.value.copy(themeMode = mode))
    }

    fun setUseDynamicColor(enabled: Boolean) {
        update(_settings.value.copy(useDynamicColor = enabled))
    }

    fun setAccent(accent: AppAccent) {
        update(_settings.value.copy(accent = accent))
    }

    private fun update(settings: AppSettings) {
        preferences.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.useDynamicColor)
            .putString(KEY_ACCENT, settings.accent.name)
            .apply()
        _settings.value = settings
    }

    private fun readSettings(): AppSettings = AppSettings(
        themeMode = ThemeMode.fromStorage(preferences.getString(KEY_THEME_MODE, null)),
        useDynamicColor = preferences.getBoolean(KEY_DYNAMIC_COLOR, false),
        accent = AppAccent.fromStorage(preferences.getString(KEY_ACCENT, null)),
    )

    private companion object {
        const val PREFERENCES_NAME = "carts_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_ACCENT = "accent"
    }
}
