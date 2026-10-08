package com.melonityhub.sublearn.core.settings

import com.melonityhub.sublearn.core.model.AiProviderId
import kotlinx.serialization.Serializable

/** Font style for one (surface, role) pair. [colorArgb] null means "use the theme colour". */
@Serializable
data class TextStyleSpec(
    val family: FontFamilyChoice = FontFamilyChoice.SYSTEM,
    val sizeSp: Int = 16,
    val weight: Int = 400,
    val italic: Boolean = false,
    val colorArgb: Long? = null,
)

@Serializable
data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val uiLanguage: UiLanguage = UiLanguage.ENGLISH,
    val reduceMotion: Boolean = false,
)

@Serializable
data class PlayerSettings(
    val decoderMode: DecoderMode = DecoderMode.HW_PLUS,
    val defaultSpeed: Float = 1.0f,
    val seekStepSeconds: Int = 10,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.PAUSE,
    val orientationMode: OrientationMode = OrientationMode.AUTO,
    val rotationLocked: Boolean = false,
    val aspectMode: AspectMode = AspectMode.FIT,
    val controlsAutoHideMs: Long = 3_000L,
    val resumeFromLastPosition: Boolean = true,
    val pictureInPictureOnLeave: Boolean = true,
)

/** One subtitle layer's MX-like options (PLY-7). */
@Serializable
data class LayerSettings(
    val visible: Boolean = true,
    val opacityPercent: Int = 75,
    val bottomFraction: Float = 0.12f,
    val maxLines: Int = 3,
    val delayMs: Long = 0L,
)

@Serializable
data class WordStyle(
    val decoration: WordDecoration = WordDecoration.NONE,
    val colorArgb: Long? = null,
)

/**
 * Word colouring (SUB-5). POS and phrasal styles take effect once LATER-9 is built; the My Words style is NOW.
 */
@Serializable
data class WordStyleSettings(
    val myWords: WordStyle = WordStyle(WordDecoration.UNDERLINE),
    val partOfSpeech: WordStyle = WordStyle(WordDecoration.NONE),
    val phrasal: WordStyle = WordStyle(WordDecoration.DOTTED_UNDERLINE),
)

@Serializable
data class SubtitleSettings(
    val learningLayer: LayerSettings = LayerSettings(),
    val translationLayer: LayerSettings = LayerSettings(bottomFraction = 0.2f, maxLines = 2),
    val legacyCharset: String = "windows-1256",
    val mergeCuesIntoBlocks: Boolean = true,
    val blockMaxGapMs: Long = 1_200L,
    val blockMaxDurationMs: Long = 6_000L,
    val blockMaxChars: Int = 96,
    val wordStyles: WordStyleSettings = WordStyleSettings(),
    val subtitleListInLandscape: Boolean = true,
    val noSpoilerMode: Boolean = false,
)

/** Persisted font overrides; any missing (surface, role) uses [FontSettings.resolve] defaults. */
@Serializable
data class FontSettings(
    val overrides: Map<String, TextStyleSpec> = emptyMap(),
) {
    fun resolve(surface: FontSurface, role: LanguageRole): TextStyleSpec =
        overrides[key(surface, role)] ?: defaultFor(surface, role)

    fun withStyle(surface: FontSurface, role: LanguageRole, style: TextStyleSpec): FontSettings =
        copy(overrides = overrides + (key(surface, role) to style))

    companion object {
        fun key(surface: FontSurface, role: LanguageRole): String = "${surface.name}.${role.name}"

        fun defaultFor(surface: FontSurface, role: LanguageRole): TextStyleSpec {
            val family = if (role == LanguageRole.LEARNING) FontFamilyChoice.SANS_SERIF else FontFamilyChoice.SYSTEM
            val (size, weight) = when (surface) {
                FontSurface.APP_MENUS -> 14 to 400
                FontSurface.LEARNING_SUBTITLES -> 20 to 600
                FontSurface.TRANSLATION_SUBTITLES -> 16 to 500
                FontSurface.TRANSLATION_POPUPS -> 15 to 500
                FontSurface.WORD_CARDS -> 16 to 600
                FontSurface.AI_ANSWERS -> 15 to 400
            }
            return TextStyleSpec(family = family, sizeSp = size, weight = weight)
        }
    }
}

@Serializable
data class GestureSettings(
    val leftVertical: GestureAction = GestureAction.BRIGHTNESS,
    val rightVertical: GestureAction = GestureAction.VOLUME,
    val horizontal: GestureAction = GestureAction.SEEK,
    val twoFingerSwipeUp: GestureAction = GestureAction.SPEED_UP,
    val twoFingerSwipeDown: GestureAction = GestureAction.SPEED_DOWN,
)

@Serializable
data class ShadowingSettings(
    /** Extra repeats after the first play. -1 means auto-repeat until stopped. */
    val repeatCount: Int = 2,
    val pauseMultiplier: Double = 1.0,
    val pauseFormula: String = "M * D",
    val minPauseMs: Long = 0L,
    val maxPauseMs: Long = 30_000L,
    val stopAtBlockEnd: Boolean = false,
)

@Serializable
data class LearningSettings(
    val mode: LearningMode = LearningMode.ENTERTAINMENT,
    val manualLevel: String = "B1",
    val maxPopupsPerBlock: Int = 3,
    val popupDisplayMs: Long = 3_500L,
)

@Serializable
data class TranslationSettings(
    val sourceLanguage: String = "en",
    val targetLanguage: String = "fa",
    val pauseOnTranslate: Boolean = true,
    val detailsSource: DetailsSource = DetailsSource.GOOGLE_TRANSLATE,
)

@Serializable
data class AiSettings(
    val provider: AiProviderId = AiProviderId.GEMINI,
    val geminiModel: String = "gemini-2.5-flash",
    val openAiModel: String = "gpt-4o-mini",
    val anthropicModel: String = "claude-haiku-4-5",
    val promptTemplate: String = DEFAULT_PROMPT,
    val contextBlocks: Int = 10,
    val includeFilmTitle: Boolean = true,
    val includeTimestamps: Boolean = true,
    val pauseOnResult: Boolean = true,
    val requestTimeoutSeconds: Int = 60,
) {
    companion object {
        const val DEFAULT_PROMPT: String =
            "You help an English learner understand a line from a film or video.\n" +
                "Explain the selected text (or the whole block) in plain English, then answer in Persian if it helps.\n" +
                "Cover: 1) the tone, 2) why it is used here, 3) how it differs from close synonyms, " +
                "4) where else it is commonly used.\n\n" +
                "{{context}}\n\nSelected text: {{selection}}"
    }
}

/** Dictionary/detail options (LRN-1). The offline dictionary is LATER-3 and is shown disabled. */
@Serializable
data class DictionarySettings(
    val preferOfflineWhenAvailable: Boolean = false,
)

/** One quick-action button (SUB-1, SUB-2). Positions are fractions of the player surface. */
@Serializable
data class QuickButtonSettings(
    val dock: ButtonDock = ButtonDock.BAR,
    val sizeDp: Int = 44,
    val opacityPercent: Int = 90,
    val xFraction: Float = 0.04f,
    val yFraction: Float = 0.3f,
)

@Serializable
data class QuickActionSettings(
    val learningToggle: QuickButtonSettings = QuickButtonSettings(),
    val translationToggle: QuickButtonSettings = QuickButtonSettings(),
    val shadowRepeat: QuickButtonSettings = QuickButtonSettings(dock = ButtonDock.BAR),
    val aiButton: QuickButtonSettings = QuickButtonSettings(dock = ButtonDock.BAR),
    val subtitleTools: QuickButtonSettings = QuickButtonSettings(dock = ButtonDock.FLOATING),
)
