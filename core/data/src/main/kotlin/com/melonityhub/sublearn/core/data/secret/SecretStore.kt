package com.melonityhub.sublearn.core.data.secret

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.melonityhub.sublearn.core.model.AiProviderId
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.secretDataStore: DataStore<Preferences> by preferencesDataStore(name = "sublearn_secrets")

/**
 * API keys (ENG-7, D-011). Each key is encrypted with AES-256-GCM using a non-exportable key held in
 * the Android Keystore; only ciphertext is written to DataStore. Keys are never logged, never part
 * of settings export, and backups are disabled by the manifest and data-extraction rules.
 */
class SecretStore(context: Context) {
    private val store = context.applicationContext.secretDataStore

    suspend fun save(provider: AiProviderId, apiKey: String) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            clear(provider)
            return
        }
        val encrypted = encrypt(trimmed.toByteArray(Charsets.UTF_8))
        store.edit { it[keyFor(provider)] = encrypted }
    }

    suspend fun load(provider: AiProviderId): String? {
        val stored = store.data.first()[keyFor(provider)] ?: return null
        return runCatching { String(decrypt(stored), Charsets.UTF_8) }.getOrNull()
    }

    suspend fun hasKey(provider: AiProviderId): Boolean = load(provider)?.isNotEmpty() == true

    suspend fun clear(provider: AiProviderId) {
        store.edit { it.remove(keyFor(provider)) }
    }

    private fun keyFor(provider: AiProviderId) = stringPreferencesKey("ai_key_${provider.name.lowercase()}")

    private fun encrypt(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain)
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText)
    }

    private fun decrypt(stored: String): ByteArray {
        val (ivPart, dataPart) = stored.split(':', limit = 2).let { it[0] to it[1] }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_BITS, Base64.getDecoder().decode(ivPart)))
        return cipher.doFinal(Base64.getDecoder().decode(dataPart))
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "sublearn_api_keys_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
