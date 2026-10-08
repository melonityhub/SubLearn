package com.melonityhub.sublearn.core.translation

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/**
 * On-device translation through the official Google ML Kit Translation API (D-018). ML Kit downloads
 * its own models; SubLearn never ships or extracts model files. After the first download, translation
 * works offline (GEN-7).
 */
class MlKitTranslationProvider(
    private val cache: TranslationCache = TranslationCache(capacity = 512),
) : TranslationProvider {

    private val translators = ConcurrentHashMap<String, Translator>()

    override suspend fun translate(text: String, from: String, to: String): TranslationResult {
        val clean = text.trim()
        if (clean.isEmpty()) return TranslationResult("", fromCache = true)
        val key = "$from>$to|$clean"
        cache.lookup(key)?.let { return TranslationResult(it, fromCache = true) }

        prepareModels(from, to)
        val translator = translatorFor(from, to)
        val result = try {
            translator.translate(clean).await()
        } catch (e: Exception) {
            throw TranslationException("Translation failed", e)
        }
        cache.store(key, result)
        return TranslationResult(result, fromCache = false)
    }

    override suspend fun prepareModels(from: String, to: String) {
        val translator = translatorFor(from, to)
        try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        } catch (e: Exception) {
            throw TranslationException("The $from-$to language model could not be downloaded", e)
        }
    }

    private fun translatorFor(from: String, to: String): Translator {
        val source = TranslateLanguage.fromLanguageTag(from)
            ?: throw TranslationException("Unsupported source language: $from")
        val target = TranslateLanguage.fromLanguageTag(to)
            ?: throw TranslationException("Unsupported target language: $to")
        return translators.getOrPut("$source>$target") {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build(),
            )
        }
    }
}
