package com.melonityhub.sublearn.core.settings

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * JSON export/import and schema migrations (ENG-6). Import is forgiving about unknown keys and
 * strict about newer schema versions, which it refuses with a message instead of guessing.
 */
object SettingsCodec {
    const val CURRENT_VERSION = 1

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    /** One migration per source version: step N turns a version-N document into version N+1. */
    private val migrations: Map<Int, (JsonObject) -> JsonObject> = mapOf(
        // 0 = documents exported before schemaVersion existed (none shipped, kept for the mechanism).
        0 to { obj: JsonObject -> JsonObject(obj + ("schemaVersion" to JsonPrimitive(1))) },
    )

    fun encode(settings: AppSettings): String =
        json.encodeToString(AppSettings.serializer(), settings.sanitized().copy(schemaVersion = CURRENT_VERSION))

    fun decode(text: String): Result<AppSettings> = runCatching {
        val raw = json.parseToJsonElement(text).jsonObject
        val version = raw["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 0
        require(version <= CURRENT_VERSION) {
            "This settings file was made by a newer version of SubLearn (schema $version); update the app to import it."
        }
        require(version >= 0) { "Invalid schema version $version" }
        json.decodeFromJsonElement(AppSettings.serializer(), migrate(raw, version)).sanitized()
    }

    internal fun migrate(document: JsonObject, fromVersion: Int): JsonObject {
        var current = document
        for (version in fromVersion until CURRENT_VERSION) {
            val step = migrations[version] ?: error("No settings migration from schema $version")
            current = step(current)
        }
        return current
    }
}
