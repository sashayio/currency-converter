package com.worldprices.converter.ui.utils

data class ExpressionResult(
    val value: Double,
    val isExpression: Boolean
)

/**
 * Safe recursive-descent arithmetic evaluator supporting +, -, *, /, and parentheses.
 */
object AmountExpressionEvaluator {

    fun evaluate(raw: String): ExpressionResult? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ExpressionResult(0.0, false)

        val normalized = trimmed
            .replace(" ", "")
            .replace("\u00A0", "")
            .replace(",", ".")

        val withoutLeadingMinus = if (normalized.startsWith("-")) normalized.drop(1) else normalized
        val hasOperator = withoutLeadingMinus.any {
            it == '+' || it == '-' || it == '*' || it == '/' || it == '(' || it == ')'
        }

        if (!hasOperator) {
            val num = normalized.toDoubleOrNull() ?: return null
            return if (num.isFinite()) ExpressionResult(num.coerceAtLeast(0.0), false) else null
        }

        if (!normalized.all { it in '0'..'9' || it in "+-*/()." }) return null

        val parser = Parser(normalized)
        val result = parser.parseExpr() ?: return null
        if (!parser.isAtEnd() || !result.isFinite()) return null
        return ExpressionResult(result.coerceAtLeast(0.0), true)
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun isAtEnd(): Boolean = pos == input.length

        private fun peek(): Char? = if (pos < input.length) input[pos] else null
        private fun consume(): Char = input[pos++]

        fun parseExpr(): Double? {
            var value = parseTerm() ?: return null
            while (peek() == '+' || peek() == '-') {
                val op = consume()
                val right = parseTerm() ?: return null
                value = if (op == '+') value + right else value - right
            }
            return value
        }

        private fun parseTerm(): Double? {
            var value = parseFactor() ?: return null
            while (peek() == '*' || peek() == '/') {
                val op = consume()
                val right = parseFactor() ?: return null
                if (op == '/') {
                    if (right == 0.0) return null
                    value /= right
                } else {
                    value *= right
                }
            }
            return value
        }

        private fun parseFactor(): Double? {
            if (peek() == '+') {
                consume()
                return parseFactor()
            }
            if (peek() == '-') {
                consume()
                val f = parseFactor() ?: return null
                return -f
            }
            if (peek() == '(') {
                consume()
                val inner = parseExpr() ?: return null
                if (peek() != ')') return null
                consume()
                return inner
            }
            val start = pos
            var dotSeen = false
            while (pos < input.length && (input[pos] in '0'..'9' || input[pos] == '.')) {
                if (input[pos] == '.') {
                    if (dotSeen) return null
                    dotSeen = true
                }
                pos++
            }
            if (pos == start) return null
            return input.substring(start, pos).toDoubleOrNull()
        }
    }
}
