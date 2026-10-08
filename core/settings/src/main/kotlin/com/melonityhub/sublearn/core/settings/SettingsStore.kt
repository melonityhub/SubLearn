package com.melonityhub.sublearn.core.settings

import kotlinx.coroutines.flow.Flow

/** Persistence boundary for settings. The DataStore implementation lives in core:data. */
interface SettingsStore {
    val settings: Flow<AppSettings>

    /** Applies [transform] atomically to the stored settings. */
    suspend fun update(transform: (AppSettings) -> AppSettings)

    /** Replaces everything (used by import). */
    suspend fun replace(settings: AppSettings)
}
