package com.melonityhub.sublearn.core.model.wordlevel

import com.melonityhub.sublearn.core.model.CefrLevel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordLevelTest {

    @Test
    fun basicLevelsKnowCoreWordsAndUserMarks() {
        val provider = CoreWordLevelProvider(CefrLevel.A2, markedKnown = setOf("Banana"))
        assertTrue(provider.isKnown("the"))
        assertTrue(provider.isKnown("THE"))
        assertTrue(provider.isKnown("banana"))
        assertFalse(provider.isKnown("serendipity"))
    }

    @Test
    fun higherLevelsTrustOnlyUserMarks() {
        val provider = CoreWordLevelProvider(CefrLevel.B1, markedKnown = emptySet())
        assertFalse(provider.isKnown("the"))
        assertTrue(CoreWordLevelProvider(CefrLevel.C1, markedKnown = setOf("the")).isKnown("the"))
    }

    @Test
    fun normalizationIgnoresZwnjAndCase() {
        assertTrue(CoreWordLevelProvider.normalize(" Can\u200Cnot ") == "cannot")
    }
}
