package dev.robin.privacify

import android.app.Application
import dev.robin.privacify.core.utils.AppContextProvider

class PrivacifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextProvider.context = this
    }
}
