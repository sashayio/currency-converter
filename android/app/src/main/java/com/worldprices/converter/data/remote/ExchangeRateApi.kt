package com.worldprices.converter.data.remote

import com.worldprices.converter.data.model.OpenExchangeRatesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ExchangeRateApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        private const val BASE_URL = "https://open.er-api.com/v6/latest"
        private const val FALLBACK_PAGES_DEV = "https://latest.currency-api.pages.dev/v1/currencies"
        private const val FALLBACK_JSDELIVR = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies"
    }

    suspend fun fetchFiatRates(baseCurrency: String): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        val primary = fetchOpenExchangeRates(baseCurrency)
        if (primary.isSuccess) return@withContext primary

        val code = baseCurrency.lowercase()
        val fallbackUrls = listOf(
            "$FALLBACK_PAGES_DEV/$code.min.json",
            "$FALLBACK_JSDELIVR/$code.min.json"
        )
        for (url in fallbackUrls) {
            val result = fetchCurrencyApiFallback(url, code)
            if (result.isSuccess) return@withContext result
        }

        primary
    }

    private fun fetchOpenExchangeRates(baseCurrency: String): Result<Map<String, Double>> {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/${baseCurrency.uppercase()}")
                .header("User-Agent", "WorldPricesAndroid/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val body = response.body?.string() ?: return Result.failure(Exception("Empty body"))
                val parsed = json.decodeFromString<OpenExchangeRatesResponse>(body)
                if (parsed.rates.isEmpty()) {
                    return Result.failure(Exception("Empty rates"))
                }
                Result.success(parsed.rates.mapKeys { it.key.lowercase() })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchCurrencyApiFallback(url: String, baseLower: String): Result<Map<String, Double>> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WorldPricesAndroid/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val body = response.body?.string() ?: return Result.failure(Exception("Empty body"))
                val root = json.parseToJsonElement(body) as? kotlinx.serialization.json.JsonObject
                    ?: return Result.failure(Exception("Invalid JSON"))
                val ratesObj = root[baseLower] as? kotlinx.serialization.json.JsonObject
                    ?: return Result.failure(Exception("Missing base rates"))

                val map = mutableMapOf<String, Double>()
                for ((k, v) in ratesObj) {
                    val num = (v as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull()
                    if (num != null && num > 0.0) {
                        map[k.lowercase()] = num
                    }
                }
                if (map.isEmpty()) Result.failure(Exception("Empty fallback rates")) else Result.success(map)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
