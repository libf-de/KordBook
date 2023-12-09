package de.libf.kordbook

import android.app.Application
import commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import platformModule

class KoinApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KoinApplication)
            modules(commonModule + platformModule)
        }
    }
}