package com.melonityhub.sublearn.core.model.shadowing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ShadowingTest {

    @Test
    fun defaultFormulaPausesForMultiplierTimesBlockDuration() {
        assertEquals(2_000L, ShadowingFormula.pauseMs(ShadowingFormula.DEFAULT_FORMULA, 2_000L, 1.0, 1))
        assertEquals(1_000L, ShadowingFormula.pauseMs(ShadowingFormula.DEFAULT_FORMULA, 2_000L, 0.5, 1))
    }

    @Test
    fun repeatNumberAndClampsApply() {
        assertEquals(6_000L, ShadowingFormula.pauseMs("D * N", 2_000L, 1.0, 3))
        assertEquals(4_000L, ShadowingFormula.pauseMs("D * N", 2_000L, 1.0, 3, maxPauseMs = 4_000L))
        assertEquals(500L, ShadowingFormula.pauseMs("M * D", 2_000L, 0.25, 1, minPauseMs = 500L))
    }

    @Test
    fun validateReturnsMessageOnlyForBadFormulas() {
        assertNull(ShadowingFormula.validate("M * D"))
        assertNotNull(ShadowingFormula.validate("M *"))
        assertNotNull(ShadowingFormula.validate("Q * D"))
    }

    @Test
    fun finiteRepeatsPauseThenFinish() {
        val controller = RepeatController(repeatCount = 2, multiplier = 1.0)
        assertEquals(RepeatStep.PauseThenReplay(1_000L), controller.onBlockFinished(1_000L))
        assertEquals(RepeatStep.PauseThenReplay(1_000L), controller.onBlockFinished(1_000L))
        assertEquals(RepeatStep.Finish, controller.onBlockFinished(1_000L))
    }

    @Test
    fun zeroPauseReplaysImmediatelyAndResetStartsOver() {
        val controller = RepeatController(repeatCount = 1, multiplier = 0.0)
        assertEquals(RepeatStep.Replay, controller.onBlockFinished(1_000L))
        assertEquals(RepeatStep.Finish, controller.onBlockFinished(1_000L))
        controller.reset()
        assertEquals(RepeatStep.Replay, controller.onBlockFinished(1_000L))
    }

    @Test
    fun autoRepeatNeverFinishes() {
        val controller = RepeatController(repeatCount = RepeatController.AUTO_REPEAT, multiplier = 0.0)
        repeat(50) { assertEquals(RepeatStep.Replay, controller.onBlockFinished(1_000L)) }
    }
}
