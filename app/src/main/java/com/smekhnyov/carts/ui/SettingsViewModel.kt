package com.smekhnyov.carts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.smekhnyov.carts.data.AppAccent
import com.smekhnyov.carts.data.SettingsRepository
import com.smekhnyov.carts.data.ThemeMode

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val settings = repository.settings

    fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)

    fun setUseDynamicColor(enabled: Boolean) = repository.setUseDynamicColor(enabled)

    fun setUseDynamicColorForCards(enabled: Boolean) =
        repository.setUseDynamicColorForCards(enabled)

    fun setAccent(accent: AppAccent) = repository.setAccent(accent)
}
