@file:OptIn(UnstableApi::class)

package com.melonityhub.sublearn.core.player


import androidx.media3.common.util.UnstableApi
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.settings.DecoderMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Media3 / ExoPlayer implementation of [PlayerController] (ENG-1). Must be created and used on the
 * main thread. Audio focus and becoming-noisy handling come from ExoPlayer; PiP lives in the UI layer.
 */
class Media3PlayerController(
    context: Context,
    private val scope: CoroutineScope,
    initialDecoderMode: DecoderMode,
) : PlayerController {

    private val appContext = context.applicationContext
    private var decoderMode = initialDecoderMode
    private var player: ExoPlayer = buildPlayer(appContext, decoderMode)
    private val _state = MutableStateFlow(PlaybackState())
    private val _tracks = MutableStateFlow(TrackList())
    private val _embeddedText = MutableStateFlow("")
    private var pollJob: Job? = null

    override val state: StateFlow<PlaybackState> = _state.asStateFlow()
    override val tracks: StateFlow<TrackList> = _tracks.asStateFlow()
    override val embeddedText: StateFlow<String> = _embeddedText.asStateFlow()
    override val mediaPlayer: Player get() = player

    init {
        attachListener(player)
    }

    private fun attachListener(target: ExoPlayer) {
        target.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) = publish()
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                publish()
                updatePolling(isPlaying)
            }
            override fun onPlayerError(error: PlaybackException) {
                _state.value = _state.value.copy(errorMessage = error.errorCodeName, isPlaying = false)
            }
            override fun onTracksChanged(tracks: Tracks) = publishTracks(tracks)
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                _state.value = _state.value.copy(videoWidth = videoSize.width, videoHeight = videoSize.height)
            }
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _state.value = _state.value.copy(speed = playbackParameters.speed)
            }
            override fun onCues(cueGroup: CueGroup) {
                _embeddedText.value = cueGroup.cues.mapNotNull { it.text?.toString() }.joinToString("\n")
            }
        })
    }

    override fun load(source: MediaSource, startPositionMs: Long, playWhenReady: Boolean) {
        _embeddedText.value = ""
        _state.value = PlaybackState(hasMedia = true, positionMs = startPositionMs)
        val item = MediaItem.Builder()
            .setUri(source.uri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(source.title).build())
            .build()
        player.setMediaItem(item, startPositionMs)
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    override fun play() = player.play()
    override fun pause() = player.pause()
    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        publish()
    }
    override fun seekBy(deltaMs: Long) = seekTo(player.currentPosition + deltaMs)
    override fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed.coerceIn(0.25f, 4f))
    }

    override fun setDecoderMode(mode: DecoderMode) {
        if (mode == decoderMode) return
        decoderMode = mode
        // Codec selection is fixed when renderers are created, so the player is rebuilt at the same position.
        val position = player.currentPosition
        val playWhenReady = player.playWhenReady
        val item = player.currentMediaItem
        pollJob?.cancel()
        player.release()
        player = buildPlayer(appContext, mode)
        attachListener(player)
        if (item != null) {
            player.setMediaItem(item, position)
            player.prepare()
            player.playWhenReady = playWhenReady
        }
        publish()
    }

    override fun selectAudioTrack(trackId: String?) = selectTrack(C.TRACK_TYPE_AUDIO, trackId)

    override fun selectEmbeddedTextTrack(trackId: String?) {
        if (trackId == null) {
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            _embeddedText.value = ""
        } else {
            selectTrack(C.TRACK_TYPE_TEXT, trackId)
        }
    }

    override fun release() {
        pollJob?.cancel()
        player.release()
    }

    private fun selectTrack(type: Int, trackId: String?) {
        val builder = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(type, trackId == null)
        if (trackId != null) {
            val (groupIndex, trackIndex) = trackId.split(':').map { it.toInt() }
            val group = player.currentTracks.groups.getOrNull(groupIndex) ?: return
            builder.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
        }
        player.trackSelectionParameters = builder.build()
    }

    private fun updatePolling(isPlaying: Boolean) {
        pollJob?.cancel()
        if (!isPlaying) return
        pollJob = scope.launch {
            while (isActive) {
                publish()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun publish() {
        val durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
        _state.value = _state.value.copy(
            hasMedia = player.currentMediaItem != null,
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            ended = player.playbackState == Player.STATE_ENDED,
            positionMs = player.currentPosition,
            durationMs = durationMs,
            bufferedMs = player.bufferedPosition,
            speed = player.playbackParameters.speed,
            errorMessage = if (player.playerError == null) null else _state.value.errorMessage,
        )
    }

    private fun publishTracks(tracks: Tracks) {
        val audio = mutableListOf<TrackOption>()
        val text = mutableListOf<TrackOption>()
        tracks.groups.forEachIndexed { groupIndex, group ->
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                val option = TrackOption(
                    id = "$groupIndex:$trackIndex",
                    label = format.label ?: format.language ?: "Track ${trackIndex + 1}",
                    language = format.language,
                    selected = group.isTrackSelected(trackIndex),
                )
                when (group.type) {
                    C.TRACK_TYPE_AUDIO -> audio += option
                    C.TRACK_TYPE_TEXT -> text += option
                }
            }
        }
        _tracks.value = TrackList(audio = audio, embeddedText = text)
    }

    private fun buildPlayer(context: Context, mode: DecoderMode): ExoPlayer {
        val renderers = DefaultRenderersFactory(context).setMediaCodecSelector(DecoderSelectors.forMode(mode))
        return ExoPlayer.Builder(context, renderers)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 200L
    }
}
