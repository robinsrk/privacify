package dev.robin.privacify.core.utils

import android.content.Context
import java.lang.ref.WeakReference

object AppContextProvider {
    private var contextRef: WeakReference<Context>? = null

    var context: Context
        get() = contextRef?.get() ?: throw IllegalStateException("AppContextProvider not initialized")
        set(value) {
            contextRef = WeakReference(value.applicationContext)
        }
}
