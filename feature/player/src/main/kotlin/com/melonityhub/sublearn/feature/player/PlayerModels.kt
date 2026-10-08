package com.melonityhub.sublearn.feature.player

import com.melonityhub.sublearn.core.model.Block
import com.melonityhub.sublearn.core.model.Cue
import com.melonityhub.sublearn.core.subtitle.CueIndex

/** One loaded subtitle track for a layer: cues, time index and the merged blocks. */
class SubtitleTrack(
    val name: String,
    val cues: List<Cue>,
    val blocks: List<Block>,
) {
    val index: CueIndex = CueIndex(cues)

    /** Block containing [timeMs] or the latest block that started before it (binary search, O(log n)). */
    fun blockAt(timeMs: Long): Block? {
        var lo = 0
        var hi = blocks.size - 1
        var found = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (blocks[mid].startMs <= timeMs) {
                found = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return blocks.getOrNull(found)
    }
}

/** What one layer shows right now. */
data class SubtitleLine(
    val cueId: Long,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val blockStartMs: Long,
    val blockEndMs: Long,
    val blockText: String,
)

data class TranslationPopup(
    val source: String,
    val translation: String?,
    val loading: Boolean,
    val error: String?,
    val pausedPlayback: Boolean,
)

data class OverlayState(
    val popup: TranslationPopup? = null,
    val repeatActive: Boolean = false,
    val subtitleListOpen: Boolean = false,
    val subtitleSheetOpen: Boolean = false,
    /** True while a subtitle-button hold temporarily flips visibility (SUB-1). */
    val peek: Boolean = false,
    val stopAtBlockEnd: Boolean = false,
    val message: String? = null,
)

data class PlayerUi(
    val title: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasMedia: Boolean = false,
    val errorMessage: String? = null,
    val learningLine: SubtitleLine? = null,
    val translationLine: SubtitleLine? = null,
    val learningTrackName: String? = null,
    val translationTrackName: String? = null,
    val embeddedText: String = "",
    val learningVisible: Boolean = true,
    val translationVisible: Boolean = true,
    val popup: TranslationPopup? = null,
    val repeatActive: Boolean = false,
    val subtitleListOpen: Boolean = false,
    val subtitleSheetOpen: Boolean = false,
    val stopAtBlockEnd: Boolean = false,
    val message: String? = null,
    val cues: List<Cue> = emptyList(),
    val blocks: List<Block> = emptyList(),
)
