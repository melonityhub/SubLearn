package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Token
import java.text.BreakIterator
import java.util.Locale

/**
 * Word tokenisation with the platform word-break rules (UAX #29 via BreakIterator). Offsets are
 * char offsets in logical order, which is what Compose text layout reports for taps, so this stays
 * correct for RTL/bidi text (D-014).
 */
object Tokenizer {
    fun tokenize(text: String, locale: Locale = Locale.ENGLISH): List<Token> {
        if (text.isEmpty()) return emptyList()
        val iterator = BreakIterator.getWordInstance(locale)
        iterator.setText(text)
        val tokens = ArrayList<Token>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            val piece = text.substring(start, end)
            if (piece.isNotEmpty()) {
                tokens += Token(start, end, piece, isWord = piece.any { it.isLetterOrDigit() })
            }
            start = end
            end = iterator.next()
        }
        return tokens
    }

    /** The word token covering [offset], or null when the offset is on spaces or punctuation. */
    fun wordAt(tokens: List<Token>, offset: Int): Token? =
        tokens.firstOrNull { it.isWord && offset >= it.start && offset < it.end }

    /** The word tokens from [fromOffset] to [toOffset] (inclusive of partially covered tokens). */
    fun wordsInRange(tokens: List<Token>, fromOffset: Int, toOffset: Int): List<Token> {
        val lo = minOf(fromOffset, toOffset)
        val hi = maxOf(fromOffset, toOffset)
        return tokens.filter { it.isWord && it.end > lo && it.start <= hi }
    }
}
