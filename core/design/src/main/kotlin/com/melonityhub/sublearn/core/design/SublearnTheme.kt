package com.melonityhub.sublearn.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.melonityhub.sublearn.core.settings.ThemeMode

/** Light, dark and AMOLED themes (GEN-1, ENG-11). AMOLED uses a pure black background. */
@Composable
fun SublearnTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val background = when {
        !dark -> SublearnTokens.LightBackground
        themeMode == ThemeMode.AMOLED -> SublearnTokens.AmoledBackground
        else -> SublearnTokens.DarkBackground
    }
    val colors = if (dark) {
        darkColorScheme(
            primary = SublearnTokens.Accent,
            onPrimary = SublearnTokens.DarkBackground,
            background = background,
            onBackground = SublearnTokens.DarkOnSurface,
            surface = if (themeMode == ThemeMode.AMOLED) SublearnTokens.AmoledBackground else SublearnTokens.DarkPanel,
            onSurface = SublearnTokens.DarkOnSurface,
            surfaceVariant = SublearnTokens.DarkCard,
            onSurfaceVariant = SublearnTokens.DarkOnSurfaceMuted,
            outline = SublearnTokens.DarkBorder,
            error = SublearnTokens.Danger,
        )
    } else {
        lightColorScheme(
            primary = SublearnTokens.AccentStrongLight,
            onPrimary = SublearnTokens.LightPanel,
            background = background,
            onBackground = SublearnTokens.LightOnSurface,
            surface = SublearnTokens.LightPanel,
            onSurface = SublearnTokens.LightOnSurface,
            surfaceVariant = SublearnTokens.LightBackground,
            onSurfaceVariant = SublearnTokens.LightOnSurfaceMuted,
            outline = SublearnTokens.LightBorder,
            error = SublearnTokens.Danger,
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
