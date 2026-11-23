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
    }

    suspend fun fetchFiatRates(baseCurrency: String): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/${baseCurrency.uppercase()}")
                .header("User-Agent", "WorldPricesAndroid/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val parsed = json.decodeFromString<OpenExchangeRatesResponse>(body)

                val lowercaseRates = parsed.rates.mapKeys { it.key.lowercase() }
                Result.success(lowercaseRates)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
