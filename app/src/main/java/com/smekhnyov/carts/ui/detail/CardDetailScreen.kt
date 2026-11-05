package com.smekhnyov.carts.ui.detail

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smekhnyov.carts.R
import com.smekhnyov.carts.data.CardFormat
import com.smekhnyov.carts.data.LoyaltyCard
import com.smekhnyov.carts.ui.cards.paletteFor
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.BarcodeView
import com.smekhnyov.carts.ui.components.EmblemBadge
import com.smekhnyov.carts.ui.components.SystemBars
import com.smekhnyov.carts.ui.components.findActivity

@Composable
fun CardDetailScreen(
    card: LoyaltyCard,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val palette = paletteFor(card.paletteIndex, card.customColor)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    KeepScreenBright()
    SystemBars(
        background = palette.container,
        darkIcons = palette.container.luminance() > 0.55f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.container),
    ) {
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
                        containerColor = palette.content.copy(alpha = 0.12f),
                        contentColor = palette.content,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box {
                    FilledTonalIconButton(
                        onClick = { menuExpanded = true },
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = palette.content.copy(alpha = 0.12f),
                            contentColor = palette.content,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.detail_copy_code)) },
                            leadingIcon = {
                                Icon(AppIcons.Copy, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                clipboard.setText(AnnotatedString(card.code))
                                onMessage(context.getString(R.string.message_code_copied))
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.detail_delete_card)) },
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
                        background = palette.content.copy(alpha = 0.12f),
                        contentColor = palette.content,
                        borderColor = palette.content.copy(alpha = 0.16f),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.content,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
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
                                text = stringResource(card.format.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.content.copy(alpha = 0.68f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(26.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
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
                                text = stringResource(card.format.labelRes).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.4.sp,
                            )
                            Spacer(Modifier.weight(1f))
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
                            modifier = barcodeModifier.padding(
                                horizontal = if (card.format.isSquare) 12.dp else 0.dp,
                            ),
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(start = 16.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = card.code,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 12.dp),
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(card.code))
                                    onMessage(context.getString(R.string.message_code_copied))
                                },
                            ) {
                                Icon(
                                    imageVector = AppIcons.Copy,
                                    contentDescription = stringResource(R.string.detail_copy_code),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.detail_code_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                val note = card.note?.takeIf { it.isNotBlank() }
                if (note != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.detail_note_label).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.4.sp,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.content.copy(alpha = 0.12f),
                            contentColor = palette.content,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_edit))
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(0.7f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = palette.content.copy(alpha = 0.85f),
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_delete))
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.detail_storage_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.content.copy(alpha = 0.46f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = {
                Text(
                    stringResource(R.string.detail_delete_text, card.name),
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
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
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
