package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue

/**
 * Parser for SRT and WebVTT. Both are blocks of timing lines and text separated by blank lines;
 * a block without a `-->` timing line (WEBVTT header, NOTE, STYLE, REGION, cue identifiers) yields
 * nothing for that block, and identifier lines before the timing line are ignored.
 */
object TimedTextParser {
    private val timing = Regex(
        """(?:(\d+):)?(\d{1,2}):(\d{1,2})[,.](\d{1,3})\s*-->\s*(?:(\d+):)?(\d{1,2}):(\d{1,2})[,.](\d{1,3})""",
    )

    fun parse(text: String, trackId: String): List<Cue> {
        val lines = text.replace("\r\n", "\n").replace('\r', '\n').removePrefix("\uFEFF").split('\n')
        val result = mutableListOf<Cue>()
        val block = mutableListOf<String>()

        fun flush() {
            parseBlock(block, trackId)?.let { result += it }
            block.clear()
        }

        for (line in lines) {
            if (line.isBlank()) flush() else block += line
        }
        flush()
        return finalize(result)
    }

    private fun parseBlock(lines: List<String>, trackId: String): Cue? {
        val timingIndex = lines.indexOfFirst { timing.containsMatchIn(it) }
        if (timingIndex < 0) return null
        val match = timing.find(lines[timingIndex]) ?: return null
        val g = match.groupValues
        val start = Timecode.toMillis(g[1], g[2], g[3], g[4])
        val end = Timecode.toMillis(g[5], g[6], g[7], g[8])
        val body = lines.drop(timingIndex + 1).joinToString("\n")
        val text = TextNormalizer.cleanCueText(body)
        if (text.isEmpty() || end <= start) return null
        return Cue(id = 0L, trackId = trackId, startMs = start, endMs = end, text = text)
    }

    /** Sorts by start time (stable) and assigns sequential ids. */
    internal fun finalize(cues: List<Cue>): List<Cue> = cues
        .sortedBy { it.startMs }
        .mapIndexed { index, cue -> cue.copy(id = index.toLong()) }
}

/** Shared timecode arithmetic for every supported format. */
internal object Timecode {
    /** [hours] may be null for MM:SS.mmm forms; [fraction] is 1 to 3 digits (tenths, centis or millis). */
    fun toMillis(hours: String?, minutes: String, seconds: String, fraction: String): Long {
        val h = hours?.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
        val ms = fraction.padEnd(3, '0').take(3).toLong()
        return h * 3_600_000L + minutes.toLong() * 60_000L + seconds.toLong() * 1_000L + ms
    }
}
