package com.melonityhub.sublearn.core.model.shadowing

import com.melonityhub.sublearn.core.model.formula.Formula
import com.melonityhub.sublearn.core.model.formula.FormulaException
import kotlin.math.roundToLong

/**
 * Pause between shadowing repeats (SHD-2).
 *
 * Variables available to the formula:
 * - `D` = block duration in milliseconds
 * - `M` = the adjustable multiplier from settings
 * - `N` = the repeat number, starting at 1
 *
 * The default formula `M * D` means "pause for M times the block length".
 */
object ShadowingFormula {
    const val DEFAULT_FORMULA = "M * D"
    const val MAX_PAUSE_MS = 60_000L

    fun pauseMs(
        formula: String,
        blockDurationMs: Long,
        multiplier: Double,
        repeatNumber: Int,
        minPauseMs: Long = 0L,
        maxPauseMs: Long = MAX_PAUSE_MS,
    ): Long {
        val raw = Formula.evaluate(
            formula,
            mapOf(
                "D" to blockDurationMs.toDouble(),
                "M" to multiplier,
                "N" to repeatNumber.toDouble(),
            ),
        )
        return raw.roundToLong().coerceIn(minPauseMs, maxPauseMs.coerceAtLeast(minPauseMs))
    }

    /** Returns null when [formula] is usable, otherwise a message for the settings UI. */
    fun validate(formula: String): String? = try {
        pauseMs(formula, blockDurationMs = 1_000L, multiplier = 1.0, repeatNumber = 1)
        null
    } catch (e: FormulaException) {
        e.message ?: "Invalid formula"
    }
}
