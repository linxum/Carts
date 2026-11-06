package com.smekhnyov.carts.ui.wallet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    cards: List<LoyaltyCard>,
    isLoading: Boolean,
    onCardClick: (LoyaltyCard) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit,
    onMoveCard: (cardId: Long, targetIndex: Int) -> Unit,
) {
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
                else -> {
                    if (cards.size > 1) {
                        Text(
                            text = stringResource(R.string.wallet_reorder_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp),
                        )
                    }
                    WalletStack(
                        cards = cards,
                        onCardClick = onCardClick,
                        onMoveCard = onMoveCard,
                    )
                }
            }
        }
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
    onMoveCard: (cardId: Long, targetIndex: Int) -> Unit,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val peek = 84.dp
    val cardHeight = 200.dp
    val peekPx = with(density) { peek.toPx() }

    // Local copy lets the reordered stack stay in place until the database emits the new order.
    var localCards by remember { mutableStateOf(cards) }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var settling by remember { mutableStateOf(false) }

    LaunchedEffect(cards) {
        if (draggingId == null) {
            localCards = cards
        }
    }

    val startDrag: (LoyaltyCard) -> Unit = { card ->
        val index = localCards.indexOfFirst { it.id == card.id }
        if (index >= 0 && !settling) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            draggingId = card.id
            draggingIndex = index
            targetIndex = index
            dragOffset = 0f
        }
    }

    val dragCard: (Float) -> Unit = { delta ->
        if (draggingId != null && !settling && draggingIndex >= 0) {
            val minOffset = -draggingIndex * peekPx
            val maxOffset = (localCards.lastIndex - draggingIndex) * peekPx
            dragOffset = (dragOffset + delta).coerceIn(minOffset, maxOffset)
            targetIndex = (draggingIndex + (dragOffset / peekPx).roundToInt())
                .coerceIn(0, localCards.lastIndex)
        }
    }

    val endDrag: () -> Unit = {
        val cardId = draggingId
        if (cardId == null || settling || draggingIndex < 0) {
            // Nothing to settle.
        } else {
            val from = draggingIndex
            val to = targetIndex.coerceIn(0, localCards.lastIndex)
            settling = true
            scope.launch {
                animate(
                    initialValue = dragOffset,
                    targetValue = (to - from) * peekPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                ) { value, _ -> dragOffset = value }

                val reordered = localCards.toMutableList().apply {
                    val card = removeAt(from)
                    add(to, card)
                }
                localCards = reordered
                draggingId = null
                draggingIndex = -1
                targetIndex = -1
                dragOffset = 0f
                settling = false

                if (to != from) {
                    onMoveCard(cardId, to)
                }
            }
        }
    }

    val cancelDrag: () -> Unit = {
        if (!settling) {
            draggingId = null
            draggingIndex = -1
            targetIndex = -1
            dragOffset = 0f
        }
    }

    val stackHeight = cardHeight + peek * (localCards.size - 1).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(stackHeight),
    ) {
        localCards.forEachIndexed { index, card ->
            val isDragging = card.id == draggingId
            val slot = when {
                draggingId == null -> index
                isDragging -> index
                draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> index - 1
                draggingIndex > targetIndex && index in targetIndex until draggingIndex -> index + 1
                else -> index
            }
            val animatedSlot by animateIntAsState(
                targetValue = slot,
                animationSpec = if (isDragging) {
                    snap()
                } else {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                },
                label = "cardSlot$index",
            )
            val yPx = animatedSlot * peekPx + if (isDragging) dragOffset else 0f

            WalletCard(
                card = card,
                index = index,
                isDragging = isDragging,
                canMoveUp = index > 0,
                canMoveDown = index < localCards.lastIndex,
                onMoveUp = { onMoveCard(card.id, index - 1) },
                onMoveDown = { onMoveCard(card.id, index + 1) },
                onDragStart = { startDrag(card) },
                onDrag = dragCard,
                onDragEnd = endDrag,
                onDragCancel = cancelDrag,
                onClick = { onCardClick(card) },
                modifier = Modifier
                    .offset { IntOffset(0, yPx.roundToInt()) }
                    .zIndex(if (isDragging) 100f else index.toFloat()),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WalletCard(
    card: LoyaltyCard,
    index: Int,
    isDragging: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = paletteFor(card.paletteIndex, card.customColor)
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

    val moveUpLabel = stringResource(R.string.wallet_move_up)
    val moveDownLabel = stringResource(R.string.wallet_move_down)

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics {
                customActions = buildList {
                    if (canMoveUp) {
                        add(
                            CustomAccessibilityAction(moveUpLabel) {
                                onMoveUp()
                                true
                            },
                        )
                    }
                    if (canMoveDown) {
                        add(
                            CustomAccessibilityAction(moveDownLabel) {
                                onMoveDown()
                                true
                            },
                        )
                    }
                }
            }
            .graphicsLayer {
                val scale = pressScale * (0.96f + 0.04f * entry) * (if (isDragging) 1.03f else 1f)
                scaleX = scale
                scaleY = scale
                alpha = entry
                translationY = (1f - entry) * 24.dp.toPx()
            }
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(color = palette.content.copy(alpha = 0.12f)),
                role = Role.Button,
                onClick = onClick,
            )
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { currentOnDragStart() },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragCancel() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                )
            },
        shape = shape,
        color = palette.container,
        shadowElevation = if (isDragging) 14.dp else 1.dp,
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
