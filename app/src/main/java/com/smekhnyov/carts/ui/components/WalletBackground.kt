package com.smekhnyov.carts.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

/**
 * Мягкий «дышащий» фон с цветными пятнами — визуальная база Material 3 Expressive.
 */
@Composable
fun WalletBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    colorScheme.background,
                    colorScheme.surfaceContainerLow,
                    colorScheme.background,
                ),
            ),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val primary = colorScheme.primary
            val tertiary = colorScheme.tertiary
            val secondary = colorScheme.secondary

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width * 1.02f, size.height * 0.04f),
                    radius = size.width * 0.85f,
                ),
                radius = size.width * 0.85f,
                center = Offset(size.width * 1.02f, size.height * 0.04f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(-size.width * 0.15f, size.height * 0.62f),
                    radius = size.width * 0.9f,
                ),
                radius = size.width * 0.9f,
                center = Offset(-size.width * 0.15f, size.height * 0.62f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.95f),
                    radius = size.width * 0.7f,
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.9f, size.height * 0.95f),
            )
        }
        content()
    }
}
