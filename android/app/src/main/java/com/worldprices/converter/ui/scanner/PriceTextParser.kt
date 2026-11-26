package com.worldprices.converter.ui.scanner

import android.graphics.Rect

data class OcrSymbol(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
}

data class OcrLine(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
}

data class ParsedPrice(
    val amount: Double,
    val currencyCode: String?,
    val rawText: String,
    val confidence: Float = 1.0f
)

object PriceTextParser {

    // Strong price indicator words in Russian and English
    private val priceKeywords = setOf(
        "цена", "ценник", "стоимость", "итого", "сумма", "всего", "к оплате", "расчет",
        "price", "total", "amount", "cost", "sum", "subtotal", "pay", "rate", "due", "sale"
    )

    // Unit words to reject (e.g. 500g, 100ml, 50%, 12 oz)
    private val unitPattern = Regex(
        """(?i)\d+(?:[.,]\d+)?\s*(?:г|гр|кг|мл|л|см|мм|м|шт|уп|упак|порц|порций|g|kg|ml|l|cm|mm|pcs|oz|lb|%|v|w|hz|mah|fps|rpm|gb|mb|tb|kb)\b"""
    )

    private val centPattern = Regex("""(?i)\d+(?:[.,]\d+)?\s*¢""")
    private val percentPattern = Regex("""(?i)[-+]?\d+(?:[.,]\d+)?\s*%""")

    // Date and time patterns to reject
    private val datePattern = Regex("""\b\d{1,2}[./\-]\d{1,2}[./\-]\d{2,4}\b""")
    private val timePattern = Regex("""\b\d{1,2}:\d{2}(?::\d{2})?\b""")

    // Known currency symbols and abbreviations
    private val symbolToCode = mapOf(
        "$" to "USD",
        "€" to "EUR",
        "£" to "GBP",
        "₽" to "RUB",
        "руб" to "RUB",
        "р." to "RUB",
        "₺" to "TRY",
        "tl" to "TRY",
        "₸" to "KZT",
        "тенге" to "KZT",
        "тг" to "KZT",
        "₴" to "UAH",
        "грн" to "UAH",
        "₹" to "INR",
        "¥" to "JPY",
        "₩" to "KRW",
        "฿" to "THB",
        "₫" to "VND",
        "₾" to "GEL",
        "лари" to "GEL",
        "zł" to "PLN",
        "₪" to "ILS",
        "chf" to "CHF",
        "fr." to "CHF",
        "aed" to "AED",
        "dh" to "AED",
        "dhs" to "AED",
        "дирхам" to "AED",
        "sar" to "SAR",
        "sr" to "SAR",
        "byn" to "BYN",
        "uzs" to "UZS",
        "сум" to "UZS",
        "azn" to "AZN",
        "манат" to "AZN",
        "amd" to "AMD",
        "драм" to "AMD",
        "cad" to "CAD",
        "aud" to "AUD",
        "brl" to "BRL",
        "r$" to "BRL",
        "mxn" to "MXN",
        "sgd" to "SGD",
        "hkd" to "HKD",
        "idr" to "IDR",
        "rp" to "IDR",
        "php" to "PHP",
        "usdt" to "USDT",
        "btc" to "BTC",
        "eth" to "ETH"
    )

    // Pre-compiled regexes for zero-allocation per-frame parsing
    private val singleCharSymbols = symbolToCode.filterKeys { it.length == 1 }
    private val multiCharSymbolRegexes = symbolToCode
        .filterKeys { it.length > 1 }
        .map { (symbol, code) ->
            Regex("""(?<!\p{L})${Regex.escape(symbol)}(?!\p{L})""", RegexOption.IGNORE_CASE) to code
        }

    private val priceKeywordRegexes = priceKeywords.map { kw ->
        Regex("""(?<!\p{L})${Regex.escape(kw)}(?!\p{L})""", RegexOption.IGNORE_CASE)
    }

    // Regex for candidate price:
    // Matches numbers with currency, decimals (.99, ,50), or thousands separators (space, comma, or dot)
    private val strictPriceRegex = Regex(
        """(?<!\w)(?:[€$£₽₺₸₴₹¥₩฿₫₾]\s*)?(\d{1,3}(?:[ .,]\d{3})*(?:[.,]\d{1,2}|[.,]-)?|\d+(?:[.,]\d{1,2}|[.,]-)?)(?:\s*[€$£₽₺₸₴₹¥₩฿₫₾a-zA-Zа-яА-Я.]{1,6})?(?!\w)"""
    )

    private val cleanNumberRegex = Regex("""[^0-9., \-]""")
    private val nonDigitRegex = Regex("""\D""")
    private val splitSpaceDecimalRegex = Regex("""^(\d{1,5})\s+(\d{2})$""")
    private val twoDigitKopeckRegex = Regex("""^\s*[.,]?\s*(\d{2})\s*(?:[₽$€£₺₸₴рР].*)?$""")
    private val ocrDecimalDotVariantsRegex = Regex("""(\d)\s*[·•٫]\s*(\d)""")
    private val spacedDecimalRegex = Regex("""(\d)\s+([.,])\s*(\d{1,2})(?!\d)|(\d)([.,])\s+(\d{1,2})(?!\d)""")

    private data class SpatialCandidate(
        val amount: Double,
        val currencyCode: String?,
        val rawText: String,
        val line: OcrLine,
        val hasCurrencyMarker: Boolean,
        val hasPriceKeyword: Boolean,
        val hasDecimal: Boolean,
        val isIntegerOnly: Boolean
    )

    /**
     * Spatial price parser that prioritizes the largest font size (boundingBox.height)
     * and pairs large integer rubles with smaller nearby kopecks/cents.
     */
    fun parseSpatial(lines: List<OcrLine>, defaultCurrencyCode: String? = null): ParsedPrice? {
        if (lines.isEmpty()) return null

        val cleanedLines = lines.mapNotNull { line ->
            val trimmed = line.text.trim()
            if (trimmed.isEmpty()) null else line.copy(text = trimmed)
        }
        if (cleanedLines.isEmpty()) return null

        val rawCandidates = mutableListOf<SpatialCandidate>()
        val kopeckLines = mutableListOf<Pair<OcrLine, Pair<Int, String?>>>()

        for (line in cleanedLines) {
            val text = normalizeOcrText(line.text)
            val (detectedCode, hasCurrencyMarker) = detectCurrency(text)

            // Reject dates, times, percentages, and cent-based unit prices (e.g. 66.6¢)
            if (datePattern.containsMatchIn(text) && !hasCurrencyMarker) continue
            if (timePattern.containsMatchIn(text) && !hasCurrencyMarker) continue
            if (percentPattern.containsMatchIn(text) && !hasCurrencyMarker) continue
            if (centPattern.containsMatchIn(text)) continue

            val hasPriceKeyword = hasPriceKeyword(text)

            // Check if this line could be a standalone 2-digit kopeck/cent block (e.g. "99", ".99", ",99", "99₽")
            val kopMatch = twoDigitKopeckRegex.matchEntire(text)
            if (kopMatch != null) {
                val kopVal = kopMatch.groupValues[1].toIntOrNull()
                if (kopVal != null) {
                    kopeckLines.add(line to (kopVal to detectedCode))
                }
            }

            // Handle "229 99" or "99 90." on a single line (integer + 2-digit kopeck separated by space)
            val spaceDecInput = text.replace(Regex("""[₽$€£]"""), "").trim().trimEnd('.', ',', '-')
            val spaceDecMatch = splitSpaceDecimalRegex.matchEntire(spaceDecInput)
            if (spaceDecMatch != null) {
                val mainPart = spaceDecMatch.groupValues[1].toDoubleOrNull()
                val kopPart = spaceDecMatch.groupValues[2].toDoubleOrNull()
                if (mainPart != null && kopPart != null && mainPart > 0) {
                    rawCandidates.add(
                        SpatialCandidate(
                            amount = mainPart + kopPart / 100.0,
                            currencyCode = detectedCode,
                            rawText = text,
                            line = line,
                            hasCurrencyMarker = hasCurrencyMarker,
                            hasPriceKeyword = hasPriceKeyword,
                            hasDecimal = true,
                            isIntegerOnly = false
                        )
                    )
                    continue
                }
            }

            val matches = strictPriceRegex.findAll(text)
            for (match in matches) {
                val fullMatch = match.value.trim()

                if ((unitPattern.containsMatchIn(fullMatch) || unitPattern.containsMatchIn(text)) &&
                    !hasCurrencyMarker && !hasPriceKeyword
                ) {
                    continue
                }

                val cleanedNumberStr = fullMatch.replace(cleanNumberRegex, "").trim()
                val amount = sanitizeNumber(cleanedNumberStr) ?: continue
                if (amount <= 0.0) continue

                val hasExplicitDecimalDigits = Regex("""[.,]\d+$""").containsMatchIn(cleanedNumberStr)
                val hasDecimal = hasExplicitDecimalDigits || cleanedNumberStr.endsWith("-")

                // Discard years (2020..2030) without currency/decimal
                if (!hasCurrencyMarker && amount in 2020.0..2030.0 && !hasDecimal) {
                    continue
                }

                // Discard barcodes / long numbers (> 6 digits without decimal or currency)
                val digitCount = cleanedNumberStr.replace(nonDigitRegex, "").length
                if (digitCount > 6 && !hasDecimal && !hasCurrencyMarker) {
                    continue
                }
                if (digitCount > 8 && !hasCurrencyMarker) {
                    continue
                }

                rawCandidates.add(
                    SpatialCandidate(
                        amount = amount,
                        currencyCode = detectedCode,
                        rawText = text,
                        line = line,
                        hasCurrencyMarker = hasCurrencyMarker,
                        hasPriceKeyword = hasPriceKeyword,
                        hasDecimal = hasDecimal,
                        isIntegerOnly = !hasDecimal
                    )
                )
            }
        }

        if (rawCandidates.isEmpty()) return null

        // Pair large integer candidates with nearby smaller 2-digit kopeck lines to their right
        val consumedKopeckLines = mutableSetOf<OcrLine>()
        val mergedCandidates = rawCandidates.map { candidate ->
            if (!candidate.isIntegerOnly || candidate.amount >= 100000.0) {
                candidate
            } else {
                val mainBox = candidate.line
                val matchingKop = kopeckLines
                    .filter { (kopLine, _) ->
                        kopLine !== mainBox &&
                            kopLine !in consumedKopeckLines &&
                            kopLine.height <= mainBox.height * 0.85 &&
                            kopLine.left >= mainBox.right - (mainBox.width * 0.25) &&
                            kopLine.left <= mainBox.right + (mainBox.height * 1.4) &&
                            kopLine.top >= mainBox.top - (mainBox.height * 0.35) &&
                            kopLine.bottom <= mainBox.bottom + (mainBox.height * 0.35)
                    }
                    .minByOrNull { (kopLine, _) ->
                        kotlin.math.abs(kopLine.left - mainBox.right)
                    }

                if (matchingKop != null) {
                    val (kopLine, kopInfo) = matchingKop
                    val (kopVal, kopCurrency) = kopInfo
                    consumedKopeckLines.add(kopLine)
                    val combinedCurrency = candidate.currencyCode ?: kopCurrency
                    candidate.copy(
                        amount = candidate.amount + kopVal / 100.0,
                        currencyCode = combinedCurrency,
                        rawText = "${candidate.rawText}.${kopVal.toString().padStart(2, '0')}",
                        hasCurrencyMarker = candidate.hasCurrencyMarker || kopCurrency != null,
                        hasDecimal = true,
                        isIntegerOnly = false
                    )
                } else {
                    candidate
                }
            }
        }

        // Remove standalone kopeck lines that were already merged into a main integer price
        val finalCandidates = mergedCandidates.filter { it.line !in consumedKopeckLines }
        if (finalCandidates.isEmpty()) return null

        val maxHeight = finalCandidates.maxOf { it.line.height }.coerceAtLeast(1)

        // Filter out small-font standalone integers (e.g. "5" in logo, "12" in page number)
        val filtered = finalCandidates.filter { c ->
            if (c.isIntegerOnly && !c.hasCurrencyMarker && !c.hasPriceKeyword) {
                val heightRatio = c.line.height.toFloat() / maxHeight
                if (heightRatio < 0.55f) return@filter false
                if (c.amount < 10.0 && heightRatio < 0.70f) return@filter false
            }
            true
        }
        if (filtered.isEmpty()) return null

        // Score primarily by visual font height; use currency/decimal only to break near-ties
        fun visualScore(c: SpatialCandidate): Float {
            val heightRatio = c.line.height.toFloat() / maxHeight
            var score = heightRatio * 100f
            if (c.hasCurrencyMarker) score += 8f
            if (c.hasDecimal) score += 5f
            if (c.hasPriceKeyword) score += 4f
            return score
        }

        val best = filtered.maxWithOrNull(
            compareBy<SpatialCandidate> { visualScore(it) }
                .thenBy { it.line.height }
        ) ?: return null

        val secondBestHeight = filtered
            .filter { it !== best }
            .maxOfOrNull { it.line.height } ?: 0

        val isClearlyDominant = best.line.height >= 30 &&
            (secondBestHeight == 0 || best.line.height >= secondBestHeight * 1.35f)

        val confidence = if (best.hasCurrencyMarker || best.hasPriceKeyword || isClearlyDominant || best.hasDecimal) {
            6.0f
        } else {
            3.5f
        }

        return ParsedPrice(
            amount = best.amount,
            currencyCode = best.currencyCode,
            rawText = best.rawText,
            confidence = confidence
        )
    }

    /**
     * Parses price from plain text (legacy / unit-test / fallback mode):
     * - Discards ordinary text, dates, times, measurements (100g, 50ml, etc.).
     * - Only accepts numbers with explicit currency marker OR price keywords OR decimal format.
     */
    fun parse(rawText: String, defaultCurrencyCode: String? = null): ParsedPrice? {
        if (rawText.isBlank()) return null

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val candidates = mutableListOf<ParsedPrice>()

        for (rawLine in lines) {
            val line = normalizeOcrText(rawLine)
            val (detectedCode, hasCurrencyMarker) = detectCurrency(line)

            if (datePattern.containsMatchIn(line) && !hasCurrencyMarker) continue
            if (timePattern.containsMatchIn(line) && !hasCurrencyMarker) continue

            val hasPriceKeyword = hasPriceKeyword(line)

            val matches = strictPriceRegex.findAll(line)
            for (match in matches) {
                val fullMatch = match.value.trim()

                if (unitPattern.containsMatchIn(fullMatch) || unitPattern.containsMatchIn(line)) {
                    if (!hasCurrencyMarker && !hasPriceKeyword) continue
                }

                val cleanedNumberStr = fullMatch.replace(cleanNumberRegex, "").trim()
                val amount = sanitizeNumber(cleanedNumberStr) ?: continue
                if (amount <= 0.0) continue

                val hasExplicitDecimalDigits = Regex("""[.,]\d+$""").containsMatchIn(cleanedNumberStr)
                val hasDecimal = hasExplicitDecimalDigits || cleanedNumberStr.endsWith("-")

                if (!hasCurrencyMarker && !hasPriceKeyword && !hasDecimal) {
                    continue
                }

                if (!hasCurrencyMarker && amount in 2020.0..2030.0 && !hasDecimal) {
                    continue
                }

                if (cleanedNumberStr.replace(nonDigitRegex, "").length > 8 && !hasCurrencyMarker) {
                    continue
                }

                if (!hasCurrencyMarker && amount < 10.0 && !hasDecimal) {
                    continue
                }

                var confidence = 1.0f
                if (hasCurrencyMarker) confidence += 4.0f
                if (hasPriceKeyword) confidence += 3.0f
                if (hasDecimal) confidence += 2.0f
                if (line.length < 25) confidence += 1.0f

                candidates.add(
                    ParsedPrice(
                        amount = amount,
                        currencyCode = detectedCode,
                        rawText = line,
                        confidence = confidence
                    )
                )
            }
        }

        return candidates.maxWithOrNull(
            compareBy<ParsedPrice> { it.confidence }
                .thenBy { it.currencyCode != null }
                .thenByDescending { it.amount }
        )
    }

    private fun detectCurrency(line: String): Pair<String?, Boolean> {
        for ((symbol, code) in singleCharSymbols) {
            if (line.contains(symbol)) {
                return code to true
            }
        }
        for ((regex, code) in multiCharSymbolRegexes) {
            if (regex.containsMatchIn(line)) {
                return code to true
            }
        }
        return null to false
    }

    private fun hasPriceKeyword(line: String): Boolean {
        return priceKeywordRegexes.any { it.containsMatchIn(line) }
    }

    /**
     * Maps an OCR bounding box from camera sensor coordinates (frameWidth x frameHeight)
     * into PreviewView FILL_CENTER screen coordinates and checks if its center lies
     * strictly inside the visible viewfinder cutout rectangle.
     */
    fun isInsideScreenCutout(
        box: Rect?,
        frameWidth: Int,
        frameHeight: Int,
        viewWidth: Float,
        viewHeight: Float,
        cutLeft: Float,
        cutTop: Float,
        cutRight: Float,
        cutBottom: Float
    ): Boolean {
        if (box == null || frameWidth <= 0 || frameHeight <= 0 || viewWidth <= 0f || viewHeight <= 0f) {
            return false
        }
        val scale = maxOf(viewWidth / frameWidth.toFloat(), viewHeight / frameHeight.toFloat())
        val offsetX = (viewWidth - frameWidth * scale) / 2f
        val offsetY = (viewHeight - frameHeight * scale) / 2f

        val screenCenterX = box.centerX() * scale + offsetX
        val screenCenterY = box.centerY() * scale + offsetY

        return screenCenterX in cutLeft..cutRight && screenCenterY in cutTop..cutBottom
    }

    /**
     * Checks if a bounding box is within the central viewfinder target region.
     * Normalized coordinates (0..1) of the rotated image frame.
     */
    fun isInsideViewfinder(box: Rect?, imageWidth: Int, imageHeight: Int): Boolean {
        if (box == null || imageWidth <= 0 || imageHeight <= 0) return true
        return isInsideViewfinder(box.left, box.top, box.right, box.bottom, imageWidth, imageHeight)
    }

    fun isInsideViewfinder(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Boolean {
        if (imageWidth <= 0 || imageHeight <= 0) return true

        val minX = (imageWidth * 0.08).toInt()
        val maxX = (imageWidth * 0.92).toInt()
        val minY = (imageHeight * 0.12).toInt()
        val maxY = (imageHeight * 0.72).toInt()

        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        return centerX in minX..maxX && centerY in minY..maxY
    }

    /**
     * Reconstructs missing decimal points in ML Kit symbol runs where the last 2 digits
     * are superscripted/subscripted (smaller height or raised baseline, e.g. Pyaterochka 99^90)
     * or separated by a horizontal gap where a bold/square dot was dropped by OCR.
     */
    fun reconstructSuperscriptDecimals(symbols: List<OcrSymbol>): String {
        if (symbols.isEmpty()) return ""
        val sb = StringBuilder(symbols.size + 4)
        var i = 0
        while (i < symbols.size) {
            val s = symbols[i]
            if (s.text.length == 1 && s.text[0].isDigit()) {
                val runStart = i
                while (i < symbols.size && symbols[i].text.length == 1 && symbols[i].text[0].isDigit()) {
                    i++
                }
                val run = symbols.subList(runStart, i)
                val prevIsSep = runStart > 0 && symbols[runStart - 1].text in setOf(".", ",")
                val nextIsSep = i < symbols.size && symbols[i].text in setOf(".", ",")

                if (!prevIsSep && !nextIsSep && run.size in 4..7) {
                    val splitIdx = run.size - 2
                    val mainDigits = run.subList(0, splitIdx)
                    val lastTwo = run.subList(splitIdx, run.size)

                    val mainAvgH = mainDigits.map { it.height }.average().toFloat().coerceAtLeast(1f)
                    val mainAvgBottom = mainDigits.map { it.bottom }.average().toFloat()
                    val lastAvgH = lastTwo.map { it.height }.average().toFloat()
                    val lastAvgBottom = lastTwo.map { it.bottom }.average().toFloat()

                    val gapBeforeLastTwo = (lastTwo.first().left - mainDigits.last().right).toFloat()
                    val isSmallerSuperscript = lastAvgH <= mainAvgH * 0.75f
                    val isRaisedBaseline = lastAvgBottom <= mainAvgBottom - mainAvgH * 0.20f
                    val hasDroppedDotGap = gapBeforeLastTwo >= mainAvgH * 0.22f && lastAvgH <= mainAvgH * 0.90f

                    if (isSmallerSuperscript || isRaisedBaseline || hasDroppedDotGap) {
                        for (d in mainDigits) sb.append(d.text)
                        sb.append('.')
                        for (d in lastTwo) sb.append(d.text)
                        continue
                    }
                }
                for (d in run) sb.append(d.text)
            } else {
                sb.append(s.text)
                i++
            }
        }
        return sb.toString()
    }

    private fun normalizeOcrText(raw: String): String {
        var s = ocrDecimalDotVariantsRegex.replace(raw) { m -> "${m.groupValues[1]}.${m.groupValues[2]}" }
        s = spacedDecimalRegex.replace(s) { m ->
            if (m.groupValues[1].isNotEmpty()) {
                "${m.groupValues[1]}${m.groupValues[2]}${m.groupValues[3]}"
            } else {
                "${m.groupValues[4]}${m.groupValues[5]}${m.groupValues[6]}"
            }
        }
        return s
    }

    fun sanitizeNumber(raw: String): Double? {
        var str = normalizeOcrText(raw).trim().trimEnd('-', '.', ',')
        if (str.isEmpty()) return null

        try {
            if (str.contains('.') && str.contains(',')) {
                if (str.lastIndexOf('.') > str.lastIndexOf(',')) {
                    str = str.replace(",", "").replace(" ", "")
                } else {
                    str = str.replace(".", "").replace(" ", "").replace(',', '.')
                }
            } else if (str.contains(',')) {
                val commaIndex = str.lastIndexOf(',')
                val intPart = str.substring(0, commaIndex).replace(" ", "")
                val digitsAfter = str.length - 1 - commaIndex
                if (digitsAfter == 3 && !intPart.contains(',') && intPart != "0") {
                    str = str.replace(",", "").replace(" ", "")
                } else {
                    str = str.replace(" ", "").replace(',', '.')
                }
            } else if (str.contains('.')) {
                val dotIndex = str.lastIndexOf('.')
                val intPart = str.substring(0, dotIndex).replace(" ", "")
                val digitsAfter = str.length - 1 - dotIndex
                if (digitsAfter == 3 && !intPart.contains('.') && intPart != "0") {
                    str = str.replace(".", "").replace(" ", "")
                } else {
                    str = str.replace(" ", "")
                }
            } else {
                str = str.replace(" ", "")
            }

            return str.toDoubleOrNull()
        } catch (e: Exception) {
            return null
        }
    }
}
