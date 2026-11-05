package com.smekhnyov.carts.ui.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.smekhnyov.carts.R
import com.smekhnyov.carts.barcode.BarcodeRenderer
import com.smekhnyov.carts.data.CardDraft
import com.smekhnyov.carts.data.CardFormat
import com.smekhnyov.carts.data.LoyaltyCard
import com.smekhnyov.carts.ui.cards.CardPaletteCount
import com.smekhnyov.carts.ui.cards.DefaultEmblem
import com.smekhnyov.carts.ui.cards.EmblemPresets
import com.smekhnyov.carts.ui.cards.paletteFor
import com.smekhnyov.carts.ui.cards.rememberEmblemImage
import com.smekhnyov.carts.ui.cards.rememberPickedImage
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.BarcodeView
import com.smekhnyov.carts.ui.components.SystemBars
import com.smekhnyov.carts.ui.components.WalletBackground
import com.smekhnyov.carts.ui.scanner.ScannerScreen
import com.smekhnyov.carts.ui.scanner.toCardFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    initialCard: LoyaltyCard?,
    onClose: () -> Unit,
    onSave: (CardDraft, onResult: (Boolean) -> Unit) -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val initialKey = initialCard?.id ?: -1L

    var name by rememberSaveable(initialKey) {
        mutableStateOf(initialCard?.name.orEmpty())
    }
    var code by rememberSaveable(initialKey) {
        mutableStateOf(initialCard?.code.orEmpty())
    }
    var formatName by rememberSaveable(initialKey) {
        mutableStateOf((initialCard?.format ?: CardFormat.QR_CODE).name)
    }
    var formatAuto by rememberSaveable(initialKey) {
        mutableStateOf(
            initialCard == null ||
                detectCodeFormat(initialCard.code) == initialCard.format,
        )
    }
    var codeFromScan by rememberSaveable(initialKey) {
        mutableStateOf(false)
    }
    var emoji by rememberSaveable(initialKey) {
        mutableStateOf(initialCard?.emblemEmoji ?: DefaultEmblem)
    }
    var paletteIndex by rememberSaveable(initialKey) {
        mutableIntStateOf(initialCard?.paletteIndex ?: 0)
    }
    var imagePath by rememberSaveable(initialKey) {
        mutableStateOf(initialCard?.emblemImagePath)
    }
    var pickedUri by rememberSaveable(initialKey) {
        mutableStateOf<String?>(null)
    }
    var removeImage by rememberSaveable(initialKey) {
        mutableStateOf(false)
    }
    var note by rememberSaveable(initialKey) {
        mutableStateOf(initialCard?.note.orEmpty())
    }

    var showEmblemSheet by rememberSaveable { mutableStateOf(false) }
    var scannerOpen by rememberSaveable { mutableStateOf(false) }
    var scanningPhoto by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val format = CardFormat.entries.firstOrNull { it.name == formatName } ?: CardFormat.QR_CODE
    val isEditing = initialCard != null

    val onCodeChange: (String) -> Unit = { value ->
        code = value
        // Ручное редактирование снимает блокировку формата, полученного сканером.
        if (codeFromScan) {
            codeFromScan = false
            formatAuto = true
        }
        if (formatAuto) {
            formatName = detectCodeFormat(value).name
        }
        errorMessage = null
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            pickedUri = uri.toString()
            imagePath = null
            removeImage = false
            showEmblemSheet = false
        }
    }

    fun launchPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    val codePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scanningPhoto = true
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (_: IOException) {
            scanningPhoto = false
            onMessage(context.getString(R.string.error_photo_scan))
            return@rememberLauncherForActivityResult
        }
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                val value = barcode?.rawValue
                if (barcode == null || value == null) {
                    onMessage(context.getString(R.string.error_photo_scan))
                } else {
                    val detectedFormat = if (barcode.format == Barcode.FORMAT_UNKNOWN) {
                        detectCodeFormat(value)
                    } else {
                        barcode.format.toCardFormat()
                    }
                    code = value
                    formatName = detectedFormat.name
                    codeFromScan = true
                    formatAuto = false
                    errorMessage = null
                    onMessage(
                        context.getString(
                            R.string.message_code_scanned,
                            context.getString(detectedFormat.labelRes),
                        ),
                    )
                }
            }
            .addOnFailureListener {
                onMessage(context.getString(R.string.error_photo_scan))
            }
            .addOnCompleteListener {
                scanningPhoto = false
                scanner.close()
            }
    }

    fun launchCodePhotoPicker() {
        codePhotoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    fun validateAndSave() {
        val trimmedName = name.trim()
        val trimmedCode = code.trim()
        when {
            trimmedName.isEmpty() -> {
                errorMessage = context.getString(R.string.error_store_name_required)
                return
            }

            trimmedCode.isEmpty() -> {
                errorMessage = context.getString(R.string.error_code_required)
                return
            }
        }
        saving = true
        errorMessage = null
        scope.launch {
            val valid = withContext(Dispatchers.Default) {
                BarcodeRenderer.isValid(trimmedCode, format)
            }
            if (!valid) {
                saving = false
                errorMessage = context.getString(
                    R.string.error_code_format,
                    context.getString(format.labelRes),
                )
                return@launch
            }
            val draft = CardDraft(
                id = initialCard?.id,
                name = trimmedName,
                code = trimmedCode,
                format = format,
                paletteIndex = paletteIndex,
                emblemEmoji = emoji,
                emblemImagePath = if (removeImage) null else imagePath,
                newEmblemUri = pickedUri?.let(Uri::parse),
                removeEmblemImage = removeImage,
                note = note.trim(),
            )
            onSave(draft) { success ->
                saving = false
                if (!success) {
                    errorMessage = context.getString(R.string.error_save_card)
                }
            }
        }
    }

    WalletBackground(Modifier.fillMaxSize()) {
        SystemBars(
            background = MaterialTheme.colorScheme.background,
            darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.55f,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                )
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 6.dp, bottom = 132.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isEditing) {
                            stringResource(R.string.add_title_edit)
                        } else {
                            stringResource(R.string.add_title_new)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = if (isEditing) {
                            stringResource(R.string.add_subtitle_edit)
                        } else {
                            stringResource(R.string.add_subtitle_new)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            DraftPreviewCard(
                name = name,
                code = code,
                format = format,
                paletteIndex = paletteIndex,
                emoji = emoji,
                imagePath = imagePath,
                pickedUri = pickedUri,
            )

            Spacer(Modifier.height(20.dp))

            FormSection(title = stringResource(R.string.add_section_store)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmblemSelector(
                        emoji = emoji,
                        imagePath = imagePath,
                        pickedUri = pickedUri,
                        onClick = { showEmblemSheet = true },
                    )
                    Spacer(Modifier.width(14.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.add_store_name_label)) },
                        placeholder = { Text(stringResource(R.string.add_store_name_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = appTextFieldColors(),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            FormSection(title = stringResource(R.string.add_section_code)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.add_code_label)) },
                    placeholder = { Text(stringResource(format.placeholderRes)) },
                    minLines = 1,
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    keyboardActions = KeyboardActions.Default,
                    trailingIcon = {
                        if (code.isNotEmpty()) {
                            IconButton(
                                onClick = { onCodeChange("") },
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                            }
                        }
                    },
                    colors = appTextFieldColors(),
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalButton(
                        onClick = { scannerOpen = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.Scan,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_scan_camera))
                    }
                    FilledTonalButton(
                        onClick = ::launchCodePhotoPicker,
                        enabled = !scanningPhoto,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        if (scanningPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = AppIcons.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_scan_photo))
                    }
                }

                AnimatedVisibility(visible = !codeFromScan) {
                    Column {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.add_code_format),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item(key = "auto") {
                                FilterChip(
                                    selected = formatAuto,
                                    onClick = {
                                        formatAuto = true
                                        formatName = detectCodeFormat(code).name
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.format_auto)) },
                                    leadingIcon = if (formatAuto) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                )
                            }
                            items(CardFormat.pickerOrder, key = { it.name }) { item ->
                                val selected = !formatAuto && format == item
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        formatAuto = false
                                        formatName = item.name
                                        errorMessage = null
                                    },
                                    label = { Text(item.shortLabel) },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (formatAuto) {
                                stringResource(R.string.add_format_auto_hint, format.shortLabel)
                            } else {
                                stringResource(R.string.add_format_manual_hint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            FormSection(title = stringResource(R.string.add_section_note)) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.add_note_label)) },
                    placeholder = { Text(stringResource(R.string.add_note_placeholder)) },
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = appTextFieldColors(),
                )
            }

            Spacer(Modifier.height(14.dp))

            FormSection(title = stringResource(R.string.add_section_appearance)) {
                Text(
                    text = stringResource(R.string.add_card_color),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CardPaletteCount) { index ->
                        val palette = paletteFor(index)
                        val selected = paletteIndex == index
                        val colorDescription = stringResource(
                            R.string.add_card_color_description,
                            index + 1,
                        )
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .padding(5.dp)
                                .clip(CircleShape)
                                .background(palette.container)
                                .clickable {
                                    paletteIndex = index
                                }
                                .semantics {
                                    role = Role.RadioButton
                                    contentDescription = colorDescription
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = palette.content,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.add_code_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 18.dp,
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = ::validateAndSave,
                    enabled = !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = if (isEditing) {
                                stringResource(R.string.add_save_changes)
                            } else {
                                stringResource(R.string.add_add_to_wallet)
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        if (showEmblemSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEmblemSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                EmblemSheet(
                    selectedEmoji = emoji,
                    hasImage = pickedUri != null || imagePath != null,
                    onEmojiSelected = {
                        emoji = it
                        pickedUri = null
                        imagePath = null
                        removeImage = true
                        showEmblemSheet = false
                    },
                    onPickPhoto = ::launchPhotoPicker,
                    onRemovePhoto = {
                        pickedUri = null
                        imagePath = null
                        removeImage = true
                        showEmblemSheet = false
                    },
                )
            }
        }

        if (scannerOpen) {
            ScannerScreen(
                onClose = { scannerOpen = false },
                onResult = { value, scannedFormat ->
                    code = value
                    formatName = scannedFormat.name
                    codeFromScan = true
                    formatAuto = false
                    scannerOpen = false
                    errorMessage = null
                    onMessage(
                        context.getString(
                            R.string.message_code_scanned,
                            context.getString(scannedFormat.labelRes),
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EmblemSelector(
    emoji: String,
    imagePath: String?,
    pickedUri: String?,
    onClick: () -> Unit,
) {
    val image = rememberPickedImage(pickedUri?.let(Uri::parse), targetSize = 192)
        ?: rememberEmblemImage(imagePath, targetSize = 192)
    val emblemDescription = stringResource(R.string.add_emblem_select)

    Box(
        modifier = Modifier.size(70.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = emblemDescription
                },
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
                Text(text = emoji, fontSize = 32.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLow, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun DraftPreviewCard(
    name: String,
    code: String,
    format: CardFormat,
    paletteIndex: Int,
    emoji: String,
    imagePath: String?,
    pickedUri: String?,
) {
    val palette = paletteFor(paletteIndex)
    val shape = RoundedCornerShape(30.dp)
    val image = rememberPickedImage(pickedUri?.let(Uri::parse), targetSize = 192)
        ?: rememberEmblemImage(imagePath, targetSize = 192)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp)
            .shadow(
                elevation = 2.dp,
                shape = shape,
            )
            .clip(shape)
            .background(palette.container)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.content.copy(alpha = 0.10f))
                        .border(
                            1.dp,
                            palette.content.copy(alpha = 0.12f),
                            CircleShape,
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
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { stringResource(R.string.add_preview_store_name) },
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.content.copy(alpha = if (name.isBlank()) 0.48f else 1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.add_preview_loyalty_card),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.content.copy(alpha = 0.62f),
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(palette.content.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (format.isTwoDimensional && format != CardFormat.PDF_417) {
                            AppIcons.QrCode
                        } else {
                            AppIcons.Barcode
                        },
                        contentDescription = null,
                        tint = palette.content,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = format.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.content,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
            ) {
                if (code.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.add_preview_empty_code),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A8490),
                        )
                    }
                } else {
                    BarcodeView(
                        content = code,
                        format = format,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmblemSheet(
    selectedEmoji: String,
    hasImage: Boolean,
    onEmojiSelected: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.add_emblem_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.add_emblem_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 5,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            EmblemPresets.forEach { item ->
                val selected = item == selectedEmoji && !hasImage
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                        )
                        .then(
                            if (selected) {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onEmojiSelected(item) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = item, fontSize = 26.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onPickPhoto,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Photo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_emblem_photo))
            }
            if (hasImage) {
                TextButton(
                    onClick = onRemovePhoto,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.add_emblem_remove))
                }
            }
        }
    }
}

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    cursorColor = MaterialTheme.colorScheme.primary,
)

/**
 * Подбирает формат кода по его содержимому: числовые длины выбирают EAN/UPC,
 * остальной текст — Code 128, а всё, что не кодируется линейно, — QR-код.
 */
private fun detectCodeFormat(raw: String): CardFormat {
    val code = raw.trim()
    if (code.isEmpty()) return CardFormat.QR_CODE

    val candidates = buildList {
        if (code.all(Char::isDigit)) {
            when (code.length) {
                8 -> add(CardFormat.EAN_8)
                12 -> add(CardFormat.UPC_A)
                13 -> add(CardFormat.EAN_13)
            }
        }
        add(CardFormat.CODE_128)
        add(CardFormat.CODE_39)
        add(CardFormat.QR_CODE)
    }
    return candidates.firstOrNull { BarcodeRenderer.isValid(code, it) } ?: CardFormat.QR_CODE
}
