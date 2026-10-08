package com.melonityhub.sublearn.core.model.formula

/** Thrown for malformed formulas, unknown names or non-finite results. */
class FormulaException(message: String) : IllegalArgumentException(message)

/**
 * A small, safe arithmetic evaluator (no reflection, no scripting engine).
 *
 * Supported: numbers, `+ - * / %`, unary `+`/`-`, parentheses, variables, and the functions
 * `min(a, b, ...)`, `max(a, b, ...)` and `clamp(value, low, high)`.
 */
object Formula {
    const val MAX_LENGTH = 200

    fun evaluate(expression: String, variables: Map<String, Double>): Double {
        if (expression.isBlank()) throw FormulaException("Formula is empty")
        if (expression.length > MAX_LENGTH) throw FormulaException("Formula is longer than $MAX_LENGTH characters")
        val value = Parser(expression, variables).parseAll()
        if (!value.isFinite()) throw FormulaException("Formula result is not a finite number")
        return value
    }

    private class Parser(private val src: String, private val vars: Map<String, Double>) {
        private var pos = 0

        fun parseAll(): Double {
            val value = parseExpr()
            skipWhitespace()
            if (pos != src.length) fail("Unexpected '${src[pos]}' at position $pos")
            return value
        }

        private fun parseExpr(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                when (peek()) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parseUnary()
            while (true) {
                skipWhitespace()
                when (peek()) {
                    '*' -> { pos++; value *= parseUnary() }
                    '/' -> { pos++; value /= nonZero(parseUnary()) }
                    '%' -> { pos++; value %= nonZero(parseUnary()) }
                    else -> return value
                }
            }
        }

        private fun parseUnary(): Double {
            skipWhitespace()
            return when (peek()) {
                '-' -> { pos++; -parseUnary() }
                '+' -> { pos++; parseUnary() }
                else -> parsePrimary()
            }
        }

        private fun parsePrimary(): Double {
            skipWhitespace()
            val c = peek() ?: fail("Unexpected end of formula")
            return when {
                c == '(' -> {
                    pos++
                    val value = parseExpr()
                    expect(')')
                    value
                }
                c.isDigit() || c == '.' -> parseNumber()
                c.isLetter() || c == '_' -> parseName()
                else -> fail("Unexpected '$c' at position $pos")
            }
        }

        private fun parseNumber(): Double {
            val start = pos
            while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) pos++
            return src.substring(start, pos).toDoubleOrNull() ?: fail("Invalid number '${src.substring(start, pos)}'")
        }

        private fun parseName(): Double {
            val start = pos
            while (pos < src.length && (src[pos].isLetterOrDigit() || src[pos] == '_')) pos++
            val name = src.substring(start, pos)
            skipWhitespace()
            if (peek() != '(') {
                return vars[name] ?: fail("Unknown variable '$name'")
            }
            pos++
            val args = mutableListOf(parseExpr())
            skipWhitespace()
            while (peek() == ',') {
                pos++
                args += parseExpr()
                skipWhitespace()
            }
            expect(')')
            return when (name.lowercase()) {
                "min" -> args.reduce { a, b -> minOf(a, b) }
                "max" -> args.reduce { a, b -> maxOf(a, b) }
                "clamp" -> {
                    if (args.size != 3) fail("clamp() takes 3 arguments")
                    if (args[1] > args[2]) fail("clamp() low bound is above high bound")
                    args[0].coerceIn(args[1], args[2])
                }
                else -> fail("Unknown function '$name'")
            }
        }

        private fun nonZero(value: Double): Double {
            if (value == 0.0) fail("Division by zero")
            return value
        }

        private fun expect(c: Char) {
            skipWhitespace()
            if (peek() != c) fail("Expected '$c' at position $pos")
            pos++
        }

        private fun peek(): Char? = if (pos < src.length) src[pos] else null

        private fun skipWhitespace() {
            while (pos < src.length && src[pos].isWhitespace()) pos++
        }

        private fun fail(message: String): Nothing = throw FormulaException(message)
    }
}
