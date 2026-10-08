package com.melonityhub.sublearn.core.settings

import kotlinx.serialization.Serializable

@Serializable enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

/** App UI languages (GEN-3/OTH-5). English is the default; Persian is complete. */
@Serializable enum class UiLanguage(val tag: String) { ENGLISH("en"), PERSIAN("fa") }

/**
 * Decoder mapping (D-012). SW = software-only codecs, HW = hardware-only codecs,
 * HW_PLUS = hardware first with software fallback.
 */
@Serializable enum class DecoderMode { SW, HW, HW_PLUS }

@Serializable enum class OrientationMode { AUTO, PORTRAIT, LANDSCAPE }

@Serializable enum class AspectMode { FIT, FILL, ZOOM, STRETCH, RATIO_16_9, RATIO_4_3, ORIGINAL }

/** Actions a gesture can trigger (PLY-4). Every gesture slot is remappable. */
@Serializable enum class GestureAction {
    NONE, BRIGHTNESS, VOLUME, SEEK, PLAY_PAUSE, SPEED_UP, SPEED_DOWN, REPEAT_BLOCK, PREVIOUS_BLOCK, NEXT_BLOCK,
}

@Serializable enum class DoubleTapAction { PAUSE, SEEK }

@Serializable enum class LearningMode { ENTERTAINMENT, LEARNING }

@Serializable enum class DetailsSource { GOOGLE_TRANSLATE, OFFLINE_DICTIONARY }

/** Where a quick-action button lives (SUB-2). */
@Serializable enum class ButtonDock { BAR, FLOATING, HIDDEN }

/** Surfaces that have their own font settings (GEN-3). */
@Serializable enum class FontSurface {
    APP_MENUS, LEARNING_SUBTITLES, TRANSLATION_SUBTITLES, TRANSLATION_POPUPS, WORD_CARDS, AI_ANSWERS,
}

/** Language roles that have their own font settings (GEN-3). */
@Serializable enum class LanguageRole { LEARNING, NATIVE }

@Serializable enum class FontFamilyChoice { SYSTEM, SANS_SERIF, SERIF, MONOSPACE, CURSIVE }

@Serializable enum class WordDecoration { NONE, UNDERLINE, DOTTED_UNDERLINE, BOX, BACKGROUND, BOLD, COLOR }
