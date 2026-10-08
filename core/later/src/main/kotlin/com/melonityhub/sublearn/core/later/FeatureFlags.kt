package com.melonityhub.sublearn.core.later

/**
 * Feature flags for LATER items (docs/EXTENSION_POINTS.md). A flag that is false keeps the feature
 * hidden or shown as "Coming soon" with its controls disabled. Nothing behind a false flag runs.
 * Flags are compile-time constants on purpose: they are changed in code, never by a user setting.
 */
object FeatureFlags {
    const val YOUTUBE = false // LATER-1
    const val LEARN_SECTION = false // LATER-2
    const val DICTIONARY = false // LATER-3 (offline dictionary); NOW uses Google Translate details
    const val LEVEL_DETECTION = false // LATER-4
    const val QUIZ = false // LATER-5
    const val UPDATE_CHECKER = false // LATER-6
    const val AI_RESEGMENTATION = false // LATER-7
    const val SPEECH_TO_TEXT = false // LATER-8
    const val WORD_ANALYSIS = false // LATER-9
    const val ON_DEVICE_AI = false // LATER-10
    const val MORE_LANGUAGES = false // LATER-11
}
