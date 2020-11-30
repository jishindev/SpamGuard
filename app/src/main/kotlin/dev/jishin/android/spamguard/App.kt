package dev.jishin.android.spamguard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        configTimber()
    }

    private fun configTimber() {
        Timber.plant(Timber.DebugTree())
    }

}