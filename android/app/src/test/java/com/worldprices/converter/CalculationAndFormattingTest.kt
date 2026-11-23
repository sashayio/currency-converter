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
}
