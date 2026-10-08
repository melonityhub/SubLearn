package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue
import com.melonityhub.sublearn.core.model.SubtitleFormat

/** Format detection and dispatch. The public entry point for all subtitle text. */
object SubtitleParsers {

    /** Detects the format from the content first, then from the file name extension. */
    fun detectFormat(text: String, fileName: String? = null): SubtitleFormat? {
        val head = text.removePrefix("\uFEFF").trimStart()
        return when {
            head.startsWith("WEBVTT") -> SubtitleFormat.VTT
            head.contains("[Events]", ignoreCase = true) || head.contains("Dialogue:", ignoreCase = true) -> SubtitleFormat.ASS
            head.contains("-->") -> SubtitleFormat.SRT
            else -> formatFromExtension(fileName)
        }
    }

    fun parse(text: String, trackId: String, format: SubtitleFormat): List<Cue> = when (format) {
        SubtitleFormat.SRT, SubtitleFormat.VTT -> TimedTextParser.parse(text, trackId)
        SubtitleFormat.ASS -> AssParser.parse(text, trackId)
    }

    /** Returns null when the format cannot be determined, so the caller can show an error. */
    fun parseAuto(text: String, trackId: String, fileName: String? = null): Pair<SubtitleFormat, List<Cue>>? {
        val format = detectFormat(text, fileName) ?: return null
        return format to parse(text, trackId, format)
    }

    private fun formatFromExtension(fileName: String?): SubtitleFormat? = when (fileName?.substringAfterLast('.', "")?.lowercase()) {
        "srt" -> SubtitleFormat.SRT
        "vtt" -> SubtitleFormat.VTT
        "ass", "ssa" -> SubtitleFormat.ASS
        else -> null
    }
}
