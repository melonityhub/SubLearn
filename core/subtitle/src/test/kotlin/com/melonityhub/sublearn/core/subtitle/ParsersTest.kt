package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.SubtitleFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParsersTest {

    @Test
    fun srt_parsesTimesTagsAndMultilineText() {
        val srt = """
            1
            00:00:01,000 --> 00:00:03,500
            Hello <i>world</i>

            2
            00:00:04,000 --> 00:00:05,000
            Line one
            line two
        """.trimIndent()
        val cues = TimedTextParser.parse(srt, "t1")
        assertEquals(2, cues.size)
        assertEquals(1_000L, cues[0].startMs)
        assertEquals(3_500L, cues[0].endMs)
        assertEquals("Hello world", cues[0].text)
        assertEquals("Line one\nline two", cues[1].text)
        assertEquals(listOf(0L, 1L), cues.map { it.id })
        assertEquals("t1", cues[0].trackId)
    }

    @Test
    fun srt_handlesBomCrlfAndSkipsEmptyCues() {
        val srt = "\uFEFF1\r\n00:00:01,000 --> 00:00:02,000\r\n<font color=red></font>\r\n\r\n" +
            "2\r\n00:00:03,000 --> 00:00:04,000\r\nKept\r\n"
        val cues = TimedTextParser.parse(srt, "t")
        assertEquals(listOf("Kept"), cues.map { it.text })
        assertEquals(3_000L, cues[0].startMs)
    }

    @Test
    fun vtt_skipsHeaderNotesAndIdentifiersAndReadsMinuteTimes() {
        val vtt = """
            WEBVTT

            NOTE this is a comment

            intro
            00:01.000 --> 00:02.000 align:start
            <v Narrator>Welcome</v>

            00:03:00.500 --> 00:03:01.250
            <c.yellow>Second</c>
        """.trimIndent()
        val cues = TimedTextParser.parse(vtt, "v")
        assertEquals(listOf("Welcome", "Second"), cues.map { it.text })
        assertEquals(1_000L, cues[0].startMs)
        assertEquals(2_000L, cues[0].endMs)
        assertEquals(180_500L, cues[1].startMs)
        assertEquals(181_250L, cues[1].endMs)
    }

    @Test
    fun ass_keepsTextOnlyAndSkipsDrawingsAndComments() {
        val ass = """
            [Script Info]
            Title: test

            [Events]
            Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
            Dialogue: 0,0:00:01.00,0:00:02.50,Default,,0,0,0,,{\an8}Hi\Nthere, friend
            Dialogue: 0,0:00:03.00,0:00:04.00,Default,,0,0,0,,{\p1}m 0 0 l 10 10{\p0}
            Comment: 0,0:00:05.00,0:00:06.00,Default,,0,0,0,,ignored
        """.trimIndent()
        val cues = AssParser.parse(ass, "a")
        assertEquals(1, cues.size)
        assertEquals(1_000L, cues[0].startMs)
        assertEquals(2_500L, cues[0].endMs)
        assertEquals("Hi\nthere, friend", cues[0].text)
    }

    @Test
    fun detectFormat_fromContentThenExtension() {
        assertEquals(SubtitleFormat.VTT, SubtitleParsers.detectFormat("WEBVTT\n\n00:00.000 --> 00:01.000\nx"))
        assertEquals(SubtitleFormat.ASS, SubtitleParsers.detectFormat("[Script Info]\n[Events]\n"))
        assertEquals(SubtitleFormat.SRT, SubtitleParsers.detectFormat("1\n00:00:01,000 --> 00:00:02,000\nx"))
        assertEquals(SubtitleFormat.ASS, SubtitleParsers.detectFormat("", "movie.ssa"))
        assertNull(SubtitleParsers.detectFormat("plain text", "notes.txt"))
    }

    @Test
    fun decoder_utf8WithBomUtf16AndWindows1256() {
        val utf8 = "hello".toByteArray(Charsets.UTF_8)
        assertEquals("hello", SubtitleDecoder.decode(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + utf8))
        val utf16 = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + "hi".toByteArray(Charsets.UTF_16LE)
        assertEquals("hi", SubtitleDecoder.decode(utf16))
        val persian = "سلام كتاب"
        val legacy = persian.toByteArray(SubtitleDecoder.DEFAULT_LEGACY_CHARSET)
        assertEquals(persian, SubtitleDecoder.decode(legacy))
    }
}
