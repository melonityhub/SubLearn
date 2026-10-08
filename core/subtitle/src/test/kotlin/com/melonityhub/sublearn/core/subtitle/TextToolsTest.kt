package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Cue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextToolsTest {

    @Test
    fun normalizer_stripsMarkupAndCollapsesSpaces() {
        assertEquals("Hi there, friend", TextNormalizer.cleanCueText("<b>Hi</b>   there ,  friend"))
        assertEquals("a\nb", TextNormalizer.cleanCueText("  a  \n\n b  "))
        assertEquals("ok", TextNormalizer.cleanCueText("{\\an8}ok"))
    }

    @Test
    fun normalizer_keepsPersianZwnjAndRemovesBidiControls() {
        val persian = "می\u200Cخواهم"
        assertEquals(persian, TextNormalizer.sanitizeInvisible("\u200F$persian\u202E"))
        assertTrue(TextNormalizer.sanitizeInvisible(persian).contains('\u200C'))
    }

    @Test
    fun normalizer_removesSpaceBeforePersianPunctuation() {
        assertEquals("سلام؟", TextNormalizer.normalizeLine("سلام ؟"))
        assertEquals("سلام، دنیا", TextNormalizer.normalizeLine("سلام،  دنیا"))
    }

    @Test
    fun normalizer_joinLinesAndSpaceAfterPunctuation() {
        assertEquals("one two three", TextNormalizer.joinLines("one\ntwo  \n three"))
        assertEquals("wait, what", TextNormalizer.spaceAfterPunctuation("wait,what"))
        assertEquals("3.14", TextNormalizer.spaceAfterPunctuation("3.14"))
    }

    @Test
    fun endsSentence_recognisesTerminalPunctuation() {
        assertTrue(TextNormalizer.endsSentence("Hello."))
        assertTrue(TextNormalizer.endsSentence("Really?!"))
        assertTrue(TextNormalizer.endsSentence("He said \"yes.\""))
        assertTrue(TextNormalizer.endsSentence("سلام؟"))
        assertFalse(TextNormalizer.endsSentence("Hello"))
    }

    @Test
    fun search_isCaseInsensitiveAndMatchesPersianVariants() {
        assertEquals(TextNormalizer.foldForSearch("كتاب"), TextNormalizer.foldForSearch("کتاب"))
        assertEquals(TextNormalizer.foldForSearch("میخواهم"), TextNormalizer.foldForSearch("می\u200Cخواهم"))
        val cues = listOf(cue(0, "I want the BOOK"), cue(1, "Nothing here"))
        assertEquals(listOf(0L), SubtitleTools.search(cues, "book").map { it.id })
        assertTrue(SubtitleTools.search(cues, "   ").isEmpty())
    }

    @Test
    fun tools_removeLineBreaksKeepsTimes() {
        val out = SubtitleTools.removeLineBreaks(listOf(cue(0, "first\nsecond", 100, 900)))
        assertEquals("first second", out[0].text)
        assertEquals(100L, out[0].startMs)
        assertEquals(900L, out[0].endMs)
    }

    @Test
    fun tools_applyDelayShiftsAndDropsCuesEndingBeforeZero() {
        val shifted = SubtitleTools.applyDelay(listOf(cue(0, "a", 1_000, 2_000), cue(1, "b", 0, 100)), -500L)
        assertEquals(listOf("a"), shifted.map { it.text })
        assertEquals(500L, shifted[0].startMs)
        assertEquals(1_500L, shifted[0].endMs)
        val later = SubtitleTools.applyDelay(listOf(cue(0, "a", 1_000, 2_000)), 250L)
        assertEquals(1_250L, later[0].startMs)
    }

    @Test
    fun tools_limitCharactersSplitsAndRenumbers() {
        val out = SubtitleTools.limitCharacters(listOf(cue(0, "alpha beta gamma delta", 0, 4_000)), maxChars = 11)
        assertEquals(listOf("alpha beta", "gamma delta"), out.map { it.text })
        assertEquals(listOf(0L, 1L), out.map { it.id })
        assertEquals(4_000L, out.last().endMs)
    }

    private fun cue(id: Long, text: String, start: Long = 0L, end: Long = 1_000L) =
        Cue(id = id, trackId = "t", startMs = start, endMs = end, text = text)
}
