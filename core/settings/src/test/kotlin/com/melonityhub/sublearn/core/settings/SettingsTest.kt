package com.melonityhub.sublearn.core.settings

import com.melonityhub.sublearn.core.model.AiProviderId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {

    @Test
    fun roundTripPreservesEveryField() {
        val original = AppSettings(
            appearance = AppearanceSettings(themeMode = ThemeMode.AMOLED, uiLanguage = UiLanguage.PERSIAN),
            shadowing = ShadowingSettings(repeatCount = -1, pauseFormula = "D * N"),
            ai = AiSettings(provider = AiProviderId.ANTHROPIC, contextBlocks = 4),
        )
        val decoded = SettingsCodec.decode(SettingsCodec.encode(original)).getOrThrow()
        assertEquals(original, decoded)
    }

    @Test
    fun legacyDocumentWithoutVersionMigratesAndFillsDefaults() {
        val legacy = """{"appearance":{"themeMode":"DARK"},"unknownFutureKey":123}"""
        val decoded = SettingsCodec.decode(legacy).getOrThrow()
        assertEquals(ThemeMode.DARK, decoded.appearance.themeMode)
        assertEquals(SettingsCodec.CURRENT_VERSION, decoded.schemaVersion)
        assertEquals(PlayerSettings(), decoded.player)
    }

    @Test
    fun newerSchemaIsRefusedWithAMessage() {
        val result = SettingsCodec.decode("""{"schemaVersion":99}""")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("newer version"))
    }

    @Test
    fun malformedJsonFailsWithoutThrowingOutOfTheCodec() {
        assertTrue(SettingsCodec.decode("{not json").isFailure)
    }

    @Test
    fun sanitizedClampsHandEditedValues() {
        val wild = AppSettings(
            player = PlayerSettings(defaultSpeed = 10f, seekStepSeconds = 0),
            shadowing = ShadowingSettings(repeatCount = -7, pauseMultiplier = 99.0),
            ai = AiSettings(contextBlocks = 999),
        ).sanitized()
        assertEquals(4f, wild.player.defaultSpeed, 0f)
        assertEquals(1, wild.player.seekStepSeconds)
        assertEquals(-1, wild.shadowing.repeatCount)
        assertEquals(5.0, wild.shadowing.pauseMultiplier, 0.0)
        assertEquals(50, wild.ai.contextBlocks)
    }

    @Test
    fun fontsAreIndependentPerRoleAndSurface() {
        val base = FontSettings()
        val tuned = base.withStyle(
            FontSurface.AI_ANSWERS,
            LanguageRole.NATIVE,
            TextStyleSpec(family = FontFamilyChoice.SERIF, sizeSp = 22),
        )
        assertEquals(22, tuned.resolve(FontSurface.AI_ANSWERS, LanguageRole.NATIVE).sizeSp)
        // Nothing else may change: other surfaces and the other role keep their defaults.
        assertEquals(FontSettings.defaultFor(FontSurface.AI_ANSWERS, LanguageRole.LEARNING), tuned.resolve(FontSurface.AI_ANSWERS, LanguageRole.LEARNING))
        assertEquals(FontSettings.defaultFor(FontSurface.LEARNING_SUBTITLES, LanguageRole.NATIVE), tuned.resolve(FontSurface.LEARNING_SUBTITLES, LanguageRole.NATIVE))
        assertEquals(FontFamilyChoice.SANS_SERIF, base.resolve(FontSurface.WORD_CARDS, LanguageRole.LEARNING).family)
        assertEquals(FontFamilyChoice.SYSTEM, base.resolve(FontSurface.WORD_CARDS, LanguageRole.NATIVE).family)
    }

    @Test
    fun exportNeverContainsSecretFields() {
        val json = SettingsCodec.encode(AppSettings(ai = AiSettings(promptTemplate = "p")))
        assertFalse(json.contains("apiKey", ignoreCase = true))
        assertFalse(json.contains("api_key", ignoreCase = true))
    }
}
