package com.smekhnyov.carts.ui.wallet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.smekhnyov.carts.data.CardFormat
import com.smekhnyov.carts.data.LoyaltyCard
import com.smekhnyov.carts.ui.cards.EmblemPresets
import com.smekhnyov.carts.ui.cards.paletteFor
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.EmblemBadge
import com.smekhnyov.carts.ui.components.SystemBars
import com.smekhnyov.carts.ui.components.WalletBackground
import kotlinx.coroutines.delay

@Composable
fun WalletScreen(
    cards: List<LoyaltyCard>,
    isLoading: Boolean,
    onCardClick: (LoyaltyCard) -> Unit,
    onAddCard: () -> Unit,
) {
    WalletBackground(Modifier.fillMaxSize()) {
        SystemBars(
            background = MaterialTheme.colorScheme.background,
            darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.55f,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                )
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 132.dp),
        ) {
            WalletHeader(cardCount = cards.size)

            Spacer(Modifier.height(28.dp))

            when {
                isLoading -> LoadingWallet()
                cards.isEmpty() -> EmptyWallet(onAddCard = onAddCard)
                else -> {
                    WalletStack(cards = cards, onCardClick = onCardClick)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Нажмите на карту, чтобы открыть код",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddCard,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            },
            text = { Text("Добавить карту") },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(bottom = 22.dp),
        )
    }
}

@Composable
private fun WalletHeader(cardCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "КОШЕЛЁК",
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 2.2.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.weight(1f))
        if (cardCount > 0) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = cardCountText(cardCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(22.dp))

    Text(
        text = "Мои карты",
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Бонусные карты и коды — всегда под рукой",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LoadingWallet() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyWallet(onAddCard: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EmptyCardsArtwork()
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Пока ни одной карты",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Добавьте первую бонусную карту — и её код будет всегда под рукой.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(22.dp))
        FilledTonalButton(onClick = onAddCard) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Добавить карту")
        }
    }
}

@Composable
private fun EmptyCardsArtwork() {
    val palettes = listOf(paletteFor(0), paletteFor(4), paletteFor(3))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center,
    ) {
        listOf(-11f, 0f, 11f).forEachIndexed { index, rotation ->
            val palette = palettes[index]
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .height(142.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        translationY = if (index == 1) 0f else 10.dp.toPx()
                        translationX = if (index == 0) (-28).dp.toPx() else if (index == 2) 28.dp.toPx() else 0f
                    }
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = palette.glow,
                        spotColor = palette.glow,
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(palette.brush),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = EmblemPresets[index * 3 + 1],
                    fontSize = 54.sp,
                    modifier = Modifier
                        .padding(18.dp)
                        .graphicsLayer { alpha = 0.82f },
                )
            }
        }
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(18.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun WalletStack(
    cards: List<LoyaltyCard>,
    onCardClick: (LoyaltyCard) -> Unit,
) {
    val density = LocalDensity.current
    val peek = 78.dp
    val cardHeight = 200.dp

    Layout(
        content = {
            cards.forEachIndexed { index, card ->
                WalletCard(
                    card = card,
                    index = index,
                    onClick = { onCardClick(card) },
                    modifier = Modifier
                        .zIndex((cards.size - index).toFloat())
                        .padding(horizontal = (index * 3).dp),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val placeables = measurables.map { measurable ->
            measurable.measure(Constraints.fixedWidth(width))
        }
        val peekPx = with(density) { peek.roundToPx() }
        val cardHeightPx = with(density) { cardHeight.roundToPx() }
        val height = cardHeightPx + peekPx * (placeables.size - 1).coerceAtLeast(0)

        layout(width, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += peekPx
            }
        }
    }
}

@Composable
private fun WalletCard(
    card: LoyaltyCard,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = paletteFor(card.paletteIndex)
    val shape = RoundedCornerShape(30.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cardPress",
    )

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 55L)
        entered = true
    }
    val entry by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 320f,
        ),
        label = "cardEntry",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer {
                val scale = pressScale * (0.94f + 0.06f * entry)
                scaleX = scale
                scaleY = scale
                alpha = entry
                translationY = (1f - entry) * 42.dp.toPx()
            }
            .shadow(
                elevation = 18.dp,
                shape = shape,
                clip = false,
                ambientColor = palette.glow.copy(alpha = 0.55f),
                spotColor = palette.glow.copy(alpha = 0.55f),
            )
            .clip(shape)
            .background(palette.brush)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = palette.onColor.copy(alpha = 0.20f)),
                role = Role.Button,
                onClickLabel = "Открыть карту ${card.name}",
                onClick = onClick,
            )
            .semantics {
                contentDescription = "${card.name}, ${card.format.label}"
            },
    ) {
        CardDecorations(card = card, paletteOnColor = palette.onColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmblemBadge(
                    emoji = card.emblemEmoji,
                    imagePath = card.emblemImagePath,
                    size = 50.dp,
                    background = palette.onColor.copy(alpha = 0.16f),
                    contentColor = palette.onColor,
                    borderColor = palette.onColor.copy(alpha = 0.18f),
                )
                Spacer(Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.onColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Бонусная карта",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onColor.copy(alpha = 0.62f),
                    )
                }
                Spacer(Modifier.width(8.dp))
                FormatPill(format = card.format, onColor = palette.onColor)
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "КОД",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onColor.copy(alpha = 0.56f),
                        letterSpacing = 1.6.sp,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = card.shortCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = palette.onColor,
                        letterSpacing = 1.4.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.onColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = palette.onColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CardDecorations(card: LoyaltyCard, paletteOnColor: Color) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = size.width * 0.62f,
            center = Offset(size.width * 1.02f, -size.height * 0.20f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.055f),
            radius = size.width * 0.36f,
            center = Offset(size.width * 0.02f, size.height * 1.06f),
        )
    }
    Text(
        text = card.emblemEmoji ?: "🛍️",
        fontSize = 132.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 56.dp, bottom = 18.dp)
            .graphicsLayer {
                alpha = 0.12f
                rotationZ = -12f
            },
    )
}

@Composable
private fun FormatPill(format: CardFormat, onColor: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(onColor.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = when {
                format == CardFormat.PDF_417 -> AppIcons.DataMatrix
                format.isTwoDimensional -> AppIcons.QrCode
                else -> AppIcons.Barcode
            },
            contentDescription = null,
            tint = onColor,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = format.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = onColor,
            maxLines = 1,
        )
    }
}

private fun cardCountText(count: Int): String {
    val lastTwo = count % 100
    val last = count % 10
    val word = when {
        lastTwo in 11..14 -> "карт"
        last == 1 -> "карта"
        last in 2..4 -> "карты"
        else -> "карт"
    }
    return "$count $word"
}
