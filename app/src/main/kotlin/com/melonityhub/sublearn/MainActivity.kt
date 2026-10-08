package com.melonityhub.sublearn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import com.melonityhub.sublearn.core.design.SublearnTheme
import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.settings.AppSettings
import com.melonityhub.sublearn.core.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

/**
 * The single activity. It opens straight into the player for `ACTION_VIEW` video files and links (APP-1),
 * otherwise shows the normal tabs. Edge-to-edge and predictive back are enabled (ENG-8).
 */
class MainActivity : AppCompatActivity() {

    private val externalOpen = MutableStateFlow<MediaSource?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            val store: SettingsStore = koinInject()
            val settings by store.settings.collectAsState(initial = AppSettings())
            // The app UI language follows the setting (English by default, GEN-3 / OTH-5).
            LaunchedEffect(settings.appearance.uiLanguage) {
                val wanted = settings.appearance.uiLanguage.tag
                if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != wanted) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(wanted))
                }
            }
            SublearnTheme(themeMode = settings.appearance.themeMode) {
                val open by externalOpen.collectAsState()
                AppRoot(
                    external = open,
                    onExternalConsumed = { externalOpen.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        externalOpen.value = MediaSource(uri = uri.toString(), title = displayNameOf(uri))
    }

    private fun displayNameOf(uri: Uri): String {
        runCatching {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.takeIf { it.isNotBlank() }?.let { return it }
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: getString(R.string.unlisted_title)
    }
}
