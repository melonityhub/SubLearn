package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.AiProviderId
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

/** OpenAI Chat Completions API ("ChatGPT", AI-1). Bearer auth. No token cap is sent: newer models reject some parameters. */
class OpenAiProvider(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://api.openai.com/v1/",
) : AiProvider {
    override val id: AiProviderId = AiProviderId.OPENAI

    override suspend fun complete(request: AiRequest): AiAnswer {
        if (request.apiKey.isBlank()) throw AiException.MissingKey(id)
        val url = baseUrl.toHttpUrl().newBuilder().addPathSegments("chat/completions").build()
        val payload = buildJsonObject {
            put("model", request.model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", request.prompt)
                }
            }
        }
        val httpRequest = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${request.apiKey}")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()
        val root = client.executeForJson(httpRequest).jsonObject
        val content = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content?.trim()
            ?: throw AiException.BadResponse("no choices")
        if (content.isEmpty()) throw AiException.BadResponse("empty answer")
        return AiAnswer(content, id, request.model)
    }
}
