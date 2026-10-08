package com.melonityhub.sublearn.core.later

import com.melonityhub.sublearn.core.model.Cue
import com.melonityhub.sublearn.core.model.CefrLevel
import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.model.Token

/*
 * LATER extension points. Each interface has a NotImplemented implementation that reports
 * isAvailable = false and throws NotImplementedError if it is ever called. The UI must check
 * isAvailable (or the FeatureFlags constant) first, so a stub is never reached. See EXTENSION_POINTS.md.
 */

/** LATER-3: offline dictionary lookup. NOW the details overlay opens Google Translate instead. */
interface DictionaryProvider {
    val isAvailable: Boolean
    suspend fun lookup(term: String, sourceLanguage: String): DictionaryEntry?
}

data class DictionaryEntry(val headword: String, val senses: List<String>)

object NotImplementedDictionaryProvider : DictionaryProvider {
    override val isAvailable: Boolean = false
    override suspend fun lookup(term: String, sourceLanguage: String): DictionaryEntry =
        throw NotImplementedError("LATER-3: offline dictionary lookup is not implemented yet")
}

/** LATER-9: offline POS, phrasal verb, collocation, idiom and CEFR detection. */
interface WordAnalyzer {
    val isAvailable: Boolean
    fun analyze(tokens: List<Token>, text: String): List<WordAnnotation>
}

data class WordAnnotation(val start: Int, val end: Int, val kind: Kind) {
    enum class Kind { PART_OF_SPEECH, PHRASAL_VERB, COLLOCATION, IDIOM, CEFR }
}

object NotImplementedWordAnalyzer : WordAnalyzer {
    override val isAvailable: Boolean = false
    override fun analyze(tokens: List<Token>, text: String): List<WordAnnotation> =
        throw NotImplementedError("LATER-9: word analysis is not implemented yet")
}

/** LATER-8: offline speech-to-text subtitle generation. */
interface SpeechToText {
    val isAvailable: Boolean
    suspend fun transcribe(audio: MediaSource, language: String): List<Cue>
}

object NotImplementedSpeechToText : SpeechToText {
    override val isAvailable: Boolean = false
    override suspend fun transcribe(audio: MediaSource, language: String): List<Cue> =
        throw NotImplementedError("LATER-8: speech-to-text is not implemented yet")
}

/** LATER-6: update checker via GitHub Releases (stretch item 3). */
interface UpdateChecker {
    val isAvailable: Boolean
    suspend fun latestVersion(): String?
}

object NotImplementedUpdateChecker : UpdateChecker {
    override val isAvailable: Boolean = false
    override suspend fun latestVersion(): String? =
        throw NotImplementedError("LATER-6: update checker is not implemented yet")
}

/** LATER-1: YouTube section. Any undocumented endpoint must sit behind this interface (OTH-4). */
interface VideoSourceResolver {
    val isAvailable: Boolean
    suspend fun resolve(url: String): MediaSource?
}

object NotImplementedVideoSourceResolver : VideoSourceResolver {
    override val isAvailable: Boolean = false
    override suspend fun resolve(url: String): MediaSource? =
        throw NotImplementedError("LATER-1: YouTube source resolution is not implemented yet")
}

/** LATER-5: quiz built from My Words. */
interface QuizEngine {
    val isAvailable: Boolean
    suspend fun questionsFor(words: List<String>): List<QuizQuestion>
}

data class QuizQuestion(val prompt: String, val choices: List<String>, val answerIndex: Int)

object NotImplementedQuizEngine : QuizEngine {
    override val isAvailable: Boolean = false
    override suspend fun questionsFor(words: List<String>): List<QuizQuestion> =
        throw NotImplementedError("LATER-5: quiz is not implemented yet")
}

/** LATER-4: automatic level detection. NOW the level is set manually. */
interface LevelDetector {
    val isAvailable: Boolean
    suspend fun estimate(transcript: String): CefrLevel?
}

object NotImplementedLevelDetector : LevelDetector {
    override val isAvailable: Boolean = false
    override suspend fun estimate(transcript: String): CefrLevel? =
        throw NotImplementedError("LATER-4: level detection is not implemented yet")
}

/** LATER-7: AI re-segmentation of subtitles by word timing and AI quote marking. */
interface SubtitleReSegmenter {
    val isAvailable: Boolean
    suspend fun resegment(cues: List<Cue>): List<Cue>
}

object NotImplementedSubtitleReSegmenter : SubtitleReSegmenter {
    override val isAvailable: Boolean = false
    override suspend fun resegment(cues: List<Cue>): List<Cue> =
        throw NotImplementedError("LATER-7: AI re-segmentation is not implemented yet")
}
