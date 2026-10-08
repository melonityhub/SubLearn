package com.melonityhub.sublearn.feature.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.util.Rational
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.melonityhub.sublearn.core.design.FontResolver
import com.melonityhub.sublearn.core.design.SublearnTokens
import com.melonityhub.sublearn.core.design.GlassPanel
import com.melonityhub.sublearn.core.model.LayerRole
import com.melonityhub.sublearn.core.model.Token
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import com.melonityhub.sublearn.core.player.PlayerController
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.DecoderMode
import com.melonityhub.sublearn.core.settings.FontSurface
import com.melonityhub.sublearn.core.settings.LanguageRole
import com.melonityhub.sublearn.core.settings.OrientationMode
import com.melonityhub.sublearn.core.subtitle.Tokenizer
import com.melonityhub.sublearn.feature.player.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.melonityhub.sublearn.core.model.MediaSource
import kotlin.math.abs

private const val TAP_WINDOW_MS = 320L

/** Full player screen (PLY-1..PLY-7, SUB-1..SUB-4, SHD-1..SHD-3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    source: MediaSource,
    controller: PlayerController,
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    val viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(source, controller) })
    val ui by viewModel.ui.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val savedTerms by viewModel.savedTerms.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var controlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableFloatStateOf(0f) }
    var brightnessHint by remember { mutableStateOf<String?>(null) }
    var seekHint by remember { mutableStateOf<String?>(null) }

    // Auto-hide the overlay after the configured idle time (PLY-2).
    LaunchedEffect(controlsVisible, interactionTick, ui.isPlaying) {
        if (controlsVisible && ui.isPlaying) {
            delay(settings.player.controlsAutoHideMs)
            controlsVisible = false
        }
    }

    // Keep the screen awake while playing and save progress when leaving.
    DisposableEffect(Unit) {
        onDispose { viewModel.saveNow() }
    }

    // Orientation and lock (PLY-5).
    LaunchedEffect(settings.player.orientationMode, settings.player.rotationLocked, activity) {
        activity?.requestedOrientation = when {
            settings.player.rotationLocked -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
            settings.player.orientationMode == OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            settings.player.orientationMode == OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    DisposableEffect(activity) {
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    // Auto picture-in-picture when leaving while playing (PLY-1 / ENG-8).
    LaunchedEffect(ui.isPlaying, settings.player.pictureInPictureOnLeave, activity) {
        activity?.let { a ->
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setAutoEnterEnabled(ui.isPlaying && settings.player.pictureInPictureOnLeave)
                .build()
            a.setPictureInPictureParams(params)
        }
    }

    BackHandler {
        if (ui.popup != null) viewModel.dismissPopup() else onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Video surface.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    subtitleView?.visibility = View.GONE
                }
            },
            update = { view ->
                view.player = controller.mediaPlayer
                view.resizeMode = when (settings.player.aspectMode) {
                    com.melonityhub.sublearn.core.settings.AspectMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    com.melonityhub.sublearn.core.settings.AspectMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
        )

        // 2. Gesture layer (PLY-4). Sits under subtitles and buttons, which take their taps first.
        Box(
            modifier = Modifier.fillMaxSize()
                .pointerInput(settings.player.doubleTapAction) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = { offset ->
                            val rightHalf = offset.x > size.width / 2f
                            when (settings.player.doubleTapAction) {
                                com.melonityhub.sublearn.core.settings.DoubleTapAction.PAUSE -> viewModel.togglePlay()
                                com.melonityhub.sublearn.core.settings.DoubleTapAction.SEEK -> viewModel.seekBy(
                                    (if (rightHalf) 1 else -1) * settings.player.seekStepSeconds * 1000L,
                                )
                            }
                            interactionTick += 1f
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { seekHint = null },
                        onDragEnd = { seekHint = null },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val deltaMs = (dragAmount / size.width * 90_000f).toLong()
                            viewModel.seekBy(deltaMs)
                            seekHint = if (deltaMs >= 0) "+${deltaMs / 1000}s" else "${deltaMs / 1000}s"
                        },
                    )
                }
                .pointerInput(activity) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val fraction = -dragAmount / size.height
                            if (change.position.x < size.width / 2f) {
                                activity?.let { a ->
                                    val current = a.window.attributes.screenBrightness.takeIf { it >= 0f } ?: 0.5f
                                    val next = (current + fraction).coerceIn(0.01f, 1f)
                                    a.window.attributes = a.window.attributes.apply { screenBrightness = next }
                                    brightnessHint = "${(next * 100).toInt()}%"
                                }
                            } else {
                                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val next = (audio.getStreamVolume(AudioManager.STREAM_MUSIC) + fraction * max)
                                    .toInt().coerceIn(0, max)
                                audio.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                                brightnessHint = "${next * 100 / max}%"
                            }
                        },
                    )
                },
        )

        // 3. Subtitle layers (PLY-7, SUB-4). Learning sits at the bottom, translation above it.
        SubtitleLayers(
            ui = ui,
            learningStyle = FontResolver.toTextStyle(
                settings.fonts.resolve(FontSurface.LEARNING_SUBTITLES, LanguageRole.LEARNING),
                Color.White,
            ),
            translationStyle = FontResolver.toTextStyle(
                settings.fonts.resolve(FontSurface.TRANSLATION_SUBTITLES, LanguageRole.NATIVE),
                Color.White,
            ),
            settings = settings,
            savedTerms = savedTerms,
            onWordTap = { line, token, taps ->
                viewModel.translateTapped(token?.text, line.text, line.blockText, taps)
            },
            onTranslationTap = { },
            controlsVisible = controlsVisible,
        )

        // 4. Translation popup (SUB-4, LRN-1).
        ui.popup?.let { popup ->
            TranslationCard(
                popup = popup,
                style = FontResolver.toTextStyle(
                    settings.fonts.resolve(FontSurface.TRANSLATION_POPUPS, LanguageRole.NATIVE),
                    Color.White,
                ),
                onClose = viewModel::dismissPopup,
                onSave = { viewModel.saveWordFromPopup(popup.source.takeIf { it.length <= 40 } ?: popup.source.take(40)) },
                onDetails = { onOpenDetails(popup.source) },
                modifier = Modifier.align(Alignment.Center).padding(SublearnTokens.SpaceL),
            )
        }

        // 5. Overlay: top bar, quick actions, centre and bottom controls (PLY-1..PLY-3).
        AnimatedVisibility(visible = controlsVisible || !ui.isPlaying) {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                TopBar(
                    title = ui.title,
                    decoder = settings.player.decoderMode,
                    onBack = onBack,
                    onList = { viewModel.showSubtitleList(true) },
                    onPip = { activity?.enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()) },
                    onLock = {
                        // The lock state is a setting so it survives rotation and process death.
                        // Toggled through the screen below via SettingsStore in the ViewModel.
                        viewModel.toggleRotationLock()
                    },
                    locked = settings.player.rotationLocked,
                    onSubtitleSheet = { viewModel.showSubtitleSheet(true) },
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                )
                QuickActions(
                    learningOn = ui.learningVisible,
                    translationOn = ui.translationVisible,
                    repeatActive = ui.repeatActive,
                    onToggleLearning = { viewModel.toggleLayer(LayerRole.LEARNING) },
                    onToggleTranslation = { viewModel.toggleLayer(LayerRole.TRANSLATION) },
                    onPeek = { viewModel.setPeek(it) },
                    onRepeatTap = { viewModel.startRepeat(auto = false) },
                    onRepeatHold = { viewModel.startRepeat(auto = true) },
                    onStopRepeat = { viewModel.stopRepeat() },
                    stopAtBlockEnd = ui.stopAtBlockEnd,
                    onToggleStopAtBlockEnd = { viewModel.setStopAtBlockEnd(!ui.stopAtBlockEnd) },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = SublearnTokens.SpaceS),
                )
                BottomControls(
                    ui = ui,
                    onPrevBlock = { viewModel.seekToBlock(-1) },
                    onNextBlock = { viewModel.seekToBlock(1) },
                    onPlayPause = viewModel::togglePlay,
                    onSeek = viewModel::seekTo,
                    onSpeed = { viewModel.setSpeed(nextSpeed(ui.speed)) },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(SublearnTokens.SpaceM),
                )
            }
        }

        // Transient hints for gestures and messages.
        val hint = brightnessHint ?: seekHint
        if (hint != null) {
            LaunchedEffect(hint) {
                delay(700)
                brightnessHint = null
                seekHint = null
            }
            Text(
                text = hint,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(SublearnTokens.CornerMedium))
                    .padding(horizontal = SublearnTokens.SpaceL, vertical = SublearnTokens.SpaceS),
            )
        }
        ui.message?.let { message ->
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(SublearnTokens.SpaceL),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(modifier = Modifier.padding(SublearnTokens.SpaceM), verticalAlignment = Alignment.CenterVertically) {
                    Text(message, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::dismissMessage) { Text(stringResource(R.string.player_close)) }
                }
            }
        }
    }

    if (ui.subtitleListOpen) {
        SubtitleListSheet(
            cues = ui.cues,
            positionMs = ui.positionMs,
            noSpoiler = settings.subtitles.noSpoilerMode,
            onSelect = { cue ->
                viewModel.seekToCue(cue.startMs)
                viewModel.showSubtitleList(false)
            },
            onNoSpoilerToggle = { viewModel.setNoSpoiler(!settings.subtitles.noSpoilerMode) },
            onDismiss = { viewModel.showSubtitleList(false) },
        )
    }
    if (ui.subtitleSheetOpen) {
        SubtitleSourceSheet(
            learningName = ui.learningTrackName,
            translationName = ui.translationTrackName,
            onLoad = { layer, uri, name -> viewModel.loadSubtitle(layer, context.contentResolver, uri, name) },
            onClear = { viewModel.clearSubtitle(it) },
            onDismiss = { viewModel.showSubtitleSheet(false) },
        )
    }
}

@Composable
private fun SubtitleLayers(
    ui: PlayerUi,
    learningStyle: TextStyle,
    translationStyle: TextStyle,
    settings: AppSettings,
    savedTerms: Set<String>,
    onWordTap: (SubtitleLine, Token?, Int) -> Unit,
    onTranslationTap: (SubtitleLine) -> Unit,
    controlsVisible: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val learningPad = (maxHeight * settings.subtitles.learningLayer.bottomFraction) + if (controlsVisible) 96.dp else 0.dp
        val translationPad = learningPad + (maxHeight * 0.06f) + 40.dp
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = learningPad)) {
            Spacer(Modifier.height(0.dp))
        }
        val learningLine = ui.learningLine
        if (ui.learningVisible && (learningLine != null || ui.embeddedText.isNotEmpty())) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = learningPad),
                contentAlignment = Alignment.Center,
            ) {
                if (learningLine != null) {
                    TappableLine(
                        line = learningLine,
                        style = learningStyle,
                        savedTerms = savedTerms,
                        opacityPercent = settings.subtitles.learningLayer.opacityPercent,
                        onTap = onWordTap,
                    )
                } else {
                    Text(
                        ui.embeddedText,
                        style = learningStyle,
                        color = learningStyle.color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = SublearnTokens.SpaceL)
                            .background(Color.Black.copy(alpha = settings.subtitles.learningLayer.opacityPercent / 100f), RoundedCornerShape(SublearnTokens.CornerSmall))
                            .padding(horizontal = SublearnTokens.SpaceS),
                    )
                }
            }
        }
        val translationLine = ui.translationLine
        if (ui.translationVisible && translationLine != null) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = translationPad),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    translationLine.text,
                    style = translationStyle,
                    color = translationStyle.color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = SublearnTokens.SpaceL)
                        .background(Color.Black.copy(alpha = settings.subtitles.translationLayer.opacityPercent / 100f), RoundedCornerShape(SublearnTokens.CornerSmall))
                        .padding(horizontal = SublearnTokens.SpaceS)
                        .pointerInput(translationLine) { detectTapGestures(onTap = { onTranslationTap(translationLine) }) },
                )
            }
        }
    }
}

/**
 * A learning subtitle line whose words can be tapped (SUB-4). Hit-testing uses the layout's logical
 * offsets, so it is correct for Persian (RTL) and English in the same line. Taps are counted in a short
 * window: one tap = word, two = line, three = block.
 */
@Composable
private fun TappableLine(
    line: SubtitleLine,
    style: TextStyle,
    savedTerms: Set<String>,
    opacityPercent: Int,
    onTap: (SubtitleLine, Token?, Int) -> Unit,
) {
    val tokens = remember(line.text) { Tokenizer.tokenize(line.text) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val annotated = remember(line.text, savedTerms) {
        buildAnnotatedString {
            append(line.text)
            tokens.forEach { token ->
                if (token.isWord && com.melonityhub.sublearn.core.data.repo.MyWordsRepository.normalize(token.text) in savedTerms) {
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold, color = SublearnTokens.Accent),
                        token.start,
                        token.end,
                    )
                }
            }
        }
    }
    val currentLine by rememberUpdatedState(line)
    val currentTokens by rememberUpdatedState(tokens)
    val currentOnTap by rememberUpdatedState(onTap)
    val scope = rememberCoroutineScope()
    val sequencer = remember(scope) { TapSequencer(scope) }
    Text(
        text = annotated,
        style = style,
        color = style.color,
        textAlign = TextAlign.Center,
        onTextLayout = { layout = it },
        modifier = Modifier
            .padding(horizontal = SublearnTokens.SpaceL)
            .background(Color.Black.copy(alpha = opacityPercent / 100f), RoundedCornerShape(SublearnTokens.CornerSmall))
            .padding(horizontal = SublearnTokens.SpaceS)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { position: Offset ->
                    val textLayout = layout ?: return@detectTapGestures
                    val offset = textLayout.getOffsetForPosition(position)
                    val token = Tokenizer.wordAt(currentTokens, offset)
                    sequencer.tap(currentLine, token) { taps, tapped, shown ->
                        currentOnTap(shown, tapped, taps)
                    }
                })
            }
            .semantics { contentDescription = line.text },
    )
}

/** Counts taps within [TAP_WINDOW_MS] and reports once the window closes. */
private class TapSequencer(private val scope: CoroutineScope) {
    private var job: Job? = null
    private var count = 0
    private var lastToken: Token? = null

    fun tap(line: SubtitleLine, token: Token?, report: (Int, Token?, SubtitleLine) -> Unit) {
        count = if (job?.isActive == true) count + 1 else 1
        lastToken = token ?: lastToken?.takeIf { count > 1 }
        job?.cancel()
        val taps = count
        val tapped = token
        job = scope.launch {
            delay(TAP_WINDOW_MS)
            report(taps, if (taps == 1) tapped else lastToken, line)
            count = 0
            lastToken = null
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    decoder: DecoderMode,
    onBack: () -> Unit,
    onList: () -> Unit,
    onPip: () -> Unit,
    onLock: () -> Unit,
    locked: Boolean,
    onSubtitleSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = SublearnTokens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "back" }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.player_back), tint = Color.White)
        }
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(horizontal = SublearnTokens.SpaceS),
        )
        Text(
            text = decoderLabel(decoder),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(SublearnTokens.CornerSmall))
                .padding(horizontal = SublearnTokens.SpaceS, vertical = SublearnTokens.SpaceXs),
        )
        IconButton(
            onClick = onList,
            modifier = Modifier.semantics { contentDescription = "subtitle list" },
        ) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.player_subtitle_list), tint = Color.White)
        }
        IconButton(onClick = onSubtitleSheet) {
            Icon(Icons.Filled.Subtitles, contentDescription = stringResource(R.string.player_subtitles_load), tint = Color.White)
        }
        IconButton(onClick = onPip) {
            Icon(Icons.Filled.PictureInPictureAlt, contentDescription = stringResource(R.string.player_pip), tint = Color.White)
        }
        IconButton(onClick = onLock) {
            Icon(
                if (locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = stringResource(if (locked) R.string.player_unlock else R.string.player_lock),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun decoderLabel(mode: DecoderMode): String = stringResource(
    when (mode) {
        DecoderMode.SW -> R.string.player_decoder_sw
        DecoderMode.HW -> R.string.player_decoder_hw
        DecoderMode.HW_PLUS -> R.string.player_decoder_hw_plus
    },
)

@Composable
private fun QuickActions(
    learningOn: Boolean,
    translationOn: Boolean,
    repeatActive: Boolean,
    onToggleLearning: () -> Unit,
    onToggleTranslation: () -> Unit,
    onPeek: (Boolean) -> Unit,
    onRepeatTap: () -> Unit,
    onRepeatHold: () -> Unit,
    onStopRepeat: () -> Unit,
    stopAtBlockEnd: Boolean,
    onToggleStopAtBlockEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
        QuickButton(
            label = stringResource(R.string.player_learning_layer),
            active = learningOn,
            icon = { Text("EN", color = Color.White, style = MaterialTheme.typography.labelLarge) },
            onTap = onToggleLearning,
            onPeek = onPeek,
        )
        QuickButton(
            label = stringResource(R.string.player_translation_layer),
            active = translationOn,
            icon = { Icon(Icons.Filled.Translate, contentDescription = null, tint = Color.White) },
            onTap = onToggleTranslation,
            onPeek = onPeek,
        )
        QuickButton(
            label = stringResource(R.string.player_repeat),
            active = repeatActive,
            icon = { Icon(Icons.Filled.Repeat, contentDescription = null, tint = Color.White) },
            onTap = { if (repeatActive) onStopRepeat() else onRepeatTap() },
            onHold = onRepeatHold,
        )
        QuickButton(
            label = stringResource(R.string.player_stop_at_block_end),
            active = stopAtBlockEnd,
            icon = { Text("▮", color = Color.White) },
            onTap = onToggleStopAtBlockEnd,
        )
    }
}

@Composable
private fun QuickButton(
    label: String,
    active: Boolean,
    icon: @Composable () -> Unit,
    onTap: () -> Unit,
    onPeek: ((Boolean) -> Unit)? = null,
    onHold: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .width(SublearnTokens.MinTouchTarget)
            .height(SublearnTokens.MinTouchTarget)
            .clip(RoundedCornerShape(SublearnTokens.CornerMedium))
            .background(if (active) SublearnTokens.AccentSoft else Color.Black.copy(alpha = 0.45f))
            .semantics { contentDescription = label }
            .pointerInput(active) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = {
                        if (onHold != null) onHold() else onPeek?.invoke(true)
                    },
                    onPress = {
                        val released = tryAwaitRelease()
                        if (onPeek != null && onHold == null && released) onPeek.invoke(false)
                        if (onPeek != null && onHold == null && !released) onPeek.invoke(false)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Composable
private fun BottomControls(
    ui: PlayerUi,
    onPrevBlock: () -> Unit,
    onNextBlock: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = ui.durationMs.coerceAtLeast(1L)
    var dragging by remember { mutableStateOf<Float?>(null) }
    val progress = dragging ?: (ui.positionMs.toFloat() / duration).coerceIn(0f, 1f)
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatTime(ui.positionMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
            Slider(
                value = progress,
                onValueChange = { dragging = it },
                onValueChangeFinished = {
                    dragging?.let { onSeek((it * duration).toLong()) }
                    dragging = null
                },
                modifier = Modifier.weight(1f).padding(horizontal = SublearnTokens.SpaceS)
                    .semantics { contentDescription = "seek" },
            )
            Text(formatTime(ui.durationMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevBlock) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.player_prev_block), tint = Color.White)
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.size56()) {
                Icon(
                    if (ui.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (ui.isPlaying) R.string.player_pause else R.string.player_play),
                    tint = Color.White,
                )
            }
            IconButton(onClick = onNextBlock) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.player_next_block), tint = Color.White)
            }
            TextButton(onClick = onSpeed) {
                Text(speedLabel(ui.speed), color = Color.White)
            }
        }
    }
}

private fun Modifier.size56(): Modifier = this.width(56.dp).height(56.dp)

private val PLAYBACK_SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)

/** Cycles through the speeds offered in the player (PLY-3 speed control). */
internal fun nextSpeed(current: Float): Float {
    val index = PLAYBACK_SPEEDS.indexOfFirst { abs(it - current) < 0.01f }
    return PLAYBACK_SPEEDS[(index + 1) % PLAYBACK_SPEEDS.size]
}

private fun speedLabel(speed: Float): String = "${speed.toString().removeSuffix(".0")}x"
@Composable
private fun TranslationCard(
    popup: TranslationPopup,
    style: TextStyle,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassPanel(modifier = modifier.fillMaxWidth(0.92f), alpha = 0.9f) {
        Column(verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
            Text(popup.source, style = style.copy(color = Color.White))
            when {
                popup.loading -> Text(stringResource(R.string.player_translation_loading), color = Color.White.copy(alpha = 0.8f))
                popup.error != null -> Text(popup.error, color = SublearnTokens.Danger)
                popup.translation != null -> Text(popup.translation, style = style.copy(color = SublearnTokens.Accent))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
                OutlinedButton(onClick = onSave, enabled = !popup.loading) { Text(stringResource(R.string.player_save_word)) }
                OutlinedButton(onClick = onDetails) { Text(stringResource(R.string.player_details)) }
                Button(onClick = onClose) { Text(stringResource(R.string.player_close)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleListSheet(
    cues: List<com.melonityhub.sublearn.core.model.Cue>,
    positionMs: Long,
    noSpoiler: Boolean,
    onSelect: (com.melonityhub.sublearn.core.model.Cue) -> Unit,
    onNoSpoilerToggle: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val activeIndex = remember(cues, positionMs) { cues.indexOfLast { it.startMs <= positionMs }.coerceAtLeast(0) }
    LaunchedEffect(activeIndex) { if (cues.isNotEmpty()) listState.animateScrollToItem(activeIndex) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Row(modifier = Modifier.fillMaxWidth().padding(SublearnTokens.SpaceM), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.player_list_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            // Long-press toggles no-spoiler mode (PLY-6): upcoming lines are hidden.
            Box(
                modifier = Modifier.clip(RoundedCornerShape(SublearnTokens.CornerSmall))
                    .background(if (noSpoiler) SublearnTokens.AccentSoft else Color.Transparent)
                    .padding(SublearnTokens.SpaceS)
                    .pointerInput(noSpoiler) { detectTapGestures(onLongPress = { onNoSpoilerToggle() }) }
                    .semantics { contentDescription = "no spoiler" },
            ) {
                Text(stringResource(R.string.player_no_spoiler))
            }
        }
        if (cues.isEmpty()) {
            Text(
                stringResource(R.string.player_list_empty),
                modifier = Modifier.padding(SublearnTokens.SpaceL),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().height(420.dp)) {
            itemsIndexed(cues, key = { _, cue -> cue.id }) { index, cue ->
                val hidden = noSpoiler && cue.startMs > positionMs
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (index == activeIndex) SublearnTokens.AccentSoft else Color.Transparent)
                        .padding(horizontal = SublearnTokens.SpaceM, vertical = SublearnTokens.SpaceS)
                        .semantics { contentDescription = "subtitle line ${index + 1}" },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(formatTime(cue.startMs), style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(64.dp))
                    Text(if (hidden) "••••" else cue.text, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onSelect(cue) }) { Text(stringResource(R.string.player_play)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleSourceSheet(
    learningName: String?,
    translationName: String?,
    onLoad: (LayerRole, Uri, String) -> Unit,
    onClear: (LayerRole) -> Unit,
    onDismiss: () -> Unit,
) {
    val learningPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { onLoad(LayerRole.LEARNING, it, displayNameOf(it)) } }
    val translationPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { onLoad(LayerRole.TRANSLATION, it, displayNameOf(it)) } }
    val mimeTypes = arrayOf("application/x-subrip", "text/vtt", "text/x-ssa", "text/plain", "application/octet-stream")
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(SublearnTokens.SpaceL), verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
            Text(stringResource(R.string.player_subtitles_load), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.player_subtitle_formats), style = MaterialTheme.typography.bodySmall)
            Button(onClick = { learningPicker.launch(mimeTypes) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.player_load_learning) + (learningName?.let { ": $it" } ?: ""))
            }
            OutlinedButton(onClick = { translationPicker.launch(mimeTypes) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.player_load_translation) + (translationName?.let { ": $it" } ?: ""))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
                TextButton(onClick = { onClear(LayerRole.LEARNING) }) { Text(stringResource(R.string.player_clear_learning)) }
                TextButton(onClick = { onClear(LayerRole.TRANSLATION) }) { Text(stringResource(R.string.player_clear_translation)) }
            }
        }
    }
}

private fun displayNameOf(uri: Uri): String = uri.lastPathSegment?.substringAfterLast('/') ?: "subtitle"

private fun formatTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000)
    val h = total / 3600
    val m = (total / 60) % 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringResource(id: Int, vararg args: Any): String = androidx.compose.ui.res.stringResource(id, *args)
