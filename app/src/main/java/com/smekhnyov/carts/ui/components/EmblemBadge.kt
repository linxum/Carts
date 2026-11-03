package com.smekhnyov.carts.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smekhnyov.carts.ui.cards.rememberEmblemImage

@Composable
fun EmblemBadge(
    emoji: String?,
    imagePath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    background: Color = Color.White.copy(alpha = 0.20f),
    contentColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.24f),
) {
    val image = rememberEmblemImage(imagePath, targetSize = (size.value * 3).toInt())
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background)
            .then(
                if (borderColor.alpha > 0f) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = emoji?.takeIf { it.isNotBlank() } ?: "🛍️",
                fontSize = (size.value * 0.48f).sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = (size.value * 0.48f).sp),
            )
        }
    }
}
