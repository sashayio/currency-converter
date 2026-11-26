package com.worldprices.converter

import com.worldprices.converter.ui.scanner.OcrLine
import com.worldprices.converter.ui.scanner.PriceTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PriceTextParserTest {

    @Test
    fun testParseDollarPrice() {
        val parsed = PriceTextParser.parse("Special Offer: $19.99 today!")
        assertNotNull(parsed)
        assertEquals(19.99, parsed!!.amount, 0.001)
        assertEquals("USD", parsed.currencyCode)
    }

    @Test
    fun testParseEuroCommaPrice() {
        val parsed = PriceTextParser.parse("Price: 49,90 € incl. VAT")
        assertNotNull(parsed)
        assertEquals(49.90, parsed!!.amount, 0.001)
        assertEquals("EUR", parsed.currencyCode)
    }

    @Test
    fun testParseRublePriceWithSpace() {
        val parsed = PriceTextParser.parse("Итого: 1 500 ₽")
        assertNotNull(parsed)
        assertEquals(1500.0, parsed!!.amount, 0.001)
        assertEquals("RUB", parsed.currencyCode)
    }

    @Test
    fun testParseTurkishLiraPrice() {
        val parsed = PriceTextParser.parse("150 TL Menü")
        assertNotNull(parsed)
        assertEquals(150.0, parsed!!.amount, 0.001)
        assertEquals("TRY", parsed.currencyCode)
    }

    @Test
    fun testParseKazakhstanTenge() {
        val parsed = PriceTextParser.parse("Цена: 15000 ₸")
        assertNotNull(parsed)
        assertEquals(15000.0, parsed!!.amount, 0.001)
        assertEquals("KZT", parsed.currencyCode)
    }

    @Test
    fun testParseDirhamPrice() {
        val parsed = PriceTextParser.parse("Total: 120 AED")
        assertNotNull(parsed)
        assertEquals(120.0, parsed!!.amount, 0.001)
        assertEquals("AED", parsed.currencyCode)
    }

    @Test
    fun testParseDecimalPriceWithoutCurrency() {
        val parsed = PriceTextParser.parse("TOTAL 250.00")
        assertNotNull(parsed)
        assertEquals(250.0, parsed!!.amount, 0.001)
    }

    @Test
    fun testRejectOrdinaryTextWithRandomNumbers() {
        // Plain text with quantities or numbers should be REJECTED (returns null)
        assertNull(PriceTextParser.parse("Привет, это текст со словом привет и 5 яблок"))
        assertNull(PriceTextParser.parse("Model X200 with 16GB RAM and 512GB SSD"))
        assertNull(PriceTextParser.parse("Date: 12.05.2024, Page 4"))
        assertNull(PriceTextParser.parse("Time: 14:30 in room 12"))
        assertNull(PriceTextParser.parse("Вес: 500г, объем: 200мл"))
        assertNull(PriceTextParser.parse("Только 10% скидка сегодня"))
        assertNull(PriceTextParser.parse("Just a regular message without any price tag"))
    }

    @Test
    fun testSanitizeNumberFormats() {
        assertEquals(1250.50, PriceTextParser.sanitizeNumber("1,250.50")!!, 0.001)
        assertEquals(1250.50, PriceTextParser.sanitizeNumber("1.250,50")!!, 0.001)
        assertEquals(15000.0, PriceTextParser.sanitizeNumber("15 000")!!, 0.001)
        assertEquals(99.0, PriceTextParser.sanitizeNumber("99.-")!!, 0.001)
    }

    @Test
    fun testSpatialRussianPromoCardTag() {
        // Simulates exampleprice/price.png:
        // Main price: huge "229" + smaller bottom-right "99₽"
        // Distractors: "-15%", "200Г", "5" (logo), "269" + "99" (non-card price), footer metadata
        val lines = listOf(
            OcrLine("-15%", left = 20, top = 80, right = 110, bottom = 140),
            OcrLine("200Г", left = 140, top = 60, right = 210, bottom = 85),
            OcrLine("5", left = 40, top = 160, right = 70, bottom = 190),
            OcrLine("269", left = 150, top = 110, right = 230, bottom = 165),
            OcrLine("99", left = 232, top = 110, right = 265, bottom = 135),
            OcrLine("229", left = 240, top = 150, right = 420, bottom = 270),
            OcrLine("99₽", left = 425, top = 210, right = 490, bottom = 255),
            OcrLine("03.09.25 12:13 4230417", left = 140, top = 285, right = 450, bottom = 305)
        )

        val parsed = PriceTextParser.parseSpatial(lines, "RUB")
        assertNotNull(parsed)
        assertEquals(229.99, parsed!!.amount, 0.001)
        assertEquals("RUB", parsed.currencyCode)
    }

    @Test
    fun testSpatialRussianStandardTagWithSuperscriptKopecks() {
        // Simulates exampleprice/price2.jpg:
        // Main price: huge "49" + medium top-right superscript "99" (no currency symbol on same line)
        // Distractors: "500г", barcode, date, time
        val lines = listOf(
            OcrLine("500г", left = 60, top = 60, right = 130, bottom = 90),
            OcrLine("49", left = 220, top = 110, right = 350, bottom = 230),
            OcrLine("99", left = 355, top = 110, right = 415, bottom = 155),
            OcrLine("₽/шт.", left = 360, top = 170, right = 420, bottom = 200),
            OcrLine("05.01.21 13:07 4607179354628", left = 60, top = 260, right = 400, bottom = 280)
        )

        val parsed = PriceTextParser.parseSpatial(lines, "RUB")
        assertNotNull(parsed)
        assertEquals(49.99, parsed!!.amount, 0.001)
    }

    @Test
    fun testSpatialUsSaleTagWithoutDollarSign() {
        // Simulates exampleprice/scale_1200.png:
        // Huge "7.99" (SALE PRICE, no $ sign) vs medium "10.99" (regular price) + "66.6¢", "91.6¢", "12 OZ", barcode
        val lines = listOf(
            OcrLine("12 OZ", left = 30, top = 50, right = 90, bottom = 70),
            OcrLine("10.99", left = 40, top = 120, right = 160, bottom = 190),
            OcrLine("91.6¢", left = 180, top = 140, right = 230, bottom = 165),
            OcrLine("7.99", left = 270, top = 95, right = 470, bottom = 220),
            OcrLine("66.6¢", left = 475, top = 180, right = 525, bottom = 205),
            OcrLine("0078535783416", left = 30, top = 240, right = 200, bottom = 258)
        )

        val parsed = PriceTextParser.parseSpatial(lines, "USD")
        assertNotNull(parsed)
        assertEquals(7.99, parsed!!.amount, 0.001)
    }

    @Test
    fun testSpatialSingleLineSpaceSeparatedKopecks() {
        val lines = listOf(
            OcrLine("229 99", left = 100, top = 100, right = 300, bottom = 220),
            OcrLine("269.99", left = 100, top = 40, right = 200, bottom = 85)
        )

        val parsed = PriceTextParser.parseSpatial(lines, "RUB")
        assertNotNull(parsed)
        assertEquals(229.99, parsed!!.amount, 0.001)
    }
}
