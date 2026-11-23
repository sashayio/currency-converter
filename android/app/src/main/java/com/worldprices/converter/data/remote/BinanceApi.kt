package com.worldprices.converter.data.remote

import com.worldprices.converter.data.model.BinancePriceTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class BinanceApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        private const val TICKER_URL = "https://api.binance.com/api/v3/ticker/price"
        private val TRACKED_SYMBOLS = setOf(
            "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
            "ADAUSDT", "DOGEUSDT", "DOTUSDT", "LTCUSDT", "TONUSDT", "TRXUSDT"
        )
    }

    suspend fun fetchCryptoPricesInUsdt(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(TICKER_URL)
                .header("User-Agent", "WorldPricesAndroid/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val tickers = json.decodeFromString<List<BinancePriceTicker>>(body)

            val prices = mutableMapOf<String, Double>()
            // 1 USDT = 1 USD
            prices["usdt"] = 1.0

            for (ticker in tickers) {
                if (ticker.symbol in TRACKED_SYMBOLS) {
                    val coin = ticker.symbol.removeSuffix("USDT").lowercase()
                    val price = ticker.price.toDoubleOrNull()
                    if (price != null && price > 0.0) {
                        prices[coin] = price
                    }
                }
            }
            Result.success(prices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
