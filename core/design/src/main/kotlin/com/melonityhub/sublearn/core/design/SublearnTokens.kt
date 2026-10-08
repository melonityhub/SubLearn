package com.melonityhub.sublearn.core.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens (GEN-1, ENG-11). Every colour, radius, spacing and duration used by feature code
 * must come from here. Palette direction: dark translucent panels with an amber highlight, which was
 * re-implemented from the visual idea of proudvocab (no code or assets copied; see REFERENCES.md).
 */
object SublearnTokens {
    // Dark theme
    val DarkBackground = Color(0xFF0E1322)
    val DarkPanel = Color(0xFF131A2C)
    val DarkCard = Color(0xFF1B2438)
    val DarkBorder = Color(0xFF2B3752)
    val DarkOnSurface = Color(0xFFE8ECF7)
    val DarkOnSurfaceMuted = Color(0xFF9AA6C4)
    val AmoledBackground = Color(0xFF000000)

    // Light theme
    val LightBackground = Color(0xFFF6F7FB)
    val LightPanel = Color(0xFFFFFFFF)
    val LightBorder = Color(0xFFD6DCEB)
    val LightOnSurface = Color(0xFF1A2236)
    val LightOnSurfaceMuted = Color(0xFF55607C)

    // Accents (amber highlight, used for the current subtitle line and My Words)
    val Accent = Color(0xFFFFC878)
    val AccentSoft = Color(0x33FFC878)
    /** Darker amber for text and controls on light backgrounds (contrast). */
    val AccentStrongLight = Color(0xFF9A5B00)
    val Danger = Color(0xFFFF6B6B)

    /** Subtitle background scrim behind learning and translation text (alpha applied from settings). */
    val SubtitleScrim = Color(0xFF000000)

    val CornerSmall = 8.dp
    val CornerMedium = 12.dp
    val CornerLarge = 20.dp

    val SpaceXs = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXl = 24.dp

    /** Smallest touch target (Material guidance is 48dp; quick actions use the 44dp setting minimum). */
    val MinTouchTarget = 48.dp

    const val DurationFastMs = 150
    const val DurationMediumMs = 260
    const val DurationSlowMs = 420
    const val PopupRiseMs = 900

    /** Standard easing for overlays and panels. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
