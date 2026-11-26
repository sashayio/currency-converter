package com.worldprices.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationAndFormattingTest {

    @Test
    fun testAmountParsingWithCommaAndDot() {
        val inputDot = "100.50".replace(',', '.')
        val parsedDot = inputDot.toDoubleOrNull() ?: 0.0
        assertEquals(100.50, parsedDot, 0.0001)

        val inputComma = "100,50".replace(',', '.')
        val parsedComma = inputComma.toDoubleOrNull() ?: 0.0
        assertEquals(100.50, parsedComma, 0.0001)

        val inputEmpty = "".replace(',', '.')
        val parsedEmpty = inputEmpty.toDoubleOrNull() ?: 0.0
        assertEquals(0.0, parsedEmpty, 0.0001)
    }

    @Test
    fun testConversionCalculation() {
        val amount = 1000.0
        val rateRubToUsd = 0.0104166667 // ~96 RUB per 1 USD
        val converted = amount * rateRubToUsd
        assertEquals(10.42, converted, 0.01)
    }

    @Test
    fun testReciprocalRates() {
        val usdInRub = 96.0
        val rubInUsd = 1.0 / usdInRub
        assertEquals(0.01041666, rubInUsd, 0.00001)

        val backToUsdInRub = 1.0 / rubInUsd
        assertEquals(96.0, backToUsdInRub, 0.00001)
    }

    @Test
    fun testOfflineCrossRateCalculation() {
        // Cached rates with base RUB: 1 RUB = 0.01 USD, 1 RUB = 0.009 EUR
        val cachedBase = "rub"
        val cachedRates = mapOf("usd" to 0.01, "eur" to 0.009, "rub" to 1.0)

        // User switches base to USD offline
        val newBase = "usd"
        val baseInCached = cachedRates[newBase]!!
        val crossRates = cachedRates.mapValues { (_, rate) -> rate / baseInCached }.toMutableMap()
        crossRates[cachedBase] = 1.0 / baseInCached
        crossRates[newBase] = 1.0

        // 1 USD = 100 RUB
        assertEquals(100.0, crossRates["rub"]!!, 0.0001)
        // 1 USD = 0.9 EUR
        assertEquals(0.9, crossRates["eur"]!!, 0.0001)
        // 1 USD = 1.0 USD
        assertEquals(1.0, crossRates["usd"]!!, 0.0001)
    }

    @Test
    fun testExpressionEvaluator() {
        val simple = com.worldprices.converter.ui.utils.AmountExpressionEvaluator.evaluate("120 + 45")
        org.junit.Assert.assertNotNull(simple)
        assertEquals(165.0, simple!!.value, 0.0001)
        org.junit.Assert.assertTrue(simple.isExpression)

        val precedence = com.worldprices.converter.ui.utils.AmountExpressionEvaluator.evaluate("100 + 50 * 2")
        assertEquals(200.0, precedence!!.value, 0.0001)

        val parens = com.worldprices.converter.ui.utils.AmountExpressionEvaluator.evaluate("(100 + 50) * 1.1")
        assertEquals(165.0, parens!!.value, 0.0001)

        val plain = com.worldprices.converter.ui.utils.AmountExpressionEvaluator.evaluate("1000,5")
        assertEquals(1000.5, plain!!.value, 0.0001)
        org.junit.Assert.assertFalse(plain.isExpression)

        val divZero = com.worldprices.converter.ui.utils.AmountExpressionEvaluator.evaluate("100/0")
        org.junit.Assert.assertNull(divZero)
    }
}
