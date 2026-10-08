package com.melonityhub.sublearn.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonityhub.sublearn.core.data.secret.SecretStore
import com.melonityhub.sublearn.core.model.AiProviderId
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.SettingsCodec
import com.melonityhub.sublearn.core.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings state and actions. Keys go to [SecretStore] only; the settings document never holds them. */
class SettingsViewModel(
    private val store: SettingsStore,
    private val secrets: SecretStore,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _keyStatus = MutableStateFlow<Map<AiProviderId, Boolean>>(emptyMap())
    val keyStatus: StateFlow<Map<AiProviderId, Boolean>> = _keyStatus

    /** Message for the last import/export action, shown as a snackbar. */
    val message = MutableStateFlow<String?>(null)

    init {
        refreshKeyStatus()
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { store.update(transform) }
    }

    fun saveKey(provider: AiProviderId, key: String) {
        viewModelScope.launch {
            secrets.save(provider, key)
            refreshKeyStatus()
        }
    }

    fun clearKey(provider: AiProviderId) {
        viewModelScope.launch {
            secrets.clear(provider)
            refreshKeyStatus()
        }
    }

    fun exportJson(): String = SettingsCodec.encode(settings.value)

    fun importJson(text: String) {
        viewModelScope.launch {
            SettingsCodec.decode(text)
                .onSuccess {
                    store.replace(it)
                    message.value = "Settings imported"
                }
                .onFailure { message.value = it.message ?: "The file could not be imported" }
        }
    }

    fun showMessage(text: String) {
        message.value = text
    }

    fun dismissMessage() {
        message.value = null
    }

    private fun refreshKeyStatus() {
        viewModelScope.launch {
            _keyStatus.value = AiProviderId.entries.associateWith { secrets.hasKey(it) }
        }
    }
}
