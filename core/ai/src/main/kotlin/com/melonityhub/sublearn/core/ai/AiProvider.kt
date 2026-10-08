package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.AiProviderId

/** One request to a model. [apiKey] is passed per call and is never logged or stored here. */
data class AiRequest(
    val prompt: String,
    val model: String,
    val apiKey: String,
    val maxOutputTokens: Int = 1_024,
)

data class AiAnswer(val text: String, val provider: AiProviderId, val model: String)

/** Failures are typed so the UI can explain them. Messages never include the API key. */
sealed class AiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class MissingKey(provider: AiProviderId) : AiException("No API key is saved for ${provider.name}")
    class Http(val code: Int, val detail: String) : AiException("The AI provider returned HTTP $code: $detail")
    class Network(cause: Throwable) : AiException("Network error while contacting the AI provider", cause)
    class BadResponse(detail: String) : AiException("Unexpected response from the AI provider: $detail")
}

/** Abstraction over Gemini, OpenAI and Anthropic (ENG-2, AI-1). */
interface AiProvider {
    val id: AiProviderId
    suspend fun complete(request: AiRequest): AiAnswer
}
