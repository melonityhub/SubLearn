package com.melonityhub.sublearn.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** A translucent dark panel with a hairline border (the "glass" direction of GEN-1). */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    alpha: Float = 0.78f,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(SublearnTokens.CornerMedium)
    Box(
        modifier = modifier
            .clip(shape)
            .background(SublearnTokens.DarkPanel.copy(alpha = alpha), shape)
            .border(1.dp, SublearnTokens.DarkBorder, shape)
            .padding(SublearnTokens.SpaceS),
    ) {
        content()
    }
}

/** Visible label for features that are planned but not built yet (LATER items). */
@Composable
fun ComingSoonBadge(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SublearnTokens.CornerSmall))
            .background(SublearnTokens.AccentSoft)
            .padding(horizontal = SublearnTokens.SpaceS, vertical = SublearnTokens.SpaceXs)
            .semantics { contentDescription = label },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Keeps the Color import used by callers that build their own translucent surfaces. */
fun scrimColor(alpha: Float): Color = SublearnTokens.SubtitleScrim.copy(alpha = alpha)

/** A border modifier helper so callers do not repeat the token values. */
fun Modifier.hairlineBorder(): Modifier = border(1.dp, SublearnTokens.DarkBorder, RoundedCornerShape(SublearnTokens.CornerSmall))
