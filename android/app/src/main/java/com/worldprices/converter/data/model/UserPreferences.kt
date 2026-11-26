package com.worldprices.converter.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val sourceCurrency: String = "rub",
    val targetCurrencies: List<String> = listOf("usd", "eur", "byn"),
    val customRates: Map<String, Double> = emptyMap(),
    val hasSeenOnboarding: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "ru"
)
