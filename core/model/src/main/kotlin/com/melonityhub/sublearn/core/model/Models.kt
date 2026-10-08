package com.melonityhub.sublearn.core.model

import kotlinx.serialization.Serializable

/**
 * One timed line of subtitle text. Times are milliseconds from the start of the media.
 * [text] is already normalised (see core:subtitle TextNormalizer): line breaks are '\n'.
 */
data class Cue(
    val id: Long,
    val trackId: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
) {
    init {
        require(endMs >= startMs) { "Cue $id ends before it starts ($startMs..$endMs)" }
    }

    val durationMs: Long get() = endMs - startMs
}

/** A readable unit made of consecutive cues. Used for block navigation, block repeat and AI context. */
data class Block(
    val index: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val cueIds: List<Long>,
) {
    val durationMs: Long get() = endMs - startMs
}

/** A word or punctuation run inside a cue's text. [start] and [end] are char offsets into that text. */
data class Token(val start: Int, val end: Int, val text: String, val isWord: Boolean)

/** What the player is playing: a local content/file URI or a remote http(s) URL. */
data class MediaSource(val uri: String, val title: String) {
    val isRemote: Boolean
        get() = uri.startsWith("http://", ignoreCase = true) || uri.startsWith("https://", ignoreCase = true)
}

/** The two independent subtitle layers (PLY-7). */
enum class LayerRole { LEARNING, TRANSLATION }

enum class SubtitleFormat { SRT, VTT, ASS }

data class SubtitleTrackInfo(
    val id: String,
    val label: String,
    val format: SubtitleFormat?,
    val language: String?,
    val origin: Origin,
) {
    enum class Origin { EXTERNAL, EMBEDDED }
}

/** CEFR levels used by the manual level setting (LRN-2). [rank] is 1 (A1) to 6 (C2). */
enum class CefrLevel(val rank: Int) {
    A1(1), A2(2), B1(3), B2(4), C1(5), C2(6);

    companion object {
        fun fromName(name: String?): CefrLevel? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

/** AI backends (AI-1). Lives in model so settings and the AI module share one definition. */
@Serializable
enum class AiProviderId { GEMINI, OPENAI, ANTHROPIC }
