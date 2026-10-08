package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue

/** Batch and search tools for the subtitle list and quick actions (SUB-6, PLY-6). */
object SubtitleTools {

    /** Removes line breaks inside each cue (SUB-6 batch tool). Ids and times are kept. */
    fun removeLineBreaks(cues: List<Cue>): List<Cue> = cues.map { cue ->
        if (cue.text.contains('\n')) cue.copy(text = TextNormalizer.joinLines(cue.text)) else cue
    }

    /** Splits every cue longer than [maxChars] (SUB-6 batch tool) and re-numbers ids in order. */
    fun limitCharacters(cues: List<Cue>, maxChars: Int): List<Cue> {
        var nextId = 0L
        val out = ArrayList<Cue>(cues.size)
        for (cue in cues.sortedBy { it.startMs }) {
            out += CueSplitter.split(cue, maxChars) { nextId++ }
        }
        return out
    }

    /** Shifts every cue by [delayMs] (per-layer delay). Cues that end before zero are dropped. */
    fun applyDelay(cues: List<Cue>, delayMs: Long): List<Cue> {
        if (delayMs == 0L) return cues
        return cues.mapNotNull { cue ->
            val end = cue.endMs + delayMs
            if (end <= 0L) null else cue.copy(startMs = maxOf(0L, cue.startMs + delayMs), endMs = end)
        }
    }

    /** Case- and Persian-variant-insensitive search over cue text (PLY-6). Blank query matches nothing. */
    fun search(cues: List<Cue>, query: String): List<Cue> {
        val needle = TextNormalizer.foldForSearch(query)
        if (needle.isEmpty()) return emptyList()
        return cues.filter { TextNormalizer.foldForSearch(it.text).contains(needle) }
    }
}
