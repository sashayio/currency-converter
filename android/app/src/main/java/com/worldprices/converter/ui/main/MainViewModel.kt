package com.worldprices.converter.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.worldprices.converter.data.model.Currency
import com.worldprices.converter.data.model.CurrencyRegistry
import com.worldprices.converter.data.model.ThemeMode
import com.worldprices.converter.data.model.UserPreferences
import com.worldprices.converter.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class MainUiState(
    val sourceCurrency: Currency = CurrencyRegistry.defaultSource,
    val targetCurrencies: List<Currency> = CurrencyRegistry.defaultTargets,
    val amountText: String = "1000",
    val amountValue: Double = 1000.0,
    val marketRates: Map<String, Double> = emptyMap(),
    val customRates: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val lastUpdatedDate: String = "",
    val hasSeenOnboarding: Boolean = false,
    val isPreferencesLoaded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "ru",
    val editingTargetCurrency: Currency? = null,
    val isPickingSource: Boolean = false,
    val isPickingTargets: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isScannerOpen: Boolean = false,
    val errorMessage: String? = null
) {
    val isRussian: Boolean get() = language == "ru"

    fun getEffectiveRate(targetCode: String): Double {
        val pairKey = "${sourceCurrency.code.lowercase()}_${targetCode.lowercase()}"
        val custom = customRates[pairKey]
        if (custom != null && custom > 0.0) return custom
        return marketRates[targetCode.lowercase()] ?: 0.0
    }

    fun isCustomRate(targetCode: String): Boolean {
        val pairKey = "${sourceCurrency.code.lowercase()}_${targetCode.lowercase()}"
        val custom = customRates[pairKey]
        return custom != null && custom > 0.0
    }

    fun getMarketRate(targetCode: String): Double {
        return marketRates[targetCode.lowercase()] ?: 0.0
    }

    val effectiveRatesMap: Map<String, Double>
        get() = targetCurrencies.associate { it.code.lowercase() to getEffectiveRate(it.code) }
}

class MainViewModel(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.preferencesFlow.collectLatest { prefs ->
                val source = CurrencyRegistry.getByCode(prefs.sourceCurrency) ?: CurrencyRegistry.defaultSource
                val targets = prefs.targetCurrencies.mapNotNull { CurrencyRegistry.getByCode(it) }
                    .ifEmpty { CurrencyRegistry.defaultTargets }

                _uiState.update { current ->
                    current.copy(
                        sourceCurrency = source,
                        targetCurrencies = targets,
                        customRates = prefs.customRates,
                        hasSeenOnboarding = prefs.hasSeenOnboarding,
                        isPreferencesLoaded = true,
                        themeMode = prefs.themeMode,
                        language = prefs.language
                    )
                }

                loadRates(source.code, forceRefresh = false)
            }
        }
    }

    fun loadRates(baseCode: String? = null, forceRefresh: Boolean = false) {
        val base = baseCode ?: _uiState.value.sourceCurrency.code
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getRates(base, forceRefresh)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        marketRates = data.rates,
                        isOffline = data.isOffline,
                        lastUpdatedDate = data.formattedDate,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Rates error"
                    )
                }
            }
        }
    }

    fun setAmountText(text: String) {
        val clean = text.replace(',', '.')
        val parsed = clean.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(amountText = clean, amountValue = parsed) }
    }

    fun setSourceCurrency(code: String) {
        viewModelScope.launch {
            repository.setSourceCurrency(code)
            loadRates(code, forceRefresh = true)
        }
    }

    fun addTargetCurrency(code: String) {
        val current = _uiState.value.targetCurrencies.map { it.code }
        if (!current.contains(code.lowercase())) {
            val updated = current + code.lowercase()
            viewModelScope.launch {
                repository.setTargetCurrencies(updated)
            }
        }
    }

    fun removeTargetCurrency(code: String) {
        val current = _uiState.value.targetCurrencies.map { it.code }
        val updated = current.filterNot { it.equals(code, ignoreCase = true) }
        viewModelScope.launch {
            repository.setTargetCurrencies(updated)
        }
    }

    fun toggleTargetCurrency(currency: Currency) {
        val code = currency.code.lowercase()
        val current = _uiState.value.targetCurrencies.map { it.code.lowercase() }
        val updated = if (current.contains(code)) {
            if (current.size > 1) current - code else current
        } else {
            current + code
        }
        viewModelScope.launch {
            repository.setTargetCurrencies(updated)
        }
    }

    fun moveTargetUp(index: Int) {
        if (index <= 0) return
        val list = _uiState.value.targetCurrencies.toMutableList()
        val temp = list[index]
        list[index] = list[index - 1]
        list[index - 1] = temp
        viewModelScope.launch {
            repository.setTargetCurrencies(list.map { it.code })
        }
    }

    fun moveTargetDown(index: Int) {
        val list = _uiState.value.targetCurrencies.toMutableList()
        if (index >= list.lastIndex) return
        val temp = list[index]
        list[index] = list[index + 1]
        list[index + 1] = temp
        viewModelScope.launch {
            repository.setTargetCurrencies(list.map { it.code })
        }
    }

    fun setCustomRate(targetCode: String, rate: Double) {
        viewModelScope.launch {
            repository.setCustomRate(_uiState.value.sourceCurrency.code, targetCode, rate)
        }
    }

    fun resetCustomRate(targetCode: String) {
        viewModelScope.launch {
            repository.resetCustomRate(_uiState.value.sourceCurrency.code, targetCode)
        }
    }

    fun completeOnboarding(source: String, targets: List<String>) {
        viewModelScope.launch {
            repository.setSourceCurrency(source)
            repository.setTargetCurrencies(targets)
            repository.setOnboardingCompleted(true)
            loadRates(source, forceRefresh = true)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun toggleTheme() {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun openCustomRateDialog(target: Currency) {
        _uiState.update { it.copy(editingTargetCurrency = target) }
    }

    fun closeCustomRateDialog() {
        _uiState.update { it.copy(editingTargetCurrency = null) }
    }

    fun setPickingSource(picking: Boolean) {
        _uiState.update { it.copy(isPickingSource = picking) }
    }

    fun setPickingTargets(picking: Boolean) {
        _uiState.update { it.copy(isPickingTargets = picking) }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun setScannerOpen(open: Boolean) {
        _uiState.update { it.copy(isScannerOpen = open) }
    }

    fun applyScannedAmount(amount: Double, sourceCode: String? = null) {
        val displayAmount = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            "%.2f".format(Locale.US, amount).trimEnd('0').trimEnd('.')
        }
        setAmountText(displayAmount)
        if (sourceCode != null && sourceCode.isNotBlank()) {
            val found = CurrencyRegistry.getByCode(sourceCode)
            if (found != null && found.code.lowercase() != _uiState.value.sourceCurrency.code.lowercase()) {
                setSourceCurrency(found.code)
            }
        }
        setScannerOpen(false)
    }

    class Factory(private val repository: CurrencyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
