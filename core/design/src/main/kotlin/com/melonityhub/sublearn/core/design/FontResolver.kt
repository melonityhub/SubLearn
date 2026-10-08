package com.melonityhub.sublearn.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.melonityhub.sublearn.core.settings.FontFamilyChoice
import com.melonityhub.sublearn.core.settings.TextStyleSpec

/**
 * Converts persisted font choices to Compose text styles (GEN-3). Each (surface, role) pair resolves
 * through its own [TextStyleSpec], so one surface never inherits another's font.
 */
object FontResolver {
    fun familyOf(choice: FontFamilyChoice): FontFamily = when (choice) {
        FontFamilyChoice.SYSTEM -> FontFamily.Default
        FontFamilyChoice.SANS_SERIF -> FontFamily.SansSerif
        FontFamilyChoice.SERIF -> FontFamily.Serif
        FontFamilyChoice.MONOSPACE -> FontFamily.Monospace
        FontFamilyChoice.CURSIVE -> FontFamily.Cursive
    }

    /**
     * [themeColor] is used when the spec has no explicit colour. Direction is content-based, so
     * Persian lines render right-to-left and English lines left-to-right inside the same layer (GEN-4).
     */
    fun toTextStyle(spec: TextStyleSpec, themeColor: Color): TextStyle = TextStyle(
        fontFamily = familyOf(spec.family),
        fontSize = spec.sizeSp.sp,
        fontWeight = FontWeight(spec.weight.coerceIn(100, 900)),
        fontStyle = if (spec.italic) FontStyle.Italic else FontStyle.Normal,
        color = spec.colorArgb?.let { Color(it) } ?: themeColor,
        textDirection = TextDirection.Content,
    )
}
