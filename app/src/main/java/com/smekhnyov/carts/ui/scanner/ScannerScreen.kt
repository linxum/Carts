package com.smekhnyov.carts.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.smekhnyov.carts.data.CardFormat
import com.smekhnyov.carts.ui.components.AppIcons
import com.smekhnyov.carts.ui.components.SystemBars
import com.smekhnyov.carts.ui.components.findActivity
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Composable
fun ScannerScreen(
    onClose: () -> Unit,
    onResult: (rawValue: String, format: CardFormat) -> Unit,
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionAsked by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        permissionAsked = true
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(onBack = onClose)
    SystemBars(background = Color.Black, darkIcons = false)

    if (permissionGranted) {
        CameraScanner(onClose = onClose, onResult = onResult)
    } else {
        CameraPermission(
            wasAsked = permissionAsked,
            onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            },
            onClose = onClose,
        )
    }
}

@Composable
private fun CameraScanner(
    onClose: () -> Unit,
    onResult: (rawValue: String, format: CardFormat) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnClose by rememberUpdatedState(onClose)

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val analyzer = remember {
        BarcodeAnalyzer { rawValue, mlkitFormat ->
            currentOnResult(rawValue, mlkitFormat.toCardFormat())
        }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasFlash by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, previewView) {
        var provider: ProcessCameraProvider? = null
        var disposed = false
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                if (disposed) return@addListener
                runCatching {
                    val cameraProvider = future.get()
                    provider = cameraProvider
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(cameraExecutor, analyzer)

                    cameraProvider.unbindAll()
                    val boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                    camera = boundCamera
                    hasFlash = boundCamera.cameraInfo.hasFlashUnit()
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            disposed = true
            provider?.unbindAll()
            analyzer.close()
            cameraExecutor.shutdown()
            camera = null
        }
    }

    LaunchedEffect(torchEnabled, camera) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        ScanOverlay(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScannerIconButton(
                onClick = onClose,
                contentDescription = "Закрыть сканер",
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }

            Spacer(Modifier.weight(1f))

            Surface(
                color = Color.Black.copy(alpha = 0.42f),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "Сканирование кода",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            ScannerIconButton(
                onClick = {
                    if (hasFlash) torchEnabled = !torchEnabled
                },
                contentDescription = "Вспышка",
                active = torchEnabled,
            ) {
                Icon(AppIcons.Flashlight, contentDescription = null)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Поместите код в рамку",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "QR, Data Matrix, Code 128, EAN и другие штрихкоды",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.68f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScannerIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    active: Boolean = false,
    content: @Composable () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (active) Color.White else Color.Black.copy(alpha = 0.42f),
            contentColor = if (active) Color.Black else Color.White,
        ),
        modifier = Modifier.size(48.dp),
    ) {
        Surface(color = Color.Transparent) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}

@Composable
private fun ScanOverlay(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLineProgress",
    )

    Canvas(modifier = modifier) {
        val windowSize = min(size.width * 0.72f, with(density) { 310.dp.toPx() })
        val windowLeft = (size.width - windowSize) / 2f
        val windowTop = (size.height - windowSize) / 2f - with(density) { 34.dp.toPx() }
        val corner = windowLeft + windowSize
        val bottom = windowTop + windowSize
        val scrim = Color.Black.copy(alpha = 0.58f)

        drawRect(scrim, topLeft = Offset.Zero, size = Size(size.width, windowTop))
        drawRect(
            scrim,
            topLeft = Offset(0f, bottom),
            size = Size(size.width, (size.height - bottom).coerceAtLeast(0f)),
        )
        drawRect(
            scrim,
            topLeft = Offset(0f, windowTop),
            size = Size(windowLeft, windowSize),
        )
        drawRect(
            scrim,
            topLeft = Offset(corner, windowTop),
            size = Size((size.width - corner).coerceAtLeast(0f), windowSize),
        )

        val radius = with(density) { 30.dp.toPx() }
        val bracket = with(density) { 38.dp.toPx() }
        val stroke = with(density) { 4.dp.toPx() }
        val bracketColor = Color.White
        val path = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(windowLeft, windowTop, corner, bottom),
                    CornerRadius(radius),
                ),
            )
        }

        // Уголки рамки.
        drawLine(bracketColor, Offset(windowLeft, windowTop + radius), Offset(windowLeft, windowTop + bracket), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(windowLeft + radius, windowTop), Offset(windowLeft + bracket, windowTop), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(corner - bracket, windowTop), Offset(corner - radius, windowTop), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(corner, windowTop + radius), Offset(corner, windowTop + bracket), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(windowLeft, bottom - radius), Offset(windowLeft, bottom - bracket), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(windowLeft + radius, bottom), Offset(windowLeft + bracket, bottom), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(corner - bracket, bottom), Offset(corner - radius, bottom), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(corner, bottom - radius), Offset(corner, bottom - bracket), stroke, StrokeCap.Round)

        // Тонкая рамка и сканирующая линия.
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.28f),
            style = Stroke(width = with(density) { 1.dp.toPx() }),
        )
        val lineY = windowTop + with(density) { 28.dp.toPx() } +
            (windowSize - with(density) { 56.dp.toPx() }) * lineProgress
        drawLine(
            color = Color(0xFFCFBCFF).copy(alpha = 0.28f),
            start = Offset(windowLeft + radius, lineY),
            end = Offset(corner - radius, lineY),
            strokeWidth = with(density) { 10.dp.toPx() },
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFFEADDFF),
            start = Offset(windowLeft + radius, lineY),
            end = Offset(corner - radius, lineY),
            strokeWidth = with(density) { 2.dp.toPx() },
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CameraPermission(
    wasAsked: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Scan,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Нужен доступ к камере",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (wasAsked) {
                    "Без камеры не получится распознать код. Включите доступ в настройках приложения."
                } else {
                    "Камера используется только для распознавания кода карты. Изображение не сохраняется и не отправляется в сеть."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
            ) {
                Text("Разрешить камеру")
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onOpenSettings) {
                Text("Открыть настройки", color = Color.White.copy(alpha = 0.78f))
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onClose) {
                Text("Отмена", color = Color.White.copy(alpha = 0.55f))
            }
        }
    }
}

private class BarcodeAnalyzer(
    private val onBarcode: (rawValue: String, format: Int) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient()
    private val delivered = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (delivered.get()) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                val value = barcode?.rawValue
                if (value != null && delivered.compareAndSet(false, true)) {
                    onBarcode(value, barcode.format)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        scanner.close()
    }
}

private fun Int.toCardFormat(): CardFormat = when (this) {
    Barcode.FORMAT_QR_CODE -> CardFormat.QR_CODE
    Barcode.FORMAT_DATA_MATRIX -> CardFormat.DATA_MATRIX
    Barcode.FORMAT_AZTEC -> CardFormat.AZTEC
    Barcode.FORMAT_PDF417 -> CardFormat.PDF_417
    Barcode.FORMAT_CODE_128 -> CardFormat.CODE_128
    Barcode.FORMAT_CODE_39 -> CardFormat.CODE_39
    Barcode.FORMAT_CODE_93 -> CardFormat.CODE_93
    Barcode.FORMAT_EAN_13 -> CardFormat.EAN_13
    Barcode.FORMAT_EAN_8 -> CardFormat.EAN_8
    Barcode.FORMAT_UPC_A -> CardFormat.UPC_A
    Barcode.FORMAT_UPC_E -> CardFormat.UPC_E
    Barcode.FORMAT_ITF -> CardFormat.ITF
    Barcode.FORMAT_CODABAR -> CardFormat.CODABAR
    else -> CardFormat.QR_CODE
}
