package com.melonityhub.sublearn.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.melonityhub.sublearn.core.design.SublearnTokens
import com.melonityhub.sublearn.core.model.AiProviderId
import com.melonityhub.sublearn.core.model.CefrLevel
import com.melonityhub.sublearn.core.model.shadowing.ShadowingFormula
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.AspectMode
import com.melonityhub.sublearn.core.settings.DecoderMode
import com.melonityhub.sublearn.core.settings.DetailsSource
import com.melonityhub.sublearn.core.settings.DoubleTapAction
import com.melonityhub.sublearn.core.settings.FontFamilyChoice
import com.melonityhub.sublearn.core.settings.FontSettings
import com.melonityhub.sublearn.core.settings.FontSurface
import com.melonityhub.sublearn.core.settings.LanguageRole
import com.melonityhub.sublearn.core.settings.LearningMode
import com.melonityhub.sublearn.core.settings.TextStyleSpec
import com.melonityhub.sublearn.core.settings.ThemeMode
import com.melonityhub.sublearn.core.settings.UiLanguage
import com.melonityhub.sublearn.feature.settings.R
import org.koin.androidx.compose.koinViewModel

/** The settings categories (ENG-6). Each one is a searchable, grouped screen. */
enum class SettingsCategory(val labelRes: Int, val keywords: String) {
    APPEARANCE(R.string.cat_appearance, "theme language dark light amoled"),
    PLAYER(R.string.cat_player, "decoder hardware software speed double tap controls pip resume"),
    SUBTITLES(R.string.cat_subtitles, "subtitle layer opacity block merge charset delay"),
    FONTS(R.string.cat_fonts, "font family size weight color surface role"),
    SHADOWING(R.string.cat_shadowing, "repeat pause formula multiplier block"),
    LEARNING(R.string.cat_learning, "level mode popup learning entertainment"),
    AI(R.string.cat_ai, "ai gemini chatgpt openai claude anthropic key prompt context"),
    DICTIONARY(R.string.cat_dictionary, "dictionary details google offline"),
    ABOUT(R.string.cat_about, "about version export import backup licence license"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = koinViewModel()
    val settings by viewModel.settings.collectAsState()
    var query by remember { mutableStateOf("") }
    val labels = SettingsCategory.entries.associateWith { stringResource(it.labelRes) }
    var open by remember { mutableStateOf<SettingsCategory?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (open != null) open = null else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                    }
                },
            )
        },
    ) { padding ->
        val category = open
        if (category == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(SublearnTokens.SpaceL)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.settings_search)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                val filtered = SettingsCategory.entries.filter {
                    query.isBlank() || it.keywords.contains(query.trim(), ignoreCase = true) ||
                        labels.getValue(it).contains(query.trim(), ignoreCase = true)
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    filtered.forEach { item ->
                        OutlinedButton(
                            onClick = { open = item },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        ) { Text(stringResource(item.labelRes)) }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(SublearnTokens.SpaceL),
                verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceM),
            ) {
                when (category) {
                    SettingsCategory.APPEARANCE -> AppearanceSection(settings, viewModel)
                    SettingsCategory.PLAYER -> PlayerSection(settings, viewModel)
                    SettingsCategory.SUBTITLES -> SubtitleSection(settings, viewModel)
                    SettingsCategory.FONTS -> FontSection(settings, viewModel)
                    SettingsCategory.SHADOWING -> ShadowingSection(settings, viewModel)
                    SettingsCategory.LEARNING -> LearningSection(settings, viewModel)
                    SettingsCategory.AI -> AiSection(settings, viewModel)
                    SettingsCategory.DICTIONARY -> DictionarySection(settings, viewModel)
                    SettingsCategory.ABOUT -> AboutSection(viewModel)
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_appearance)
    ChoiceRow(R.string.theme_label, ThemeMode.entries, settings.appearance.themeMode, { themeLabel(it) }) { mode ->
        vm.update { it.copy(appearance = it.appearance.copy(themeMode = mode)) }
    }
    ChoiceRow(R.string.ui_language_label, UiLanguage.entries, settings.appearance.uiLanguage, { languageLabel(it) }) { lang ->
        vm.update { it.copy(appearance = it.appearance.copy(uiLanguage = lang)) }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(lang.tag),
        )
    }
}

@Composable
private fun PlayerSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_player)
    ChoiceRow(R.string.decoder_label, DecoderMode.entries, settings.player.decoderMode, { decoderLabel(it) }) { mode ->
        vm.update { it.copy(player = it.player.copy(decoderMode = mode)) }
    }
    ChoiceRow(R.string.double_tap_label, DoubleTapAction.entries, settings.player.doubleTapAction, { doubleTapLabel(it) }) { action ->
        vm.update { it.copy(player = it.player.copy(doubleTapAction = action)) }
    }
    SliderRow(R.string.seek_step_label, settings.player.seekStepSeconds.toFloat(), 1f..60f, "${settings.player.seekStepSeconds} s") { value ->
        vm.update { it.copy(player = it.player.copy(seekStepSeconds = value.toInt())) }
    }
    SliderRow(R.string.autohide_label, (settings.player.controlsAutoHideMs / 1000f), 1f..15f, "${settings.player.controlsAutoHideMs / 1000} s") { value ->
        vm.update { it.copy(player = it.player.copy(controlsAutoHideMs = (value * 1000).toLong())) }
    }
    SwitchRow(R.string.resume_label, settings.player.resumeFromLastPosition) { on ->
        vm.update { it.copy(player = it.player.copy(resumeFromLastPosition = on)) }
    }
    SwitchRow(R.string.pip_label, settings.player.pictureInPictureOnLeave) { on ->
        vm.update { it.copy(player = it.player.copy(pictureInPictureOnLeave = on)) }
    }
    ChoiceRow(R.string.aspect_label, listOf(AspectMode.FIT, AspectMode.FILL, AspectMode.STRETCH), settings.player.aspectMode, { aspectLabel(it) }) { mode ->
        vm.update { it.copy(player = it.player.copy(aspectMode = mode)) }
    }
}

@Composable
private fun SubtitleSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_subtitles)
    SliderRow(R.string.opacity_learning, settings.subtitles.learningLayer.opacityPercent.toFloat(), 0f..100f, "${settings.subtitles.learningLayer.opacityPercent}%") { value ->
        vm.update { it.copy(subtitles = it.subtitles.copy(learningLayer = it.subtitles.learningLayer.copy(opacityPercent = value.toInt()))) }
    }
    SliderRow(R.string.opacity_translation, settings.subtitles.translationLayer.opacityPercent.toFloat(), 0f..100f, "${settings.subtitles.translationLayer.opacityPercent}%") { value ->
        vm.update { it.copy(subtitles = it.subtitles.copy(translationLayer = it.subtitles.translationLayer.copy(opacityPercent = value.toInt()))) }
    }
    SliderRow(R.string.delay_learning, settings.subtitles.learningLayer.delayMs.toFloat(), -5000f..5000f, "${settings.subtitles.learningLayer.delayMs} ms") { value ->
        vm.update { it.copy(subtitles = it.subtitles.copy(learningLayer = it.subtitles.learningLayer.copy(delayMs = value.toLong()))) }
    }
    SwitchRow(R.string.merge_blocks, settings.subtitles.mergeCuesIntoBlocks) { on ->
        vm.update { it.copy(subtitles = it.subtitles.copy(mergeCuesIntoBlocks = on)) }
    }
    ChoiceRow(R.string.charset_label, listOf("windows-1256", "windows-1252", "ISO-8859-6"), settings.subtitles.legacyCharset, { it }) { name ->
        vm.update { it.copy(subtitles = it.subtitles.copy(legacyCharset = name)) }
    }
    SliderRow(R.string.max_chars_label, settings.subtitles.blockMaxChars.toFloat(), 20f..300f, "${settings.subtitles.blockMaxChars}") { value ->
        vm.update { it.copy(subtitles = it.subtitles.copy(blockMaxChars = value.toInt())) }
    }
}

@Composable
private fun FontSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_fonts)
    Text(stringResource(R.string.fonts_pending_note), style = MaterialTheme.typography.bodySmall)
    WIRED_FONT_SURFACES.forEach { surface ->
        Text(stringResource(surfaceLabel(surface)), style = MaterialTheme.typography.titleSmall)
        LanguageRole.entries.forEach { role ->
            val style = settings.fonts.resolve(surface, role)
            ChoiceRow(
                if (role == LanguageRole.LEARNING) R.string.role_learning else R.string.role_native,
                FontFamilyChoice.entries,
                style.family,
                { familyLabel(it) },
            ) { family ->
                vm.update { it.copy(fonts = it.fonts.withStyle(surface, role, style.copy(family = family))) }
            }
            SliderRow(R.string.font_size_label, style.sizeSp.toFloat(), 8f..40f, "${style.sizeSp} sp") { value ->
                vm.update { it.copy(fonts = it.fonts.withStyle(surface, role, style.copy(sizeSp = value.toInt()))) }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ShadowingSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_shadowing)
    SliderRow(R.string.repeat_count_label, settings.shadowing.repeatCount.toFloat(), -1f..10f, if (settings.shadowing.repeatCount < 0) "auto" else "${settings.shadowing.repeatCount}") { value ->
        vm.update { it.copy(shadowing = it.shadowing.copy(repeatCount = value.toInt())) }
    }
    SliderRow(R.string.multiplier_label, settings.shadowing.pauseMultiplier.toFloat(), 0f..3f, String.format("%.2f", settings.shadowing.pauseMultiplier)) { value ->
        vm.update { it.copy(shadowing = it.shadowing.copy(pauseMultiplier = value.toDouble())) }
    }
    var formula by remember(settings.shadowing.pauseFormula) { mutableStateOf(settings.shadowing.pauseFormula) }
    val error = ShadowingFormula.validate(formula)
    OutlinedTextField(
        value = formula,
        onValueChange = {
            formula = it
            if (ShadowingFormula.validate(it) == null) vm.update { s -> s.copy(shadowing = s.shadowing.copy(pauseFormula = it)) }
        },
        label = { Text(stringResource(R.string.formula_label)) },
        supportingText = { Text(error ?: stringResource(R.string.formula_help)) },
        isError = error != null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    SwitchRow(R.string.stop_at_end_default, settings.shadowing.stopAtBlockEnd) { on ->
        vm.update { it.copy(shadowing = it.shadowing.copy(stopAtBlockEnd = on)) }
    }
}

@Composable
private fun LearningSection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_learning)
    Text(stringResource(R.string.learning_pending_note), style = MaterialTheme.typography.bodySmall)
    ChoiceRow(R.string.mode_label, LearningMode.entries, settings.learning.mode, { modeLabel(it) }) { mode ->
        vm.update { it.copy(learning = it.learning.copy(mode = mode)) }
    }
    ChoiceRow(R.string.level_label, CefrLevel.entries.toList(), CefrLevel.fromName(settings.learning.manualLevel) ?: CefrLevel.B1, { it.name }) { level ->
        vm.update { it.copy(learning = it.learning.copy(manualLevel = level.name)) }
    }
    SliderRow(R.string.popups_label, settings.learning.maxPopupsPerBlock.toFloat(), 0f..10f, "${settings.learning.maxPopupsPerBlock}") { value ->
        vm.update { it.copy(learning = it.learning.copy(maxPopupsPerBlock = value.toInt())) }
    }
}

@Composable
private fun AiSection(settings: AppSettings, vm: SettingsViewModel) {
    val status by vm.keyStatus.collectAsState()
    SectionTitle(R.string.cat_ai)
    ChoiceRow(R.string.ai_provider_label, AiProviderId.entries, settings.ai.provider, { providerLabel(it) }) { provider ->
        vm.update { it.copy(ai = it.ai.copy(provider = provider)) }
    }
    AiProviderId.entries.forEach { provider ->
        KeyRow(provider, saved = status[provider] == true, onSave = { vm.saveKey(provider, it) }, onClear = { vm.clearKey(provider) })
    }
    SliderRow(R.string.context_blocks_label, settings.ai.contextBlocks.toFloat(), 0f..50f, "${settings.ai.contextBlocks}") { value ->
        vm.update { it.copy(ai = it.ai.copy(contextBlocks = value.toInt())) }
    }
    var prompt by remember(settings.ai.promptTemplate) { mutableStateOf(settings.ai.promptTemplate) }
    OutlinedTextField(
        value = prompt,
        onValueChange = {
            prompt = it
            vm.update { s -> s.copy(ai = s.ai.copy(promptTemplate = it)) }
        },
        label = { Text(stringResource(R.string.prompt_label)) },
        supportingText = { Text(stringResource(R.string.prompt_help)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
    )
}

@Composable
private fun KeyRow(provider: AiProviderId, saved: Boolean, onSave: (String) -> Unit, onClear: () -> Unit) {
    var key by remember(provider) { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceXs)) {
        Text(
            stringResource(R.string.key_label, providerLabel(provider)) + " — " +
                stringResource(if (saved) R.string.key_saved else R.string.key_not_saved),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text(stringResource(R.string.key_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
            Button(onClick = { onSave(key); key = "" }, enabled = key.isNotBlank()) { Text(stringResource(R.string.key_save)) }
            OutlinedButton(onClick = onClear, enabled = saved) { Text(stringResource(R.string.key_remove)) }
        }
    }
}

@Composable
private fun DictionarySection(settings: AppSettings, vm: SettingsViewModel) {
    SectionTitle(R.string.cat_dictionary)
    Text(stringResource(R.string.dictionary_google_note), style = MaterialTheme.typography.bodyMedium)
    FilterChip(
        selected = settings.translation.detailsSource == DetailsSource.GOOGLE_TRANSLATE,
        onClick = { vm.update { it.copy(translation = it.translation.copy(detailsSource = DetailsSource.GOOGLE_TRANSLATE)) } },
        label = { Text(stringResource(R.string.dictionary_google)) },
    )
    FilterChip(
        selected = false,
        onClick = {},
        enabled = false,
        label = { Text(stringResource(R.string.coming_soon)) },
    )
}

@Composable
private fun AboutSection(vm: SettingsViewModel) {
    val message by vm.message.collectAsState()
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            val json = vm.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            vm.showMessage(exportDone)
        }
    }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            if (text != null) vm.importJson(text) else vm.showMessage(importFailed)
        }
    }
    SectionTitle(R.string.cat_about)
    Text(stringResource(R.string.about_text))
    Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceS)) {
        Button(onClick = { exportLauncher.launch("sublearn-settings.json") }) { Text(stringResource(R.string.export_label)) }
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) { Text(stringResource(R.string.import_label)) }
    }
    message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
}

/** Font surfaces that the app renders today. The others are listed in KNOWN_ISSUES until their screens exist. */
private val WIRED_FONT_SURFACES = listOf(
    FontSurface.LEARNING_SUBTITLES,
    FontSurface.TRANSLATION_SUBTITLES,
    FontSurface.TRANSLATION_POPUPS,
)

private fun aspectLabel(mode: AspectMode): String = when (mode) {
    AspectMode.FIT -> "Fit"
    AspectMode.FILL -> "Fill"
    AspectMode.STRETCH -> "Stretch"
    else -> mode.name
}

@Composable
private fun SectionTitle(res: Int) {
    Text(stringResource(res), style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun <T> ChoiceRow(labelRes: Int, options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceXs)) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceXs)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(labelRes: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(labelRes), modifier = Modifier.padding(end = SublearnTokens.SpaceS))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SliderRow(labelRes: Int, value: Float, range: ClosedFloatingPointRange<Float>, valueText: String, onChange: (Float) -> Unit) {
    Column {
        Text("${stringResource(labelRes)}: $valueText", style = MaterialTheme.typography.labelLarge)
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.AMOLED -> R.string.theme_amoled
    },
)

private fun languageLabel(lang: UiLanguage): String = if (lang == UiLanguage.PERSIAN) "فارسی" else "English"

@Composable
private fun decoderLabel(mode: DecoderMode): String = stringResource(
    when (mode) {
        DecoderMode.SW -> R.string.player_decoder_sw
        DecoderMode.HW -> R.string.player_decoder_hw
        DecoderMode.HW_PLUS -> R.string.player_decoder_hw_plus
    },
)

@Composable
private fun doubleTapLabel(action: DoubleTapAction): String = stringResource(
    if (action == DoubleTapAction.PAUSE) R.string.double_tap_pause else R.string.double_tap_seek,
)

@Composable
private fun modeLabel(mode: LearningMode): String = stringResource(
    if (mode == LearningMode.ENTERTAINMENT) R.string.mode_entertainment else R.string.mode_learning,
)

private fun providerLabel(provider: AiProviderId): String = when (provider) {
    AiProviderId.GEMINI -> "Gemini"
    AiProviderId.OPENAI -> "ChatGPT (OpenAI)"
    AiProviderId.ANTHROPIC -> "Claude (Anthropic)"
}

@Composable
private fun familyLabel(family: FontFamilyChoice): String = stringResource(
    when (family) {
        FontFamilyChoice.SYSTEM -> R.string.font_system
        FontFamilyChoice.SANS_SERIF -> R.string.font_sans
        FontFamilyChoice.SERIF -> R.string.font_serif
        FontFamilyChoice.MONOSPACE -> R.string.font_mono
        FontFamilyChoice.CURSIVE -> R.string.font_cursive
    },
)

private fun surfaceLabel(surface: FontSurface): Int = when (surface) {
    FontSurface.APP_MENUS -> R.string.surface_app_menus
    FontSurface.LEARNING_SUBTITLES -> R.string.surface_learning_subtitles
    FontSurface.TRANSLATION_SUBTITLES -> R.string.surface_translation_subtitles
    FontSurface.TRANSLATION_POPUPS -> R.string.surface_translation_popups
    FontSurface.WORD_CARDS -> R.string.surface_word_cards
    FontSurface.AI_ANSWERS -> R.string.surface_ai_answers
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringResource(id: Int, vararg args: Any): String = androidx.compose.ui.res.stringResource(id, *args)
