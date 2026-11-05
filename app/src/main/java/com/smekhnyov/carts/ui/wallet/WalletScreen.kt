package com.smekhnyov.carts.ui.wallet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.smekhnyov.carts.R
import com.smekhnyov.carts.data.LoyaltyCard
import com.smekhnyov.carts.ui.cards.paletteFor
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.EmblemBadge
import com.smekhnyov.carts.ui.components.SystemBars
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    cards: List<LoyaltyCard>,
    isLoading: Boolean,
    onCardClick: (LoyaltyCard) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit,
    onMoveCard: (cardId: Long, direction: Int) -> Unit,
) {
    var showReorderSheet by rememberSaveable { mutableStateOf(false) }

    SystemBars(
        background = MaterialTheme.colorScheme.background,
        darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.55f,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wallet_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                actions = {
                    if (cards.size > 1) {
                        IconButton(onClick = { showReorderSheet = true }) {
                            Icon(
                                imageVector = AppIcons.Sort,
                                contentDescription = stringResource(R.string.wallet_reorder),
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCard,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                },
                text = { Text(stringResource(R.string.wallet_add_card)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 104.dp),
        ) {
            when {
                isLoading -> LoadingWallet()
                cards.isEmpty() -> EmptyWallet()
                else -> WalletStack(
                    cards = cards,
                    onCardClick = onCardClick,
                    onLongPress = { showReorderSheet = true },
                )
            }
        }
    }

    if (showReorderSheet && cards.isNotEmpty()) {
        ReorderSheet(
            cards = cards,
            onMoveCard = onMoveCard,
            onDismiss = { showReorderSheet = false },
        )
    }
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
private fun EmptyWallet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.wallet_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.wallet_empty_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun WalletStack(
    cards: List<LoyaltyCard>,
    onCardClick: (LoyaltyCard) -> Unit,
    onLongPress: () -> Unit,
) {
    val density = LocalDensity.current
    val peek = 84.dp
    val cardHeight = 200.dp

    Layout(
        content = {
            cards.forEachIndexed { index, card ->
                WalletCard(
                    card = card,
                    index = index,
                    onClick = { onCardClick(card) },
                    onLongClick = onLongPress,
                    modifier = Modifier.zIndex(index + 1f),
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
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(0, index * peekPx)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WalletCard(
    card: LoyaltyCard,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = paletteFor(card.paletteIndex)
    val shape = RoundedCornerShape(28.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer {
                val scale = pressScale * (0.96f + 0.04f * entry)
                scaleX = scale
                scaleY = scale
                alpha = entry
                translationY = (1f - entry) * 24.dp.toPx()
            }
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(color = palette.content.copy(alpha = 0.12f)),
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.wallet_reorder),
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        shape = shape,
        color = palette.container,
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmblemBadge(
                        emoji = card.emblemEmoji,
                        imagePath = card.emblemImagePath,
                        size = 46.dp,
                        background = palette.content.copy(alpha = 0.10f),
                        contentColor = palette.content,
                        borderColor = palette.content.copy(alpha = 0.12f),
                    )
                    Spacer(Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(card.format.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.content.copy(alpha = 0.68f),
                            maxLines = 1,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = palette.content.copy(alpha = 0.52f),
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.Bottom) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.wallet_code_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.content.copy(alpha = 0.56f),
                            letterSpacing = 1.6.sp,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = card.shortCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = palette.content,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(palette.content.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (card.format.isTwoDimensional) {
                                AppIcons.QrCode
                            } else {
                                AppIcons.Barcode
                            },
                            contentDescription = null,
                            tint = palette.content,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReorderSheet(
    cards: List<LoyaltyCard>,
    onMoveCard: (cardId: Long, direction: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.wallet_reorder_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.wallet_reorder_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmblemBadge(
                            emoji = card.emblemEmoji,
                            imagePath = card.emblemImagePath,
                            size = 42.dp,
                            background = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(card.format.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { onMoveCard(card.id, -1) },
                            enabled = index > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.wallet_move_up),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { onMoveCard(card.id, 1) },
                            enabled = index < cards.lastIndex,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.wallet_move_down),
                            )
                        }
                    }
                    if (index != cards.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}
