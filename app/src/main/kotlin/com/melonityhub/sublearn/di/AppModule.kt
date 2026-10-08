package com.melonityhub.sublearn.di

import androidx.room.Room
import com.melonityhub.sublearn.core.data.db.AppDatabase
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import com.melonityhub.sublearn.core.data.repo.PlaybackStateRepository
import com.melonityhub.sublearn.core.data.repo.RecentVideosRepository
import com.melonityhub.sublearn.core.data.secret.SecretStore
import com.melonityhub.sublearn.core.data.settings.DataStoreSettingsStore
import com.melonityhub.sublearn.core.settings.SettingsStore
import com.melonityhub.sublearn.core.translation.MlKitTranslationProvider
import com.melonityhub.sublearn.core.translation.TranslationProvider
import com.melonityhub.sublearn.feature.player.PlayerViewModel
import com.melonityhub.sublearn.feature.settings.SettingsViewModel
import com.melonityhub.sublearn.feature.words.MyWordsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The single dependency graph (D-007, Koin). Every external capability is bound to its interface. */
val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "sublearn.db")
            .build()
    }
    single { get<AppDatabase>().recentVideos() }
    single { get<AppDatabase>().playbackState() }
    single { get<AppDatabase>().myWords() }

    single { RecentVideosRepository(get()) }
    single { PlaybackStateRepository(get()) }
    single { MyWordsRepository(get()) }

    single<SettingsStore> { DataStoreSettingsStore(androidContext()) }
    single { SecretStore(androidContext()) }
    single<TranslationProvider> { MlKitTranslationProvider() }

    viewModel { params ->
        PlayerViewModel(
            source = params.get(),
            player = params.get(),
            settingsStore = get(),
            playbackStates = get(),
            recents = get(),
            translator = get(),
            myWords = get(),
        )
    }
    viewModel { SettingsViewModel(store = get(), secrets = get()) }
    viewModel { MyWordsViewModel(repository = get()) }
}
