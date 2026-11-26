package com.worldprices.converter

import com.worldprices.converter.data.model.CurrencyCategory
import com.worldprices.converter.data.model.CurrencyRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRegistryTest {

    @Test
    fun testAllCurrenciesRegistered() {
        val all = CurrencyRegistry.getAll()
        // Must contain at least 50 currencies + 12 cryptos = 62+
        assertTrue("Expected 60+ currencies, found: ${all.size}", all.size >= 60)
    }

    @Test
    fun testDefaultSourceAndTargets() {
        val source = CurrencyRegistry.defaultSource
        assertEquals("rub", source.code.lowercase())

        val targets = CurrencyRegistry.defaultTargets
        assertTrue(targets.any { it.code == "usd" })
        assertTrue(targets.any { it.code == "eur" })
        assertTrue(targets.any { it.code == "byn" })
    }

    @Test
    fun testCryptoCurrenciesAvailable() {
        val cryptos = CurrencyRegistry.getByCategory(CurrencyCategory.CRYPTO)
        val codes = cryptos.map { it.code.lowercase() }
        assertTrue(codes.contains("btc"))
        assertTrue(codes.contains("eth"))
        assertTrue(codes.contains("usdt"))
        assertTrue(codes.contains("sol"))
        assertTrue(codes.contains("ton"))
    }

    @Test
    fun testSearchMatches() {
        val usd = CurrencyRegistry.getByCode("usd")
        assertNotNull(usd)
        assertTrue(usd!!.searchMatches("usd", isRussian = true))
        assertTrue(usd.searchMatches("доллар", isRussian = true))
        assertTrue(usd.searchMatches("dollar", isRussian = false))
        assertTrue(usd.searchMatches("$", isRussian = true))
        assertFalse(usd.searchMatches("euro", isRussian = false))
    }
}
