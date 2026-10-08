package com.melonityhub.sublearn.core.subtitle

/**
 * Pure text rules for subtitles (ENG-3, D-016). Nothing here touches Android APIs.
 *
 * - ZWNJ (U+200C) and ZWJ (U+200D) are kept: Persian orthography depends on them.
 * - Bidi embedding/override controls are removed; direction is applied per text run in the UI.
 * - Non-breaking spaces become normal spaces.
 */
object TextNormalizer {
    private val markupTag = Regex("<[^>]*>")
    private val assOverride = Regex("\\{[^}]*\\}")
    private val multiSpace = Regex("[ \\t]+")
    private val spaceBeforePunctuation = Regex("\\s+([,.;:!?)\\]\u060C\u061B\u061F])")
    private val noSpaceAfterPunctuation = Regex("([,;!?\u060C\u061B\u061F])(?=\\p{L})")
    private val sentenceEnd = Regex("[.!?\u2026\u061F][\"'\u201D\u2019)\\]]*$")
    private val bidiControls = Regex("[\u200E\u200F\u202A-\u202E\u2066-\u2069\u00AD\u200B\uFEFF]")

    /** Removes HTML-like tags (`<i>`, `<c.color>`, `<v Name>`, VTT timestamps) and ASS override blocks. */
    fun stripMarkup(raw: String): String = raw.replace(assOverride, "").replace(markupTag, "")

    /** Removes invisible/bidi control characters while keeping ZWNJ and ZWJ; maps NBSP to a space. */
    fun sanitizeInvisible(text: String): String = text
        .replace('\u00A0', ' ')
        .replace('\u202F', ' ')
        .replace(bidiControls, "")

    /** Trims a single line, collapses runs of spaces and removes spaces before punctuation. */
    fun normalizeLine(line: String): String {
        val collapsed = multiSpace.replace(sanitizeInvisible(line), " ").trim()
        return spaceBeforePunctuation.replace(collapsed, "$1")
    }

    /**
     * Full cue cleanup: strip markup, normalise each line, drop empty lines and keep the rest as
     * '\n'-separated lines. Joining mid-sentence lines is a user action (see [joinLines]).
     */
    fun cleanCueText(raw: String): String = stripMarkup(raw)
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .map { normalizeLine(it) }
        .filter { it.isNotEmpty() }
        .joinToString("\n")

    /** Batch tool (SUB-6): replaces line breaks inside a cue with a single space. */
    fun joinLines(text: String): String = text
        .split('\n')
        .map { normalizeLine(it) }
        .filter { it.isNotEmpty() }
        .joinToString(" ")

    /** Adds a space after punctuation that is glued to the next letter, e.g. "wait,what" → "wait, what". */
    fun spaceAfterPunctuation(text: String): String = noSpaceAfterPunctuation.replace(text, "$1 ")

    /** True when the text ends a sentence (., !, ?, ellipsis or Arabic/Persian question mark, optionally quoted). */
    fun endsSentence(text: String): Boolean = sentenceEnd.containsMatchIn(text.trimEnd())

    /**
     * Folds text for search (PLY-6): case-insensitive, Arabic Yeh/Kaf mapped to Persian forms,
     * and invisible characters removed, so "كتاب" matches "کتاب".
     */
    fun foldForSearch(text: String): String = sanitizeInvisible(text)
        .lowercase()
        .replace('\u064A', '\u06CC')
        .replace('\u0643', '\u06A9')
        .replace("\u200C", "")
        .trim()
}
