package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue

/**
 * Batch tool (SUB-6): splits a cue whose text is longer than [maxChars] at word boundaries.
 * Time is shared between the parts in proportion to their character counts. A single word that is
 * longer than [maxChars] is kept whole rather than broken. Ids are taken from [nextId].
 */
object CueSplitter {
    fun split(cue: Cue, maxChars: Int, nextId: () -> Long): List<Cue> {
        require(maxChars >= 8) { "maxChars must be at least 8" }
        if (cue.text.length <= maxChars) return listOf(cue)

        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (word in cue.text.split(Regex("\\s+")).filter { it.isNotEmpty() }) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (candidate.length <= maxChars || current.isEmpty()) {
                current.clear().append(candidate)
            } else {
                chunks += current.toString()
                current.clear().append(word)
            }
        }
        if (current.isNotEmpty()) chunks += current.toString()
        if (chunks.size <= 1) return listOf(cue)

        val totalChars = chunks.sumOf { it.length }.toDouble()
        val duration = cue.durationMs.toDouble()
        var elapsed = 0.0
        return chunks.mapIndexed { index, chunk ->
            val start = cue.startMs + (elapsed / totalChars * duration).toLong()
            elapsed += chunk.length
            val end = if (index == chunks.lastIndex) cue.endMs else cue.startMs + (elapsed / totalChars * duration).toLong()
            Cue(id = nextId(), trackId = cue.trackId, startMs = start, endMs = end, text = chunk)
        }
    }
}
