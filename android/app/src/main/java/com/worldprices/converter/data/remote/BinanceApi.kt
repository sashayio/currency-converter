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
        private val COINGECKO_IDS = mapOf(
            "bitcoin" to "btc",
            "ethereum" to "eth",
            "tether" to "usdt",
            "binancecoin" to "bnb",
            "ripple" to "xrp",
            "solana" to "sol",
            "cardano" to "ada",
            "dogecoin" to "doge",
            "polkadot" to "dot",
            "litecoin" to "ltc",
            "the-open-network" to "ton",
            "tron" to "trx"
        )
    }

    suspend fun fetchCryptoPricesInUsdt(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        val binance = fetchFromBinance()
        if (binance.isSuccess) return@withContext binance
        fetchFromCoinGecko()
    }

    private fun fetchFromBinance(): Result<Map<String, Double>> {
        return try {
            val request = Request.Builder()
                .url(TICKER_URL)
                .header("User-Agent", "WorldPricesAndroid/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val body = response.body?.string() ?: return Result.failure(Exception("Empty body"))
                val tickers = json.decodeFromString<List<BinancePriceTicker>>(body)

                val prices = mutableMapOf<String, Double>()
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
                if (prices.size <= 1) Result.failure(Exception("No Binance prices")) else Result.success(prices)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFromCoinGecko(): Result<Map<String, Double>> {
        return try {
            val ids = COINGECKO_IDS.keys.joinToString(",")
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=$ids&vs_currencies=usd"
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

                val prices = mutableMapOf("usdt" to 1.0)
                for ((cgId, coinCode) in COINGECKO_IDS) {
                    val coinObj = root[cgId] as? kotlinx.serialization.json.JsonObject ?: continue
                    val usd = (coinObj["usd"] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull()
                    if (usd != null && usd > 0.0) {
                        prices[coinCode] = usd
                    }
                }
                Result.success(prices)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
