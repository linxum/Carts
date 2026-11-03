package com.smekhnyov.carts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smekhnyov.carts.ui.CartsApp
import com.smekhnyov.carts.ui.SettingsViewModel
import com.smekhnyov.carts.ui.theme.CartsTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            CartsTheme(settings = settings) {
                CartsApp(settingsViewModel = settingsViewModel)
            }
        }
    }
}
