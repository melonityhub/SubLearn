package com.melonityhub.sublearn.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

internal val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

private val parser = Json { ignoreUnknownKeys = true }

/** Sends [request] off the main thread and returns the parsed JSON body or a typed [AiException]. */
internal suspend fun OkHttpClient.executeForJson(request: Request): JsonElement = withContext(Dispatchers.IO) {
    val response = try {
        newCall(request).execute()
    } catch (e: IOException) {
        throw AiException.Network(e)
    }
    response.use { r ->
        val body = r.body?.string().orEmpty()
        if (!r.isSuccessful) throw AiException.Http(r.code, errorMessageOf(body))
        try {
            parser.parseToJsonElement(body)
        } catch (e: Exception) {
            throw AiException.BadResponse("body is not JSON (${e.javaClass.simpleName})")
        }
    }
}

/** Providers all return an `error.message` field on failure; fall back to a short body prefix. */
internal fun errorMessageOf(body: String): String {
    val message = try {
        Json.parseToJsonElement(body).jsonObject["error"]?.let { error ->
            (error as? JsonObject)?.get("message")?.jsonPrimitive?.content
        }
    } catch (_: Exception) {
        null
    }
    return (message ?: body.take(160)).ifBlank { "no details" }
}
