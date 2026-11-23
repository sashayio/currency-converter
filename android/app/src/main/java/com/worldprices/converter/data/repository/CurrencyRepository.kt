package com.worldprices.converter.data.repository

import com.worldprices.converter.data.local.PreferencesDataStore
import com.worldprices.converter.data.model.RatesData
import com.worldprices.converter.data.model.ThemeMode
import com.worldprices.converter.data.model.UserPreferences
import com.worldprices.converter.data.remote.BinanceApi
import com.worldprices.converter.data.remote.ExchangeRateApi
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyRepository(
    private val preferencesDataStore: PreferencesDataStore,
    private val exchangeRateApi: ExchangeRateApi = ExchangeRateApi(),
    private val binanceApi: BinanceApi = BinanceApi()
) {
    companion object {
        private const val CACHE_DURATION_MS = 15 * 60 * 1000 // 15 mins
    }

    private var memoryRates: RatesData? = null

    val preferencesFlow: Flow<UserPreferences> = preferencesDataStore.userPreferencesFlow

    suspend fun getRates(baseCode: String, forceRefresh: Boolean = false): Result<RatesData> {
        val now = System.currentTimeMillis()
        val cached = memoryRates
        if (!forceRefresh && cached != null && cached.baseCode.equals(baseCode, ignoreCase = true) &&
            (now - cached.timestamp < CACHE_DURATION_MS)
        ) {
            return Result.success(cached)
        }

        // Try network fetch
        val fiatResult = exchangeRateApi.fetchFiatRates(baseCode)
        val cryptoResult = binanceApi.fetchCryptoPricesInUsdt()

        if (fiatResult.isSuccess) {
            val fiatRates = fiatResult.getOrThrow().toMutableMap()
            // 1 base = 1 base
            fiatRates[baseCode.lowercase()] = 1.0

            // If crypto prices are available, compute rates in terms of base currency:
            // cryptoResult has USD prices for coins (e.g. BTC = 90,000 USD)
            // fiatRates has base -> USD rate (fiatRates["usd"])
            // Example: 1 BTC in USD = 90000. 1 USD in base = 1 / fiatRates["usd"].
            // Therefore, 1 base in BTC = 1 / (BTC_price_in_usd * (1 / fiatRates["usd"]))
            //                          = fiatRates["usd"] / BTC_price_in_usd
            if (cryptoResult.isSuccess) {
                val cryptoPrices = cryptoResult.getOrThrow()
                val baseToUsdRate = fiatRates["usd"] ?: 1.0
                for ((coin, usdPrice) in cryptoPrices) {
                    if (usdPrice > 0.0) {
                        val baseToCoinRate = baseToUsdRate / usdPrice
                        fiatRates[coin] = baseToCoinRate
                    }
                }
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(now))

            val newRatesData = RatesData(
                baseCode = baseCode.lowercase(),
                rates = fiatRates,
                timestamp = now,
                isOffline = false,
                formattedDate = formattedDate
            )

            memoryRates = newRatesData
            preferencesDataStore.saveCachedRates(baseCode, fiatRates, now)
            return Result.success(newRatesData)
        }

        // Network failed -> Fallback to persistent disk cache
        val diskCache = preferencesDataStore.getCachedRates()
        if (diskCache != null) {
            val (cachedBase, cachedRates, time) = diskCache
            val reqBase = baseCode.lowercase()
            val effectiveRates: Map<String, Double> = if (cachedBase.equals(reqBase, ignoreCase = true)) {
                cachedRates
            } else {
                // Compute cross-rates if baseToNewBase exists in cached rates
                val baseInCached = cachedRates[reqBase]
                if (baseInCached != null && baseInCached > 0.0) {
                    val convertedMap = cachedRates.mapValues { (_, rate) -> rate / baseInCached }.toMutableMap()
                    convertedMap[cachedBase.lowercase()] = 1.0 / baseInCached
                    convertedMap[reqBase] = 1.0
                    convertedMap
                } else {
                    cachedRates
                }
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(time))
            val offlineRates = RatesData(
                baseCode = reqBase,
                rates = effectiveRates,
                timestamp = time,
                isOffline = true,
                formattedDate = formattedDate
            )
            memoryRates = offlineRates
            return Result.success(offlineRates)
        }

        return Result.failure(fiatResult.exceptionOrNull() ?: Exception("Failed to load exchange rates"))
    }

    suspend fun setSourceCurrency(code: String) = preferencesDataStore.setSourceCurrency(code)
    suspend fun setTargetCurrencies(targets: List<String>) = preferencesDataStore.setTargetCurrencies(targets)
    suspend fun setCustomRate(sourceCode: String, targetCode: String, rate: Double) =
        preferencesDataStore.setCustomRate(sourceCode, targetCode, rate)
    suspend fun resetCustomRate(sourceCode: String, targetCode: String) =
        preferencesDataStore.resetCustomRate(sourceCode, targetCode)
    suspend fun setOnboardingCompleted(completed: Boolean) =
        preferencesDataStore.setOnboardingCompleted(completed)
    suspend fun setThemeMode(mode: ThemeMode) = preferencesDataStore.setThemeMode(mode)
    suspend fun setLanguage(lang: String) = preferencesDataStore.setLanguage(lang)
}
