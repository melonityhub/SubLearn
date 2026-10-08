package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue
import com.melonityhub.sublearn.core.model.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlocksAndIndexTest {

    @Test
    fun blockBuilder_mergesFragmentsAndBreaksAtSentenceEnds() {
        val cues = listOf(
            cue(0, "Hello there", 0, 1_000),
            cue(1, "my friend.", 1_100, 2_000),
            cue(2, "How are you?", 2_050, 3_000),
            cue(3, "Far away", 9_000, 9_500),
        )
        val blocks = BlockBuilder.build(cues)
        assertEquals(listOf("Hello there my friend.", "How are you?", "Far away"), blocks.map { it.text })
        assertEquals(listOf(0L, 1L), blocks[0].cueIds)
        assertEquals(2_000L, blocks[0].endMs)
        assertEquals(listOf(0, 1, 2), blocks.map { it.index })
    }

    @Test
    fun blockBuilder_respectsMaxDuration() {
        val cues = (0 until 10).map { i -> cue(i.toLong(), "word$i", i * 1_500L, i * 1_500L + 1_000L) }
        val blocks = BlockBuilder.build(cues, BlockOptions(maxGapMs = 10_000, maxDurationMs = 6_000, maxChars = 500))
        assertTrue(blocks.size > 1)
        assertTrue(blocks.all { it.durationMs <= 6_000L })
        assertEquals(10, blocks.sumOf { it.cueIds.size })
    }

    @Test
    fun blockBuilder_respectsMaxCharacters() {
        val long = "x".repeat(50)
        val blocks = BlockBuilder.build(
            listOf(cue(0, long, 0, 500), cue(1, long, 600, 1_100), cue(2, long, 1_200, 1_700)),
            BlockOptions(maxChars = 96),
        )
        assertEquals(3, blocks.size)
    }

    @Test
    fun cueSplitter_splitsAtWordsAndSharesTimeByLength() {
        val parts = CueSplitter.split(cue(0, "one two three four five six", 0, 2_500), maxChars = 12) { 7L }
        assertEquals(listOf("one two", "three four", "five six"), parts.map { it.text })
        assertEquals(listOf(0L, 700L, 1_700L), parts.map { it.startMs })
        assertEquals(listOf(700L, 1_700L, 2_500L), parts.map { it.endMs })
    }

    @Test
    fun cueSplitter_keepsOverlongWordWhole() {
        val parts = CueSplitter.split(cue(0, "supercalifragilistic word", 0, 1_000), maxChars = 10) { 1L }
        assertEquals(listOf("supercalifragilistic", "word"), parts.map { it.text })
    }

    @Test
    fun cueIndex_findsLatestStartingOverlapAndHandlesLongCues() {
        val index = CueIndex(
            listOf(
                cue(0, "a", 0, 1_000),
                cue(1, "b", 500, 2_000),
                cue(2, "c", 3_000, 4_000),
                cue(3, "long", 0, 100_000),
            ),
        )
        assertEquals("b", index.activeAt(600)?.text)
        assertEquals("long", index.activeAt(2_500)?.text)
        assertEquals("c", index.activeAt(3_500)?.text)
        assertNull(CueIndex(listOf(cue(0, "x", 0, 10))).activeAt(50))
        assertNull(CueIndex(emptyList()).activeAt(0))
        assertEquals(2L, index.nextAfter(1_500)?.id)
        assertEquals("b", index.previousBefore(3_000)?.text)
    }

    @Test
    fun cueIndex_stillFindsLongCueAfterManyShortOnes() {
        val cues = mutableListOf(cue(0, "long", 0, 1_000_000))
        for (i in 1..500) cues += cue(i.toLong(), "s$i", i * 1_000L, i * 1_000L + 500)
        val index = CueIndex(cues)
        assertEquals("long", index.activeAt(250_750)?.text)
        assertEquals("s250", index.activeAt(250_100)?.text)
    }

    @Test
    fun tokenizer_findsWordsAndOffsets() {
        val text = "Don't stop, Ali."
        val tokens = Tokenizer.tokenize(text)
        assertEquals(listOf("Don't", "stop", "Ali"), tokens.filter { it.isWord }.map { it.text })
        val hit: Token? = Tokenizer.wordAt(tokens, text.indexOf("stop") + 1)
        assertEquals("stop", hit?.text)
        assertNull(Tokenizer.wordAt(tokens, text.indexOf(",")))
    }

    @Test
    fun tokenizer_persianWordsAreSplitOnSpaces() {
        val tokens = Tokenizer.tokenize("سلام دنیا")
        assertEquals(listOf("سلام", "دنیا"), tokens.filter { it.isWord }.map { it.text })
    }

    private fun cue(id: Long, text: String, start: Long, end: Long) = Cue(id, "t", start, end, text)
}
