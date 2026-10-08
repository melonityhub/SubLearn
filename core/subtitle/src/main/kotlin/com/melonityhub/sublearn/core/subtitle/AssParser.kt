package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue

/**
 * Text-only parser for ASS/SSA `[Events]` dialogue (ENG-3). Styling, positioning and drawings are
 * ignored: only the readable text is kept, so the cue can be shown in our own overlay.
 */
object AssParser {
    private val defaultFormat = listOf("layer", "start", "end", "style", "name", "marginl", "marginr", "marginv", "effect", "text")
    private val assTime = Regex("""(\d+):(\d{1,2}):(\d{1,2})[.,](\d{1,3})""")
    private val drawing = Regex("""\\p[1-9]""")

    fun parse(text: String, trackId: String): List<Cue> {
        var inEvents = false
        var format: List<String>? = null
        val result = mutableListOf<Cue>()

        for (rawLine in text.replace("\r\n", "\n").removePrefix("\uFEFF").split('\n')) {
            val line = rawLine.trim()
            if (line.startsWith("[")) {
                inEvents = line.equals("[Events]", ignoreCase = true)
                continue
            }
            if (!inEvents) continue
            if (line.startsWith("Format:", ignoreCase = true)) {
                format = line.substringAfter(':').split(',').map { it.trim().lowercase() }
                continue
            }
            if (!line.startsWith("Dialogue:", ignoreCase = true)) continue

            val fields = format ?: defaultFormat
            val values = line.substringAfter(':').trimStart().split(',', limit = fields.size)
            val startIndex = fields.indexOf("start")
            val endIndex = fields.indexOf("end")
            val textIndex = fields.indexOf("text")
            if (startIndex < 0 || endIndex < 0 || textIndex < 0 || values.size < fields.size) continue

            val start = parseTime(values[startIndex]) ?: continue
            val end = parseTime(values[endIndex]) ?: continue
            val rawText = values[textIndex]
            if (drawing.containsMatchIn(rawText)) continue // vector drawings are not text

            val cleaned = TextNormalizer.cleanCueText(
                rawText.replace(Regex("\\{[^}]*\\}"), "")
                    .replace("\\N", "\n")
                    .replace("\\n", " ")
                    .replace("\\h", " "),
            )
            if (cleaned.isEmpty() || end <= start) continue
            result += Cue(id = 0L, trackId = trackId, startMs = start, endMs = end, text = cleaned)
        }
        return TimedTextParser.finalize(result)
    }

    private fun parseTime(value: String): Long? {
        val m = assTime.find(value.trim()) ?: return null
        val g = m.groupValues
        return Timecode.toMillis(g[1], g[2], g[3], g[4])
    }
}
