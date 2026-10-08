package com.melonityhub.sublearn.core.model.wordlevel

import com.melonityhub.sublearn.core.model.CefrLevel

/**
 * Decides whether a word is already known to the learner (LRN-2). Learning-mode popups show only
 * words that are NOT known. Implementations are swappable (ENG-2); the NOW default is
 * [CoreWordLevelProvider]. A frequency- or CEFR-list provider can be added without changing callers.
 */
interface WordLevelProvider {
    fun isKnown(term: String): Boolean
}

/**
 * NOW default (D-020). The default-known set is the authored core list for levels A1 and A2, plus the
 * words the user marked as known. From B1 upward only the user's marks count, because no permissively
 * licensed level list is bundled (see AR-003).
 */
class CoreWordLevelProvider(
    private val manualLevel: CefrLevel,
    private val markedKnown: Set<String>,
) : WordLevelProvider {
    private val normalizedMarks = markedKnown.map { normalize(it) }.toSet()

    override fun isKnown(term: String): Boolean {
        val key = normalize(term)
        if (key in normalizedMarks) return true
        return manualLevel.rank <= CORE_LEVEL_CUTOFF.rank && key in CoreFunctionWords.set
    }

    companion object {
        val CORE_LEVEL_CUTOFF: CefrLevel = CefrLevel.A2

        fun normalize(term: String): String = term.trim().lowercase().replace("\u200C", "")
    }
}
