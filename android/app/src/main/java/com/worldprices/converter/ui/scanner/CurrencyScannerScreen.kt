package com.worldprices.converter.ui.scanner

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.worldprices.converter.data.model.Currency
import com.worldprices.converter.data.model.CurrencyRegistry
import com.worldprices.converter.ui.components.CurrencyFlag
import com.worldprices.converter.ui.dialogs.CurrencyPickerSheet
import com.worldprices.converter.ui.theme.AccentGold
import com.worldprices.converter.ui.utils.AppStrings
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun CurrencyScannerScreen(
    sourceCurrency: Currency,
    targetCurrencies: List<Currency>,
    effectiveRates: Map<String, Double>,
    marketRates: Map<String, Double>,
    customRates: Map<String, Double>,
    isRussian: Boolean,
    onDismiss: () -> Unit,
    onApply: (amount: Double, detectedCurrency: String?) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return@remember ctx
            ctx = ctx.baseContext
        }
        ctx as? ComponentActivity
    }

    val registryOwner = LocalActivityResultRegistryOwner.current ?: activity

    if (registryOwner != null) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalActivityResultRegistryOwner provides registryOwner
        ) {
            CurrencyScannerScreenContent(
                sourceCurrency = sourceCurrency,
                targetCurrencies = targetCurrencies,
                effectiveRates = effectiveRates,
                marketRates = marketRates,
                customRates = customRates,
                isRussian = isRussian,
                onDismiss = onDismiss,
                onApply = onApply
            )
        }
    } else {
        CurrencyScannerScreenContent(
            sourceCurrency = sourceCurrency,
            targetCurrencies = targetCurrencies,
            effectiveRates = effectiveRates,
            marketRates = marketRates,
            customRates = customRates,
            isRussian = isRussian,
            onDismiss = onDismiss,
            onApply = onApply
        )
    }
}

@Composable
private fun CurrencyScannerScreenContent(
    sourceCurrency: Currency,
    targetCurrencies: List<Currency>,
    effectiveRates: Map<String, Double>,
    marketRates: Map<String, Double>,
    customRates: Map<String, Double>,
    isRussian: Boolean,
    onDismiss: () -> Unit,
    onApply: (amount: Double, detectedCurrency: String?) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission Prompt UI
            CameraPermissionPrompt(
                isRussian = isRussian,
                onGrant = {
                    try {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onClose = onDismiss
            )
        } else {
            // Live Camera Scanner UI
            LiveScannerContent(
                initialSource = sourceCurrency,
                targetCurrencies = targetCurrencies,
                effectiveRates = effectiveRates,
                marketRates = marketRates,
                customRates = customRates,
                isRussian = isRussian,
                onDismiss = onDismiss,
                onApply = onApply
            )
        }
    }
}

@Composable
private fun LiveScannerContent(
    initialSource: Currency,
    targetCurrencies: List<Currency>,
    effectiveRates: Map<String, Double>,
    marketRates: Map<String, Double>,
    customRates: Map<String, Double>,
    isRussian: Boolean,
    onDismiss: () -> Unit,
    onApply: (amount: Double, detectedCurrency: String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedSourceCurrency by remember { mutableStateOf(initialSource) }
    var isPickingSourceCurrency by remember { mutableStateOf(false) }

    var detectedAmount by remember { mutableFloatStateOf(0f) }
    var detectedCurrencyCode by remember { mutableStateOf<String?>(null) }
    var isFrozen by remember { mutableStateOf(false) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isFullListView by remember { mutableStateOf(false) }

    // Stabilization state to prevent erratic number flickering
    var candidatePrice by remember { mutableFloatStateOf(0f) }
    var candidateStreak by remember { mutableIntStateOf(0) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val textRecognizer = remember {
        try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            try {
                textRecognizer?.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    val symbols = remember {
        DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
    }

    fun calculateTargetAmount(target: Currency): Double {
        val amount = if (detectedAmount > 0f) detectedAmount.toDouble() else 0.0
        val baseCode = selectedSourceCurrency.code.lowercase()
        val targetCode = target.code.lowercase()

        if (baseCode == targetCode) return amount

        val customKey = "${baseCode}_$targetCode"
        val custom = customRates[customKey]
        if (custom != null && custom > 0.0) {
            return amount * custom
        }

        val baseInSource = marketRates[baseCode] ?: 1.0
        val targetInSource = marketRates[targetCode] ?: (effectiveRates[targetCode] ?: 0.0)

        val rate = if (baseCode == initialSource.code.lowercase()) {
            effectiveRates[targetCode] ?: 0.0
        } else if (baseInSource > 0.0) {
            targetInSource / baseInSource
        } else {
            0.0
        }

        return amount * rate
    }

    fun getCrossRate(target: Currency): Double {
        val baseCode = selectedSourceCurrency.code.lowercase()
        val targetCode = target.code.lowercase()
        if (baseCode == targetCode) return 1.0

        val customKey = "${baseCode}_$targetCode"
        val custom = customRates[customKey]
        if (custom != null && custom > 0.0) return custom

        val baseInSource = marketRates[baseCode] ?: 1.0
        val targetInSource = marketRates[targetCode] ?: (effectiveRates[targetCode] ?: 0.0)

        return if (baseCode == initialSource.code.lowercase()) {
            effectiveRates[targetCode] ?: 0.0
        } else if (baseInSource > 0.0) {
            targetInSource / baseInSource
        } else {
            0.0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                try {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx.applicationContext)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                try {
                                    if (isFrozen || textRecognizer == null) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                        val inputImage = InputImage.fromMediaImage(
                                            mediaImage,
                                            rotationDegrees
                                        )
                                        val frameW = if (rotationDegrees == 90 || rotationDegrees == 270) {
                                            mediaImage.height
                                        } else {
                                            mediaImage.width
                                        }
                                        val frameH = if (rotationDegrees == 90 || rotationDegrees == 270) {
                                            mediaImage.width
                                        } else {
                                            mediaImage.height
                                        }

                                        textRecognizer.process(inputImage)
                                            .addOnSuccessListener { visionText ->
                                                if (isFrozen) return@addOnSuccessListener
                                                try {
                                                    // 1. Collect lines inside the central viewfinder box with their bounding boxes
                                                    val ocrLines = ArrayList<OcrLine>(16)
                                                    for (block in visionText.textBlocks) {
                                                        for (line in block.lines) {
                                                            val box = line.boundingBox ?: continue
                                                            if (PriceTextParser.isInsideViewfinder(box, frameW, frameH)) {
                                                                ocrLines.add(
                                                                    OcrLine(
                                                                        text = line.text,
                                                                        left = box.left,
                                                                        top = box.top,
                                                                        right = box.right,
                                                                        bottom = box.bottom
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }

                                                    val parsed = PriceTextParser.parseSpatial(
                                                        ocrLines,
                                                        selectedSourceCurrency.code
                                                    )

                                                    if (parsed != null && parsed.amount > 0.0) {
                                                        val newAmount = parsed.amount.toFloat()

                                                        // High-confidence or visually dominant detection locks immediately
                                                        if (parsed.confidence >= 5.0f) {
                                                            detectedAmount = newAmount
                                                            candidatePrice = newAmount
                                                            candidateStreak = 0
                                                        } else {
                                                            // Multi-frame consensus for weaker detections
                                                            val isSame = candidatePrice > 0f &&
                                                                kotlin.math.abs(newAmount - candidatePrice) / candidatePrice < 0.03f

                                                            if (isSame) {
                                                                candidateStreak++
                                                                if (candidateStreak >= 2) {
                                                                    detectedAmount = candidatePrice
                                                                }
                                                            } else {
                                                                candidatePrice = newAmount
                                                                candidateStreak = 1
                                                            }
                                                        }
                                                    }
                                                } catch (e: Throwable) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            .addOnFailureListener {
                                                // ignore frame failure
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    imageProxy.close()
                                }
                            }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay
        ViewfinderOverlay()

        // Viewfinder Center Target Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isFullListView) 300.dp else 220.dp),
            contentAlignment = Alignment.Center
        ) {
            ViewfinderLaserBox(isDetected = detectedAmount > 0f)
        }

        // Top App Bar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { isPickingSourceCurrency = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurrencyFlag(currency = selectedSourceCurrency, size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedSourceCurrency.code.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = {
                    val next = !isTorchEnabled
                    isTorchEnabled = next
                    try {
                        cameraControl?.enableTorch(next)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTorchEnabled) AccentGold.copy(alpha = 0.9f)
                        else Color.Black.copy(alpha = 0.5f)
                    )
            ) {
                Icon(
                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = AppStrings.scanTorch(isRussian),
                    tint = if (isTorchEnabled) Color.Black else Color.White
                )
            }
        }

        // Bottom Controls and Currencies Drawer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            DetectedPriceBanner(
                detectedAmount = detectedAmount,
                sourceCurrency = selectedSourceCurrency,
                isFrozen = isFrozen,
                isRussian = isRussian,
                onToggleFreeze = { isFrozen = !isFrozen },
                onApply = {
                    if (detectedAmount > 0f) {
                        onApply(detectedAmount.toDouble(), selectedSourceCurrency.code)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CurrenciesLivePanel(
                targetCurrencies = targetCurrencies,
                isFullList = isFullListView,
                isRussian = isRussian,
                onToggleListView = { isFullListView = !isFullListView },
                getConvertedAmount = { calculateTargetAmount(it) },
                getRate = { getCrossRate(it) },
                symbols = symbols,
                selectedSourceCurrency = selectedSourceCurrency
            )
        }
    }

    if (isPickingSourceCurrency) {
        CurrencyPickerSheet(
            title = AppStrings.sourceCurrency(isRussian),
            isRussian = isRussian,
            selectedCodes = setOf(selectedSourceCurrency.code.lowercase()),
            multiSelect = false,
            onDismiss = { isPickingSourceCurrency = false },
            onSelectCurrency = { selected ->
                selectedSourceCurrency = selected
            }
        )
    }
}

@Composable
private fun ViewfinderOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
    )
}

@Composable
private fun ViewfinderLaserBox(isDetected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_beam"
    )

    val borderColor = if (isDetected) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(top = (laserProgress * 144).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (isDetected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            Color.Cyan,
                            if (isDetected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun DetectedPriceBanner(
    detectedAmount: Float,
    sourceCurrency: Currency,
    isFrozen: Boolean,
    isRussian: Boolean,
    onToggleFreeze: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2124).copy(alpha = 0.92f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isFrozen) AccentGold else Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (detectedAmount > 0f) {
                            val formatted = if (detectedAmount % 1f == 0f) {
                                detectedAmount.toInt().toString()
                            } else {
                                "%.2f".format(Locale.US, detectedAmount)
                            }
                            "$formatted ${sourceCurrency.code.uppercase()}"
                        } else {
                            AppStrings.scanWaiting(isRussian)
                        },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isFrozen) AppStrings.scanFreeze(isRussian) else AppStrings.scanDetected(isRussian),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFreeze,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFrozen) AccentGold.copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.1f)
                        )
                ) {
                    Icon(
                        imageVector = if (isFrozen) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isFrozen) AppStrings.scanResume(isRussian) else AppStrings.scanFreeze(isRussian),
                        tint = if (isFrozen) AccentGold else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApply,
                    enabled = detectedAmount > 0f,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.scanApply(isRussian),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrenciesLivePanel(
    targetCurrencies: List<Currency>,
    isFullList: Boolean,
    isRussian: Boolean,
    onToggleListView: () -> Unit,
    getConvertedAmount: (Currency) -> Double,
    getRate: (Currency) -> Double,
    symbols: DecimalFormatSymbols,
    selectedSourceCurrency: Currency
) {
    Card(
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF14171A).copy(alpha = 0.96f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppStrings.scanTargetListTitle(isRussian),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${targetCurrencies.size}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onToggleListView)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFullList) Icons.Default.ViewCarousel else Icons.Default.ViewAgenda,
                        contentDescription = "Toggle View",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFullList) "Карусель" else "Списком",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (targetCurrencies.isEmpty()) {
                Text(
                    text = AppStrings.scanNoTargets(isRussian),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            } else if (isFullList) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(targetCurrencies, key = { it.code }) { target ->
                        ScannerCurrencyCard(
                            target = target,
                            convertedAmount = getConvertedAmount(target),
                            rate = getRate(target),
                            symbols = symbols,
                            sourceCurrency = selectedSourceCurrency,
                            isFullWidth = true
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(targetCurrencies, key = { it.code }) { target ->
                        ScannerCurrencyCard(
                            target = target,
                            convertedAmount = getConvertedAmount(target),
                            rate = getRate(target),
                            symbols = symbols,
                            sourceCurrency = selectedSourceCurrency,
                            isFullWidth = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerCurrencyCard(
    target: Currency,
    convertedAmount: Double,
    rate: Double,
    symbols: DecimalFormatSymbols,
    sourceCurrency: Currency,
    isFullWidth: Boolean
) {
    val formatter = if (convertedAmount >= 100.0) {
        DecimalFormat("#,##0.00", symbols)
    } else if (convertedAmount >= 1.0) {
        DecimalFormat("#,##0.0000", symbols)
    } else {
        DecimalFormat("#,##0.000000", symbols)
    }

    val rateFormatter = if (rate >= 1.0) {
        DecimalFormat("#,##0.00", symbols)
    } else {
        DecimalFormat("#,##0.0000", symbols)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF22262B)
        ),
        modifier = if (isFullWidth) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.width(160.dp)
        }.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CurrencyFlag(currency = target, size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = target.code.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (target.symbol.isNotBlank()) {
                    Text(
                        text = target.symbol,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatter.format(convertedAmount),
                color = if (convertedAmount > 0.0) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "1 ${sourceCurrency.code.uppercase()} = ${rateFormatter.format(rate)}",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CameraPermissionPrompt(
    isRussian: Boolean,
    onGrant: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.scanCameraPermissionTitle(isRussian),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = AppStrings.scanCameraPermissionDesc(isRussian),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGrant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = AppStrings.scanGrantPermission(isRussian),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = AppStrings.cancel(isRussian))
                }
            }
        }
    }
}
