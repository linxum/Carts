package com.smekhnyov.carts.ui.detail

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smekhnyov.carts.data.CardFormat
import com.smekhnyov.carts.data.LoyaltyCard
import com.smekhnyov.carts.ui.cards.paletteFor
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.BarcodeView
import com.smekhnyov.carts.ui.components.EmblemBadge
import com.smekhnyov.carts.ui.components.SystemBars
import com.smekhnyov.carts.ui.components.findActivity
import kotlinx.coroutines.launch

@Composable
fun CardDetailScreen(
    card: LoyaltyCard,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val palette = paletteFor(card.paletteIndex)
    val shape = RoundedCornerShape(30.dp)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var generation by remember(card.id) { mutableIntStateOf(0) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    KeepScreenBright()
    SystemBars(
        background = palette.start,
        darkIcons = palette.start.luminance() > 0.55f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.brush),
    ) {
        DetailDecorations(emoji = card.emblemEmoji)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = palette.onColor.copy(alpha = 0.16f),
                        contentColor = palette.onColor,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                    )
                }
                Spacer(Modifier.weight(1f))
                Box {
                    FilledTonalIconButton(
                        onClick = { menuExpanded = true },
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = palette.onColor.copy(alpha = 0.16f),
                            contentColor = palette.onColor,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Действия",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Изменить") },
                            leadingIcon = {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Копировать код") },
                            leadingIcon = {
                                Icon(AppIcons.Copy, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                clipboard.setText(AnnotatedString(card.code))
                                onMessage("Код скопирован")
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Удалить карту") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 28.dp)
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmblemBadge(
                        emoji = card.emblemEmoji,
                        imagePath = card.emblemImagePath,
                        size = 66.dp,
                        background = palette.onColor.copy(alpha = 0.16f),
                        contentColor = palette.onColor,
                        borderColor = palette.onColor.copy(alpha = 0.20f),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.onColor,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${card.format.label} · генерируется заново",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.onColor.copy(alpha = 0.68f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(26.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 18.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = card.format.label.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF6F6876),
                                letterSpacing = 1.4.sp,
                            )
                            Spacer(Modifier.weight(1f))
                            FilledTonalIconButton(
                                onClick = {
                                    generation += 1
                                    scope.launch {
                                        rotation.animateTo(
                                            targetValue = rotation.value + 360f,
                                            animationSpec = tween(450),
                                        )
                                    }
                                },
                                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFFF1ECF4),
                                    contentColor = Color(0xFF2B2631),
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .rotate(rotation.value),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Обновить код",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val barcodeModifier = if (card.format.isSquare) {
                            Modifier
                                .fillMaxWidth()
                                .height(248.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .height(if (card.format == CardFormat.PDF_417) 116.dp else 148.dp)
                        }
                        BarcodeView(
                            content = card.code,
                            format = card.format,
                            modifier = barcodeModifier.padding(horizontal = if (card.format.isSquare) 12.dp else 0.dp),
                            regenerationKey = generation,
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF4F0F7))
                                .padding(start = 16.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = card.code,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1720),
                                letterSpacing = 1.2.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 12.dp),
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(card.code))
                                    onMessage("Код скопирован")
                                },
                            ) {
                                Icon(
                                    imageVector = AppIcons.Copy,
                                    contentDescription = "Копировать код",
                                    tint = Color(0xFF615B68),
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Покажите код на кассе. Он каждый раз создаётся из данных карты.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7A7480),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.onColor.copy(alpha = 0.18f),
                            contentColor = palette.onColor,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Изменить")
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(0.7f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = palette.onColor.copy(alpha = 0.85f),
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Удалить")
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Данные хранятся только на этом устройстве",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.onColor.copy(alpha = 0.46f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Удалить карту?") },
            text = {
                Text(
                    "Карта «${card.name}» и её эмблема будут удалены с устройства. " +
                        "Это действие нельзя отменить.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }
}

@Composable
private fun BoxScope.DetailDecorations(emoji: String?) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = size.width * 0.82f,
            center = Offset(size.width * 1.03f, -size.height * 0.05f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = size.width * 0.55f,
            center = Offset(-size.width * 0.12f, size.height * 0.93f),
        )
    }
    Text(
        text = emoji ?: "🛍️",
        fontSize = 210.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 80.dp, end = 2.dp)
            .graphicsLayer {
                alpha = 0.08f
                rotationZ = -14f
            },
    )
}

@Composable
private fun KeepScreenBright() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val activity = view.context.findActivity() as? Activity ?: return
    DisposableEffect(activity) {
        val window = activity.window
        val attributes = window.attributes
        val previousBrightness = attributes.screenBrightness
        attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = attributes
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            val restored = window.attributes
            restored.screenBrightness = previousBrightness
            window.attributes = restored
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
