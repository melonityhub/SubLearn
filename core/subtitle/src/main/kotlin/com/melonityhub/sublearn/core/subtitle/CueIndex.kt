package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue

/**
 * Time-indexed cue lookup (ENG-9): binary search over start times plus a prefix maximum of end
 * times, so overlapping cues are handled exactly and a lookup costs O(log n + overlaps).
 */
class CueIndex(cues: List<Cue>) {
    private val sorted: List<Cue> = cues.sortedBy { it.startMs }
    private val prefixMaxEnd: LongArray = LongArray(sorted.size).also { maxEnd ->
        var running = Long.MIN_VALUE
        sorted.forEachIndexed { i, cue ->
            running = maxOf(running, cue.endMs)
            maxEnd[i] = running
        }
    }

    val size: Int get() = sorted.size

    /** The cue shown at [positionMs] (start inclusive, end exclusive); the latest-starting one wins. */
    fun activeAt(positionMs: Long): Cue? {
        val found = lastIndexStartingAtOrBefore(positionMs)
        var i = found
        while (i >= 0 && prefixMaxEnd[i] > positionMs) {
            if (sorted[i].endMs > positionMs) return sorted[i]
            i--
        }
        return null
    }

    /** The first cue that starts strictly after [positionMs], or null. */
    fun nextAfter(positionMs: Long): Cue? {
        val i = lastIndexStartingAtOrBefore(positionMs) + 1
        return sorted.getOrNull(i)
    }

    /** The latest cue that started strictly before [positionMs] minus [graceMs], or null. */
    fun previousBefore(positionMs: Long, graceMs: Long = 0L): Cue? {
        val i = lastIndexStartingAtOrBefore(positionMs - graceMs - 1)
        return sorted.getOrNull(i)
    }

    private fun lastIndexStartingAtOrBefore(positionMs: Long): Int {
        var lo = 0
        var hi = sorted.size - 1
        var found = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (sorted[mid].startMs <= positionMs) {
                found = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return found
    }
}
