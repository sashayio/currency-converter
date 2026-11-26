package com.worldprices.converter.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldprices.converter.data.model.CurrencyRegistry
import com.worldprices.converter.data.model.ThemeMode
import com.worldprices.converter.ui.components.AmountInputField
import com.worldprices.converter.ui.components.AppHeader
import com.worldprices.converter.ui.components.CurrencyCard
import com.worldprices.converter.ui.dialogs.CurrencyPickerSheet
import com.worldprices.converter.ui.dialogs.CustomRateDialog
import com.worldprices.converter.ui.dialogs.SettingsSheet
import com.worldprices.converter.ui.scanner.CurrencyScannerScreen
import com.worldprices.converter.ui.utils.AppStrings
import com.worldprices.converter.ui.utils.ShareHelper
import com.worldprices.converter.ui.utils.UrlHelper

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isRussian = uiState.isRussian

    val isDarkTheme = when (uiState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Header
        AppHeader(
            isRefreshing = uiState.isLoading,
            isDarkTheme = isDarkTheme,
            isRussian = isRussian,
            onRefresh = { viewModel.loadRates(forceRefresh = true) },
            onToggleTheme = { viewModel.toggleTheme(isDarkTheme) },
            onOpenScanner = { viewModel.setScannerOpen(true) },
            onOpenSettings = { viewModel.setSettingsOpen(true) }
        )

        // Offline Banner
        AnimatedVisibility(visible = uiState.isOffline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.ratesOffline(isRussian, uiState.lastUpdatedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Error Banner (when offline with zero cache)
        AnimatedVisibility(visible = uiState.errorMessage != null && uiState.marketRates.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.ratesFailed(isRussian),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = AppStrings.retry(isRussian),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.loadRates(forceRefresh = true) }
                )
            }
        }

        // Amount Input Field
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            AmountInputField(
                sourceCurrency = uiState.sourceCurrency,
                amountText = uiState.amountText,
                evaluatedValue = uiState.amountValue,
                isExpressionActive = uiState.isExpressionActive,
                onAmountChange = { viewModel.setAmountText(it) },
                onSourceClick = { viewModel.setPickingSource(true) },
                onScanClick = { viewModel.setScannerOpen(true) }
            )
        }

        // Minimalist Google M3 Quick Chips: Fee/Tax + Presets
        val feeOptions = listOf(0.0 to "+0%", 2.5 to "+2.5%", 7.0 to "+7%", 10.0 to "+10%")
        val presets = listOf(
            Triple("default", AppStrings.presetDefault(isRussian), listOf("usd", "eur", "rub", "gbp", "cny")),
            Triple("travel", AppStrings.presetTravel(isRussian), listOf("eur", "usd", "try", "thb", "aed")),
            Triple("crypto", AppStrings.presetCrypto(isRussian), listOf("usdt", "btc", "eth", "sol", "ton"))
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            items(feeOptions) { (feeVal, label) ->
                val selected = uiState.feePercent == feeVal
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setFeePercent(feeVal) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium) },
                    shape = RoundedCornerShape(999.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            items(presets) { (_, label, codes) ->
                AssistChip(
                    onClick = { viewModel.applyPreset(codes) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(999.dp)
                )
            }
        }

        // Target Currencies List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(uiState.targetCurrencies, key = { _, item -> item.code }) { index, target ->
                val rate = uiState.getEffectiveRate(target.code)
                val isCustom = uiState.isCustomRate(target.code)

                CurrencyCard(
                    currency = target,
                    sourceCurrency = uiState.sourceCurrency,
                    amount = uiState.effectiveAmountValue,
                    rate = rate,
                    isCustomRate = isCustom,
                    isRussian = isRussian,
                    onEditRate = { viewModel.openCustomRateDialog(target) },
                    onSwapSource = { viewModel.swapWithSource(target.code) },
                    onRemove = { viewModel.removeTargetCurrency(target.code) },
                    onMoveUp = { viewModel.moveTargetUp(index) },
                    onMoveDown = { viewModel.moveTargetDown(index) },
                    canMoveUp = index > 0,
                    canMoveDown = index < uiState.targetCurrencies.lastIndex,
                    modifier = Modifier.animateItemPlacement(tween(durationMillis = 220))
                )
            }

            // Add currency button item
            item {
                OutlinedButton(
                    onClick = { viewModel.setPickingTargets(true) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.addCurrency(isRussian),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Footer item
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tip Author link
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                UrlHelper.openUrl(context, "https://dalink.to/sashenkatg")
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.buyCoffee(isRussian),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (uiState.lastUpdatedDate.isNotBlank()) {
                        Text(
                            text = AppStrings.ratesUpdated(isRussian, uiState.lastUpdatedDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    // Modal: Custom Rate Dialog
    uiState.editingTargetCurrency?.let { target ->
        CustomRateDialog(
            sourceCurrency = uiState.sourceCurrency,
            targetCurrency = target,
            currentRate = uiState.getEffectiveRate(target.code),
            marketRate = uiState.getMarketRate(target.code),
            currentAmount = uiState.amountValue,
            isCustom = uiState.isCustomRate(target.code),
            isRussian = isRussian,
            onDismiss = { viewModel.closeCustomRateDialog() },
            onApply = { newRate -> viewModel.setCustomRate(target.code, newRate) },
            onReset = { viewModel.resetCustomRate(target.code) }
        )
    }

    // Modal: Source Currency Picker
    if (uiState.isPickingSource) {
        CurrencyPickerSheet(
            title = AppStrings.sourceCurrency(isRussian),
            isRussian = isRussian,
            selectedCodes = setOf(uiState.sourceCurrency.code.lowercase()),
            multiSelect = false,
            onDismiss = { viewModel.setPickingSource(false) },
            onSelectCurrency = { selected ->
                viewModel.setSourceCurrency(selected.code)
            }
        )
    }

    // Modal: Target Currencies Picker
    if (uiState.isPickingTargets) {
        CurrencyPickerSheet(
            title = AppStrings.targetCurrencies(isRussian),
            isRussian = isRussian,
            selectedCodes = uiState.targetCurrencies.map { it.code.lowercase() }.toSet(),
            multiSelect = true,
            onDismiss = { viewModel.setPickingTargets(false) },
            onSelectCurrency = { },
            onToggleCurrency = { toggled ->
                viewModel.toggleTargetCurrency(toggled)
            }
        )
    }

    // Modal: Settings Sheet
    if (uiState.isSettingsOpen) {
        SettingsSheet(
            currentThemeMode = uiState.themeMode,
            currentLanguage = uiState.language,
            onThemeChange = { viewModel.setThemeMode(it) },
            onLanguageChange = { viewModel.setLanguage(it) },
            onShare = {
                ShareHelper.shareConversionSummary(
                    context = context,
                    sourceCurrency = uiState.sourceCurrency,
                    amount = uiState.effectiveAmountValue,
                    targets = uiState.targetCurrencies,
                    effectiveRates = uiState.effectiveRatesMap,
                    isRussian = isRussian
                )
            },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }

    // Modal: Currency Live Camera Scanner
    if (uiState.isScannerOpen) {
        CurrencyScannerScreen(
            sourceCurrency = uiState.sourceCurrency,
            targetCurrencies = uiState.targetCurrencies,
            effectiveRates = uiState.effectiveRatesMap,
            marketRates = uiState.marketRates,
            customRates = uiState.customRates,
            isRussian = isRussian,
            onDismiss = { viewModel.setScannerOpen(false) },
            onApply = { amount, detectedCurrency ->
                viewModel.applyScannedAmount(amount, detectedCurrency)
            }
        )
    }
}
