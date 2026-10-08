package com.melonityhub.sublearn.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.SettingsCodec
import com.melonityhub.sublearn.core.settings.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sublearn_settings")

/**
 * DataStore implementation of [SettingsStore] (D-010). The typed document is stored as one JSON
 * string, so every write goes through the same versioned codec used for export and import.
 * A stored document that cannot be read falls back to defaults rather than crashing the app.
 */
class DataStoreSettingsStore(context: Context) : SettingsStore {
    private val store = context.applicationContext.settingsDataStore
    private val documentKey = stringPreferencesKey("settings_document")

    override val settings: Flow<AppSettings> = store.data
        .map { prefs -> prefs[documentKey]?.let { SettingsCodec.decode(it).getOrNull() } ?: AppSettings() }
        .distinctUntilChanged()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { prefs ->
            val current = prefs[documentKey]?.let { SettingsCodec.decode(it).getOrNull() } ?: AppSettings()
            prefs[documentKey] = SettingsCodec.encode(transform(current))
        }
    }

    override suspend fun replace(settings: AppSettings) {
        store.edit { prefs -> prefs[documentKey] = SettingsCodec.encode(settings) }
    }
}
