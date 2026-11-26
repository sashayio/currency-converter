package com.worldprices.converter.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.worldprices.converter.data.model.ThemeMode
import com.worldprices.converter.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesDataStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_SOURCE = stringPreferencesKey("source_currency")
        private val KEY_TARGETS = stringPreferencesKey("target_currencies")
        private val KEY_CUSTOM_RATES = stringPreferencesKey("custom_rates")
        private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("has_seen_onboarding")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("language")

        private val KEY_CACHED_RATES = stringPreferencesKey("cached_rates")
        private val KEY_CACHED_TIME = longPreferencesKey("cached_rates_time")
        private val KEY_CACHED_BASE = stringPreferencesKey("cached_rates_base")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val source = prefs[KEY_SOURCE] ?: "rub"
        val targetsJson = prefs[KEY_TARGETS]
        val targets = if (!targetsJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<String>>(targetsJson)
            } catch (e: Exception) {
                listOf("usd", "eur", "byn")
            }
        } else {
            listOf("usd", "eur", "byn")
        }

        val customRatesJson = prefs[KEY_CUSTOM_RATES]
        val customRates = if (!customRatesJson.isNullOrBlank()) {
            try {
                json.decodeFromString<Map<String, Double>>(customRatesJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val hasSeen = prefs[KEY_ONBOARDING_SEEN] ?: false
        val themeStr = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
        val theme = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.SYSTEM }
        val systemDefaultLang = if (java.util.Locale.getDefault().language.equals("ru", ignoreCase = true)) "ru" else "en"
        val lang = prefs[KEY_LANGUAGE] ?: systemDefaultLang

        UserPreferences(
            sourceCurrency = source,
            targetCurrencies = targets,
            customRates = customRates,
            hasSeenOnboarding = hasSeen,
            themeMode = theme,
            language = lang
        )
    }

    suspend fun setSourceCurrency(code: String) {
        context.dataStore.edit { it[KEY_SOURCE] = code.lowercase() }
    }

    suspend fun setTargetCurrencies(targets: List<String>) {
        val targetsJson = json.encodeToString(targets.map { it.lowercase() })
        context.dataStore.edit { it[KEY_TARGETS] = targetsJson }
    }

    suspend fun setCustomRate(sourceCode: String, targetCode: String, rate: Double) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_CUSTOM_RATES]
            val currentMap = if (!currentJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<Map<String, Double>>(currentJson).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }
            val pairKey = "${sourceCode.lowercase()}_${targetCode.lowercase()}"
            currentMap[pairKey] = rate
            prefs[KEY_CUSTOM_RATES] = json.encodeToString(currentMap)
        }
    }

    suspend fun resetCustomRate(sourceCode: String, targetCode: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_CUSTOM_RATES] ?: return@edit
            val currentMap = try {
                json.decodeFromString<Map<String, Double>>(currentJson).toMutableMap()
            } catch (e: Exception) {
                return@edit
            }
            val pairKey = "${sourceCode.lowercase()}_${targetCode.lowercase()}"
            currentMap.remove(pairKey)
            prefs[KEY_CUSTOM_RATES] = json.encodeToString(currentMap)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_SEEN] = completed }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun saveCachedRates(base: String, rates: Map<String, Double>, time: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CACHED_BASE] = base.lowercase()
            prefs[KEY_CACHED_RATES] = json.encodeToString(rates)
            prefs[KEY_CACHED_TIME] = time
        }
    }

    suspend fun getCachedRates(): Triple<String, Map<String, Double>, Long>? {
        val prefs = context.dataStore.data.firstOrNull() ?: return null
        val base = prefs[KEY_CACHED_BASE] ?: return null
        val ratesJson = prefs[KEY_CACHED_RATES] ?: return null
        val time = prefs[KEY_CACHED_TIME] ?: return null
        return try {
            val rates = json.decodeFromString<Map<String, Double>>(ratesJson)
            Triple(base, rates, time)
        } catch (e: Exception) {
            null
        }
    }
}
