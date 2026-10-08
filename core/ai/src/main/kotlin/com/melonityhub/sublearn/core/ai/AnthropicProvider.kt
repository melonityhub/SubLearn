package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.AiProviderId
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Anthropic Messages API ("Claude", AI-1). Key in `x-api-key`, version pinned in `anthropic-version`. */
class AnthropicProvider(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://api.anthropic.com/v1/",
) : AiProvider {
    override val id: AiProviderId = AiProviderId.ANTHROPIC

    override suspend fun complete(request: AiRequest): AiAnswer {
        if (request.apiKey.isBlank()) throw AiException.MissingKey(id)
        val url = baseUrl.toHttpUrl().newBuilder().addPathSegments("messages").build()
        val payload = buildJsonObject {
            put("model", request.model)
            put("max_tokens", request.maxOutputTokens)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", request.prompt)
                }
            }
        }
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-api-key", request.apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()
        val root = client.executeForJson(httpRequest)
        val blocks = root["content"]?.jsonArray ?: JsonArray(emptyList())
        val text = blocks
            .mapNotNull { block -> block.jsonObject.takeIf { it["type"]?.jsonPrimitive?.content == "text" } }
            .joinToString("") { it["text"]?.jsonPrimitive?.content.orEmpty() }
            .trim()
        if (text.isEmpty()) throw AiException.BadResponse("no text content")
        return AiAnswer(text, id, request.model)
    }

    companion object {
        const val ANTHROPIC_VERSION = "2023-06-01"
    }
}
