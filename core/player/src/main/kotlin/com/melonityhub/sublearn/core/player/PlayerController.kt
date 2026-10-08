package com.melonityhub.sublearn.core.player

import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.settings.DecoderMode
import androidx.media3.common.Player
import kotlinx.coroutines.flow.StateFlow

/**
 * The player boundary (ENG-2). The UI and ViewModels depend only on this interface, so tests use
 * [FakePlayerController] and the Media3 implementation can change without touching feature code.
 */
interface PlayerController {
    val state: StateFlow<PlaybackState>
    val tracks: StateFlow<TrackList>

    /** The Media3 player used by the video surface. Null for test doubles. */
    val mediaPlayer: Player?

    /** Text of the embedded (container) subtitle track currently on screen, or empty. */
    val embeddedText: StateFlow<String>

    fun load(source: MediaSource, startPositionMs: Long, playWhenReady: Boolean)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun seekBy(deltaMs: Long)
    fun setSpeed(speed: Float)
    fun setDecoderMode(mode: DecoderMode)
    fun selectAudioTrack(trackId: String?)
    fun selectEmbeddedTextTrack(trackId: String?)
    fun release()
}

data class PlaybackState(
    val hasMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val ended: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val speed: Float = 1f,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val errorMessage: String? = null,
) {
    val isLoaded: Boolean get() = hasMedia && durationMs >= 0L
    val aspectRatio: Float get() = if (videoWidth > 0 && videoHeight > 0) videoWidth.toFloat() / videoHeight else 16f / 9f
}

data class TrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val selected: Boolean,
)

data class TrackList(
    val audio: List<TrackOption> = emptyList(),
    val embeddedText: List<TrackOption> = emptyList(),
)
