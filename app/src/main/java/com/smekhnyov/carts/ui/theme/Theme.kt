package com.smekhnyov.carts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smekhnyov.carts.data.AppAccent
import com.smekhnyov.carts.data.AppSettings
import com.smekhnyov.carts.data.ThemeMode

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun CartsTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        settings.useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme.withAccent(settings.accent, dark = true)
        else -> LightColorScheme.withAccent(settings.accent, dark = false)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        typography = Typography,
        content = content,
    )
}

private fun ColorScheme.withAccent(accent: AppAccent, dark: Boolean): ColorScheme =
    if (dark) {
        copy(
            primary = Color(accent.darkPrimary),
            onPrimary = Color(accent.darkOnPrimary),
            primaryContainer = Color(accent.darkContainer),
            onPrimaryContainer = Color(accent.darkOnContainer),
            inversePrimary = Color(accent.lightPrimary),
            surfaceTint = Color(accent.darkPrimary),
        )
    } else {
        copy(
            primary = Color(accent.lightPrimary),
            onPrimary = Color(accent.lightOnPrimary),
            primaryContainer = Color(accent.lightContainer),
            onPrimaryContainer = Color(accent.lightOnContainer),
            inversePrimary = Color(accent.darkPrimary),
            surfaceTint = Color(accent.lightPrimary),
        )
    }
