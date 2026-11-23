package com.worldprices.converter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldprices.converter.data.model.Currency
import com.worldprices.converter.data.model.CurrencyCategory

@Composable
fun CurrencyFlag(
    currency: Currency,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    if (currency.category == CurrencyCategory.CRYPTO || currency.countryCode == null) {
        val bgColor = try {
            if (!currency.colorHex.isNullOrBlank()) {
                Color(android.graphics.Color.parseColor(currency.colorHex))
            } else {
                Color(0xFF6366F1)
            }
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currency.symbol.take(3),
                color = Color.White,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        val flagEmoji = countryCodeToEmoji(currency.countryCode)
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = flagEmoji,
                fontSize = (size.value * 0.58f).sp
            )
        }
    }
}

private fun countryCodeToEmoji(countryCode: String): String {
    if (countryCode.equals("eu", ignoreCase = true)) {
        return "🇪🇺"
    }
    if (countryCode.length != 2) return "🌐"
    val firstChar = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondChar = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}
