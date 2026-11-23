package com.worldprices.converter.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenExchangeRatesResponse(
    val result: String? = null,
    @SerialName("base_code") val baseCode: String = "USD",
    val rates: Map<String, Double> = emptyMap(),
    @SerialName("time_last_update_utc") val timeLastUpdateUtc: String? = null
)

@Serializable
data class BinancePriceTicker(
    val symbol: String,
    val price: String
)

data class RatesData(
    val baseCode: String,
    val rates: Map<String, Double>,
    val timestamp: Long = System.currentTimeMillis(),
    val isOffline: Boolean = false,
    val formattedDate: String = ""
)
