package com.melonityhub.sublearn.core.translation

/**
 * Translation boundary (ENG-2, D-018). NOW implemented by [MlKitTranslationProvider] using Google ML Kit
 * on-device translation. Language tags are BCP-47 primary tags such as "en" and "fa".
 */
interface TranslationProvider {
    suspend fun translate(text: String, from: String, to: String): TranslationResult

    /** Downloads the language models for the pair if they are not on the device yet. */
    suspend fun prepareModels(from: String, to: String)
}

data class TranslationResult(val text: String, val fromCache: Boolean)

class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
