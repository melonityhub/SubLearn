package com.melonityhub.sublearn

import android.app.Application
import com.melonityhub.sublearn.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SublearnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SublearnApplication)
            modules(appModule)
        }
    }
}
