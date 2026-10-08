package com.melonityhub.sublearn.feature.player

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import com.melonityhub.sublearn.core.data.repo.PlaybackSnapshot
import com.melonityhub.sublearn.core.data.repo.PlaybackStateRepository
import com.melonityhub.sublearn.core.data.repo.RecentVideosRepository
import com.melonityhub.sublearn.core.model.Block
import com.melonityhub.sublearn.core.model.LayerRole
import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.model.shadowing.RepeatController
import com.melonityhub.sublearn.core.model.shadowing.RepeatStep
import com.melonityhub.sublearn.core.model.shadowing.ShadowingFormula
import com.melonityhub.sublearn.core.player.PlaybackState
import com.melonityhub.sublearn.core.player.PlayerController
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.SettingsStore
import com.melonityhub.sublearn.core.subtitle.BlockBuilder
import com.melonityhub.sublearn.core.subtitle.BlockOptions
import com.melonityhub.sublearn.core.subtitle.SubtitleDecoder
import com.melonityhub.sublearn.core.subtitle.SubtitleParsers
import com.melonityhub.sublearn.core.translation.TranslationException
import com.melonityhub.sublearn.core.translation.TranslationProvider
import com.melonityhub.sublearn.core.subtitle.TextNormalizer
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository.Companion.normalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns playback for one video: subtitle tracks, tap-translate, shadowing, resume and recents.
 * All playback goes through [PlayerController], so this class is unit-testable with a fake player.
 */
class PlayerViewModel(
    private val source: MediaSource,
    private val player: PlayerController,
    private val settingsStore: SettingsStore,
    private val playbackStates: PlaybackStateRepository,
    private val recents: RecentVideosRepository,
    private val translator: TranslationProvider,
    private val myWords: MyWordsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val learningTrack = MutableStateFlow<SubtitleTrack?>(null)
    private val translationTrack = MutableStateFlow<SubtitleTrack?>(null)
    private val learningVisible = MutableStateFlow(true)
    private val translationVisible = MutableStateFlow(true)
    private val overlay = MutableStateFlow(OverlayState())

    /** Terms the user has saved, normalised, for word underlines (SUB-5). */
    val savedTerms: StateFlow<Set<String>> = myWords.observe("")
        .map { rows -> rows.map { normalize(it.term) }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val tracksAndVisibility = combine(learningTrack, translationTrack, learningVisible, translationVisible) { l, t, lv, tv ->
        TrackSnapshot(l, t, lv, tv)
    }

    val ui: StateFlow<PlayerUi> = combine(player.state, player.embeddedText, tracksAndVisibility, overlay, settings) { state, embedded, snapshot, o, s ->
        val learning = snapshot.learning
        val translation = snapshot.translation
        val learningLine = learning?.let { lineAt(it, state.positionMs - s.subtitles.learningLayer.delayMs) }
        val translationLine = translation?.let { lineAt(it, state.positionMs - s.subtitles.translationLayer.delayMs) }
        PlayerUi(
            title = source.title,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            isPlaying = state.isPlaying,
            isBuffering = state.isBuffering,
            hasMedia = state.hasMedia,
            speed = state.speed,
            errorMessage = state.errorMessage,
            learningLine = learningLine,
            translationLine = translationLine,
            learningTrackName = learning?.name,
            translationTrackName = translation?.name,
            embeddedText = embedded,
            learningVisible = snapshot.learningVisible,
            translationVisible = snapshot.translationVisible,
            popup = o.popup,
            repeatActive = o.repeatActive,
            subtitleListOpen = o.subtitleListOpen,
            subtitleSheetOpen = o.subtitleSheetOpen,
            stopAtBlockEnd = o.stopAtBlockEnd,
            message = o.message,
            cues = learning?.cues.orEmpty(),
            blocks = learning?.blocks.orEmpty(),
        ).let { base ->
            if (o.peek) base.copy(learningVisible = !base.learningVisible, translationVisible = !base.translationVisible) else base
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlayerUi(title = source.title))

    private var repeatController: RepeatController? = null
    private var repeatTarget: Block? = null
    private var repeatJob: Job? = null
    private var awaitingBlockRestart = false
    private var lastSaveAt = 0L

    init {
        viewModelScope.launch { recents.touch(source) }
        viewModelScope.launch { openMedia() }
        viewModelScope.launch {
            settings.map { it.player.decoderMode }.distinctUntilChanged().drop(1).collect { mode ->
                player.setDecoderMode(mode)
            }
        }
        viewModelScope.launch { player.state.collect { onPlaybackChanged(it) } }
    }

    private suspend fun openMedia() {
        val current = settingsStore.settings.first()
        // Codec selection is applied before loading: the Media3 controller rebuilds its player if the mode differs.
        player.setDecoderMode(current.player.decoderMode)
        overlay.value = overlay.value.copy(stopAtBlockEnd = current.shadowing.stopAtBlockEnd)
        val snapshot = playbackStates.load(source.uri)
        val start = if (current.player.resumeFromLastPosition) snapshot?.positionMs ?: 0L else 0L
        player.load(source, start, playWhenReady = true)
    }

    private fun lineAt(track: SubtitleTrack, timeMs: Long): SubtitleLine? {
        val cue = track.index.activeAt(timeMs) ?: return null
        val block = track.blockAt(timeMs)
        return SubtitleLine(
            cueId = cue.id,
            text = cue.text,
            startMs = cue.startMs,
            endMs = cue.endMs,
            blockStartMs = block?.startMs ?: cue.startMs,
            blockEndMs = block?.endMs ?: cue.endMs,
            blockText = block?.text ?: cue.text,
        )
    }

    private fun onPlaybackChanged(state: PlaybackState) {
        if (!state.hasMedia) return
        val now = System.currentTimeMillis()
        if (now - lastSaveAt > SAVE_INTERVAL_MS) {
            lastSaveAt = now
            saveProgress(state)
        }
        handleRepeat(state)
        handleStopAtBlockEnd(state)
    }

    private fun handleRepeat(state: PlaybackState) {
        val target = repeatTarget ?: return
        val controller = repeatController ?: return
        if (awaitingBlockRestart) {
            if (state.positionMs < target.endMs) awaitingBlockRestart = false
            return
        }
        if (!state.isPlaying || state.positionMs < target.endMs) return
        when (val step = controller.onBlockFinished(target.durationMs)) {
            RepeatStep.Replay -> {
                awaitingBlockRestart = true
                player.seekTo(target.startMs)
            }
            is RepeatStep.PauseThenReplay -> {
                awaitingBlockRestart = true
                player.pause()
                repeatJob?.cancel()
                repeatJob = viewModelScope.launch {
                    delay(step.pauseMs)
                    if (repeatTarget != null) {
                        player.seekTo(target.startMs)
                        player.play()
                    }
                }
            }
            RepeatStep.Finish -> stopRepeat()
        }
    }

    private fun handleStopAtBlockEnd(state: PlaybackState) {
        if (!overlay.value.stopAtBlockEnd || repeatController != null || !state.isPlaying) return
        val track = learningTrack.value ?: return
        val block = track.blockAt(state.positionMs - settings.value.subtitles.learningLayer.delayMs) ?: return
        if (state.positionMs >= block.endMs) {
            player.pause()
            overlay.value = overlay.value.copy(stopAtBlockEnd = false)
        }
    }

    private fun saveProgress(state: PlaybackState) {
        viewModelScope.launch {
            playbackStates.save(
                source.uri,
                PlaybackSnapshot(
                    positionMs = state.positionMs,
                    audioTrackId = null,
                    // Track selections are restored in a later phase (needs persisted SAF grants); see KNOWN_ISSUES.
                    learningTrackId = null,
                    translationTrackId = null,
                ),
            )
            recents.updateProgress(source.uri, state.positionMs, state.durationMs)
        }
    }

    // ----- Subtitles -----

    fun loadSubtitle(layer: LayerRole, resolver: ContentResolver, uri: Uri, displayName: String) {
        viewModelScope.launch {
            val options = settings.value.subtitles
            val parsed = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("The subtitle file could not be opened")
                    val charset = runCatching { java.nio.charset.Charset.forName(options.legacyCharset) }
                        .getOrDefault(SubtitleDecoder.DEFAULT_LEGACY_CHARSET)
                    val text = SubtitleDecoder.decode(bytes, charset)
                    val (_, cues) = SubtitleParsers.parseAuto(text, trackId = uri.toString(), fileName = displayName)
                        ?: error("This subtitle format is not supported (SRT, WebVTT and ASS/SSA are)")
                    if (cues.isEmpty()) error("No subtitle lines were found in this file")
                    val blocks = if (options.mergeCuesIntoBlocks) {
                        BlockBuilder.build(
                            cues,
                            BlockOptions(options.blockMaxGapMs, options.blockMaxDurationMs, options.blockMaxChars),
                        )
                    } else {
                        cues.mapIndexed { i, c -> Block(i, c.startMs, c.endMs, c.text, listOf(c.id)) }
                    }
                    SubtitleTrack(displayName, cues, blocks)
                }
            }
            parsed.onSuccess { track ->
                when (layer) {
                    LayerRole.LEARNING -> learningTrack.value = track
                    LayerRole.TRANSLATION -> translationTrack.value = track
                }
                overlay.value = overlay.value.copy(message = null, subtitleSheetOpen = false)
            }.onFailure { error ->
                overlay.value = overlay.value.copy(message = error.message ?: "Subtitles could not be loaded")
            }
        }
    }

    fun clearSubtitle(layer: LayerRole) {
        when (layer) {
            LayerRole.LEARNING -> learningTrack.value = null
            LayerRole.TRANSLATION -> translationTrack.value = null
        }
    }

    fun toggleLayer(layer: LayerRole) {
        when (layer) {
            LayerRole.LEARNING -> learningVisible.value = !learningVisible.value
            LayerRole.TRANSLATION -> translationVisible.value = !translationVisible.value
        }
    }

    fun toggleRotationLock() {
        viewModelScope.launch {
            settingsStore.update { it.copy(player = it.player.copy(rotationLocked = !it.player.rotationLocked)) }
        }
    }

    fun setPeek(peek: Boolean) {
        overlay.value = overlay.value.copy(peek = peek)
    }

    fun dismissMessage() {
        overlay.value = overlay.value.copy(message = null)
    }

    fun showSubtitleSheet(open: Boolean) {
        overlay.value = overlay.value.copy(subtitleSheetOpen = open)
    }

    fun showSubtitleList(open: Boolean) {
        overlay.value = overlay.value.copy(subtitleListOpen = open)
    }

    fun setNoSpoiler(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.update { it.copy(subtitles = it.subtitles.copy(noSpoilerMode = enabled)) }
        }
    }

    fun seekToCue(startMs: Long) {
        player.seekTo(startMs)
        player.play()
    }

    // ----- Transport -----

    fun togglePlay() {
        if (player.state.value.isPlaying) player.pause() else player.play()
    }

    fun seekBy(deltaMs: Long) = player.seekBy(deltaMs)

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun seekToBlock(delta: Int) {
        val track = learningTrack.value ?: return
        val now = player.state.value.positionMs
        val current = track.blockAt(now) ?: return
        val target = track.blocks.getOrNull((current.index + delta).coerceIn(0, track.blocks.lastIndex)) ?: return
        player.seekTo(target.startMs)
    }

    fun setSpeed(speed: Float) = player.setSpeed(speed)

    fun setStopAtBlockEnd(enabled: Boolean) {
        overlay.value = overlay.value.copy(stopAtBlockEnd = enabled)
    }

    // ----- Shadowing (SHD-1..3) -----

    /** Tap = repeat the current block with the configured count; hold = auto-repeat until stopped. */
    fun startRepeat(auto: Boolean) {
        val track = learningTrack.value
        val block = track?.blockAt(player.state.value.positionMs - settings.value.subtitles.learningLayer.delayMs)
        if (block == null) {
            overlay.value = overlay.value.copy(message = "Load a learning subtitle to repeat blocks")
            return
        }
        val shadowing = settings.value.shadowing
        repeatTarget = block
        repeatController = RepeatController(
            repeatCount = if (auto) RepeatController.AUTO_REPEAT else shadowing.repeatCount,
            formula = shadowing.pauseFormula.takeIf { ShadowingFormula.validate(it) == null } ?: ShadowingFormula.DEFAULT_FORMULA,
            multiplier = shadowing.pauseMultiplier,
            minPauseMs = shadowing.minPauseMs,
            maxPauseMs = shadowing.maxPauseMs,
        )
        awaitingBlockRestart = true
        overlay.value = overlay.value.copy(repeatActive = true)
        player.seekTo(block.startMs)
        player.play()
    }

    fun stopRepeat() {
        repeatJob?.cancel()
        repeatJob = null
        repeatController = null
        repeatTarget = null
        awaitingBlockRestart = false
        overlay.value = overlay.value.copy(repeatActive = false)
    }

    // ----- Tap-translate (SUB-4) -----

    /** [taps] 1 = word, 2 = line, 3+ = block. */
    fun translateTapped(word: String?, line: String, block: String, taps: Int) {
        val text = when {
            taps >= 3 -> block
            taps == 2 -> line
            else -> word ?: line
        }.let { TextNormalizer.cleanCueText(it).replace('\n', ' ').trim() }
        if (text.isEmpty()) return
        translate(text)
    }

    private fun translate(text: String) {
        val languages = settings.value.translation
        val pause = languages.pauseOnTranslate && player.state.value.isPlaying
        if (pause) player.pause()
        overlay.value = overlay.value.copy(popup = TranslationPopup(text, null, loading = true, error = null, pausedPlayback = pause))
        viewModelScope.launch {
            try {
                val result = translator.translate(text, languages.sourceLanguage, languages.targetLanguage)
                updatePopup(text) { it.copy(translation = result.text, loading = false) }
            } catch (e: Exception) {
                // TranslationException carries a user-readable message; anything else is shown generically.
                val message = if (e is TranslationException) e.message else null
                updatePopup(text) { it.copy(loading = false, error = message ?: "Translation failed") }
            }
        }
    }

    private fun updatePopup(source: String, transform: (TranslationPopup) -> TranslationPopup) {
        val current = overlay.value.popup ?: return
        if (current.source != source) return
        overlay.value = overlay.value.copy(popup = transform(current))
    }

    fun dismissPopup() {
        val popup = overlay.value.popup ?: return
        overlay.value = overlay.value.copy(popup = null)
        if (popup.pausedPlayback) player.play()
    }

    fun saveWordFromPopup(word: String) {
        val popup = overlay.value.popup
        viewModelScope.launch {
            myWords.add(
                word = word,
                translation = popup?.translation,
                contextSentence = null,
                sourceTitle = source.title,
            )
        }
    }

    /** Called when the screen leaves composition, while the ViewModel scope is still alive. */
    fun saveNow() {
        val state = player.state.value
        if (state.hasMedia) saveProgress(state)
    }

    override fun onCleared() {
        repeatJob?.cancel()
        player.release()
        super.onCleared()
    }

    private data class TrackSnapshot(
        val learning: SubtitleTrack?,
        val translation: SubtitleTrack?,
        val learningVisible: Boolean,
        val translationVisible: Boolean,
    )

    private companion object {
        const val SAVE_INTERVAL_MS = 5_000L
    }
}
