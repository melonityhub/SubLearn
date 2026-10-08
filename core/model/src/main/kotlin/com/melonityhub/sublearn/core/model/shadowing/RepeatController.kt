package com.melonityhub.sublearn.core.model.shadowing

/** What the player should do after a block finished during repeat mode. */
sealed interface RepeatStep {
    /** Seek back to the block start and keep playing now. */
    data object Replay : RepeatStep

    /** Pause for [pauseMs], then seek back to the block start and play. */
    data class PauseThenReplay(val pauseMs: Long) : RepeatStep

    /** No repeats left: continue normal playback. */
    data object Finish : RepeatStep
}

/**
 * Pure state machine for SHD-1/SHD-2. The player calls [onBlockFinished] whenever a block ends.
 * A `repeatCount` of [AUTO_REPEAT] keeps repeating until the user stops it (auto-repeat, SHD-1).
 */
class RepeatController(
    private val repeatCount: Int,
    private val formula: String = ShadowingFormula.DEFAULT_FORMULA,
    private val multiplier: Double = 1.0,
    private val minPauseMs: Long = 0L,
    private val maxPauseMs: Long = ShadowingFormula.MAX_PAUSE_MS,
) {
    private var completedRepeats = 0

    init {
        require(repeatCount >= 0 || repeatCount == AUTO_REPEAT) { "repeatCount must be >= 0 or AUTO_REPEAT" }
    }

    fun reset() {
        completedRepeats = 0
    }

    fun onBlockFinished(blockDurationMs: Long): RepeatStep {
        if (repeatCount != AUTO_REPEAT && completedRepeats >= repeatCount) return RepeatStep.Finish
        completedRepeats++
        val pause = ShadowingFormula.pauseMs(
            formula = formula,
            blockDurationMs = blockDurationMs,
            multiplier = multiplier,
            repeatNumber = completedRepeats,
            minPauseMs = minPauseMs,
            maxPauseMs = maxPauseMs,
        )
        return if (pause <= 0L) RepeatStep.Replay else RepeatStep.PauseThenReplay(pause)
    }

    companion object {
        const val AUTO_REPEAT = -1
    }
}
