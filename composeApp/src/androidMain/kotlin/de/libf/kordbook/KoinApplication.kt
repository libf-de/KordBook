package de.libf.kordbook

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import commonModule
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.tools.AndroidMd5
import de.libf.kordbook.data.tools.Md5
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinApplication : Application() {

    val androidModule = module {
        single<SqlDriver> {
            AndroidSqliteDriver(ChordsDatabase.Schema, get(), "chords.db")
        }

        single<Md5> { AndroidMd5() }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KoinApplication)
            modules(commonModule + androidModule)
        }
    }
}