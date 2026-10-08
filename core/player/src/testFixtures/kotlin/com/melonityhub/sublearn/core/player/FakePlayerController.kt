package com.melonityhub.sublearn.core.player

import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.settings.DecoderMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test double for [PlayerController]. It keeps state in flows and records calls, so UI tests can
 * drive the screen without a real decoder. Never used in shipped code paths.
 */
class FakePlayerController(initial: PlaybackState = PlaybackState()) : PlayerController {
    private val _state = MutableStateFlow(initial)
    private val _tracks = MutableStateFlow(TrackList())
    private val _embedded = MutableStateFlow("")

    override val state: StateFlow<PlaybackState> = _state.asStateFlow()
    override val tracks: StateFlow<TrackList> = _tracks.asStateFlow()
    override val mediaPlayer: androidx.media3.common.Player? = null
    override val embeddedText: StateFlow<String> = _embedded.asStateFlow()

    val calls = mutableListOf<String>()
    var loadedSource: MediaSource? = null
        private set

    fun setState(transform: (PlaybackState) -> PlaybackState) {
        _state.value = transform(_state.value)
    }

    fun setTracks(tracks: TrackList) {
        _tracks.value = tracks
    }

    fun setEmbeddedText(text: String) {
        _embedded.value = text
    }

    override fun load(source: MediaSource, startPositionMs: Long, playWhenReady: Boolean) {
        loadedSource = source
        calls += "load:${source.uri}@$startPositionMs"
        _state.value = _state.value.copy(hasMedia = true, positionMs = startPositionMs, isPlaying = playWhenReady, durationMs = 600_000L)
    }

    override fun play() {
        calls += "play"
        _state.value = _state.value.copy(isPlaying = true)
    }

    override fun pause() {
        calls += "pause"
        _state.value = _state.value.copy(isPlaying = false)
    }

    override fun seekTo(positionMs: Long) {
        calls += "seekTo:$positionMs"
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    override fun seekBy(deltaMs: Long) = seekTo(_state.value.positionMs + deltaMs)

    override fun setSpeed(speed: Float) {
        calls += "speed:$speed"
        _state.value = _state.value.copy(speed = speed)
    }

    override fun setDecoderMode(mode: DecoderMode) {
        calls += "decoder:$mode"
    }

    override fun selectAudioTrack(trackId: String?) {
        calls += "audio:$trackId"
    }

    override fun selectEmbeddedTextTrack(trackId: String?) {
        calls += "text:$trackId"
    }

    override fun release() {
        calls += "release"
    }
}
