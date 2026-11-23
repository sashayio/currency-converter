package com.worldprices.converter.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.worldprices.converter.R
import com.worldprices.converter.data.model.Currency
import com.worldprices.converter.ui.components.CurrencyFlag
import com.worldprices.converter.ui.theme.AccentGold
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun CustomRateDialog(
    sourceCurrency: Currency,
    targetCurrency: Currency,
    currentRate: Double,
    marketRate: Double,
    currentAmount: Double,
    isCustom: Boolean,
    isRussian: Boolean,
    onDismiss: () -> Unit,
    onApply: (rate: Double) -> Unit,
    onReset: () -> Unit
) {
    val symbols = DecimalFormatSymbols(Locale.US).apply { decimalSeparator = '.' }
    val format6 = DecimalFormat("0.######", symbols)

    // Rate representation:
    // targetToSource: 1 Target in Source (e.g. 1 USD = 96.00 RUB) = 1.0 / rate
    // sourceToTarget: 1 Source in Target (e.g. 1 RUB = 0.0104 USD) = rate
    var sourceToTargetText by remember { mutableStateOf(format6.format(currentRate)) }
    var targetToSourceText by remember {
        mutableStateOf(if (currentRate > 0) format6.format(1.0 / currentRate) else "")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun updateFromSourceToTarget(text: String) {
        val clean = text.replace(',', '.')
        sourceToTargetText = clean
        errorMessage = null
        val num = clean.toDoubleOrNull()
        if (num != null && num > 0) {
            targetToSourceText = format6.format(1.0 / num)
        } else {
            targetToSourceText = ""
        }
    }

    fun updateFromTargetToSource(text: String) {
        val clean = text.replace(',', '.')
        targetToSourceText = clean
        errorMessage = null
        val num = clean.toDoubleOrNull()
        if (num != null && num > 0) {
            sourceToTargetText = format6.format(1.0 / num)
        } else {
            sourceToTargetText = ""
        }
    }

    val previewRate = sourceToTargetText.toDoubleOrNull() ?: currentRate
    val previewCalculated = currentAmount * previewRate
    val amountFormat = DecimalFormat("#,##0.00", symbols)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Text(
                    text = stringResource(id = R.string.custom_rate_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pair Flags & Codes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    CurrencyFlag(currency = sourceCurrency, size = 32.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "⇄", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    CurrencyFlag(currency = targetCurrency, size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${sourceCurrency.code.uppercase()} → ${targetCurrency.code.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${sourceCurrency.localizedName(isRussian)} → ${targetCurrency.localizedName(isRussian)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field 1: Target to Source (e.g. 1 USD = 96.00 RUB)
                OutlinedTextField(
                    value = targetToSourceText,
                    onValueChange = { updateFromTargetToSource(it) },
                    label = { Text("1 ${targetCurrency.code.uppercase()} =") },
                    suffix = { Text(sourceCurrency.code.uppercase(), fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 2: Source to Target (e.g. 1 RUB = 0.0104 USD)
                OutlinedTextField(
                    value = sourceToTargetText,
                    onValueChange = { updateFromSourceToTarget(it) },
                    label = { Text("1 ${sourceCurrency.code.uppercase()} =") },
                    suffix = { Text(targetCurrency.code.uppercase(), fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Market rate hint & Use Market button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val marketT2S = if (marketRate > 0) format6.format(1.0 / marketRate) else ""
                    Column {
                        Text(
                            text = stringResource(id = R.string.market_rate_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "1 ${targetCurrency.code.uppercase()} = $marketT2S ${sourceCurrency.code.uppercase()}",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = {
                            if (marketRate > 0) {
                                sourceToTargetText = format6.format(marketRate)
                                targetToSourceText = format6.format(1.0 / marketRate)
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.use_market_btn))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview calculation card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.rate_preview_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${amountFormat.format(currentAmount)} ${sourceCurrency.code.uppercase()} = ${amountFormat.format(previewCalculated)} ${targetCurrency.code.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isCustom) {
                        OutlinedButton(
                            onClick = {
                                onReset()
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(id = R.string.reset))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = {
                            val finalRate = sourceToTargetText.toDoubleOrNull()
                            if (finalRate == null || finalRate <= 0.0) {
                                errorMessage = "Введите корректное число > 0"
                            } else {
                                onApply(finalRate)
                                onDismiss()
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.apply))
                    }
                }
            }
        }
    }
}
