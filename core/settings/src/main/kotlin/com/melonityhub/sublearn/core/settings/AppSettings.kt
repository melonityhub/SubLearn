package com.melonityhub.sublearn.core.settings

import kotlinx.serialization.Serializable

/**
 * The whole typed settings tree (ENG-6). API keys are NOT stored here (see D-011): they live in the
 * Keystore-backed secret store, so they can never end up in an export.
 */
@Serializable
data class AppSettings(
    val schemaVersion: Int = SettingsCodec.CURRENT_VERSION,
    val appearance: AppearanceSettings = AppearanceSettings(),
    val player: PlayerSettings = PlayerSettings(),
    val subtitles: SubtitleSettings = SubtitleSettings(),
    val fonts: FontSettings = FontSettings(),
    val gestures: GestureSettings = GestureSettings(),
    val shadowing: ShadowingSettings = ShadowingSettings(),
    val learning: LearningSettings = LearningSettings(),
    val translation: TranslationSettings = TranslationSettings(),
    val ai: AiSettings = AiSettings(),
    val dictionary: DictionarySettings = DictionarySettings(),
    val quickActions: QuickActionSettings = QuickActionSettings(),
) {
    /** Clamps numeric fields into safe ranges so a hand-edited import cannot break playback. */
    fun sanitized(): AppSettings = copy(
        player = player.copy(
            defaultSpeed = player.defaultSpeed.coerceIn(0.25f, 4f),
            seekStepSeconds = player.seekStepSeconds.coerceIn(1, 120),
            controlsAutoHideMs = player.controlsAutoHideMs.coerceIn(1_000L, 15_000L),
        ),
        subtitles = subtitles.copy(
            learningLayer = subtitles.learningLayer.sanitized(),
            translationLayer = subtitles.translationLayer.sanitized(),
            blockMaxGapMs = subtitles.blockMaxGapMs.coerceIn(100L, 10_000L),
            blockMaxDurationMs = subtitles.blockMaxDurationMs.coerceIn(1_000L, 60_000L),
            blockMaxChars = subtitles.blockMaxChars.coerceIn(20, 400),
        ),
        fonts = fonts.copy(
            overrides = fonts.overrides.mapValues { (_, style) -> style.copy(sizeSp = style.sizeSp.coerceIn(8, 48)) },
        ),
        shadowing = shadowing.copy(
            repeatCount = if (shadowing.repeatCount < 0) -1 else shadowing.repeatCount.coerceAtMost(100),
            pauseMultiplier = shadowing.pauseMultiplier.coerceIn(0.0, 5.0),
            minPauseMs = shadowing.minPauseMs.coerceIn(0L, 60_000L),
            maxPauseMs = shadowing.maxPauseMs.coerceIn(0L, 60_000L),
        ),
        learning = learning.copy(
            maxPopupsPerBlock = learning.maxPopupsPerBlock.coerceIn(0, 10),
            popupDisplayMs = learning.popupDisplayMs.coerceIn(1_000L, 15_000L),
        ),
        ai = ai.copy(
            contextBlocks = ai.contextBlocks.coerceIn(0, 50),
            requestTimeoutSeconds = ai.requestTimeoutSeconds.coerceIn(5, 300),
        ),
    )
}

private fun LayerSettings.sanitized(): LayerSettings = copy(
    opacityPercent = opacityPercent.coerceIn(0, 100),
    bottomFraction = bottomFraction.coerceIn(0f, 0.9f),
    maxLines = maxLines.coerceIn(1, 6),
    delayMs = delayMs.coerceIn(-30_000L, 30_000L),
)
