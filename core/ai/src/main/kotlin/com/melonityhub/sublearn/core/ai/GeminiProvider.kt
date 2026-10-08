package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.AiProviderId
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Gemini `generateContent` REST API (default provider, D-021). Key is sent in the `x-goog-api-key` header. */
class GeminiProvider(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta/",
) : AiProvider {
    override val id: AiProviderId = AiProviderId.GEMINI

    override suspend fun complete(request: AiRequest): AiAnswer {
        if (request.apiKey.isBlank()) throw AiException.MissingKey(id)
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("models/${request.model}:generateContent")
            .build()
        val payload = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject { put("text", request.prompt) }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("maxOutputTokens", request.maxOutputTokens)
            }
        }
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-goog-api-key", request.apiKey)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()
        val root = client.executeForJson(httpRequest).jsonObject
        val parts = root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject?.get("content")?.jsonObject?.get("parts")
            ?.jsonArray ?: throw AiException.BadResponse("no candidates")
        val text = parts.joinToString("") { part -> part.jsonObject["text"]?.jsonPrimitive?.content.orEmpty() }.trim()
        if (text.isEmpty()) throw AiException.BadResponse("empty answer")
        return AiAnswer(text, id, request.model)
    }
}
