package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.RootManager
import dev.robin.privacify.free.root.FreeRootManager

object RootManagerProvider {
    fun provide(): RootManager {
        return try {
            val clazz = Class.forName("dev.robin.privacify.pro.root.RealRootManager")
            clazz.getDeclaredConstructor().newInstance() as RootManager
        } catch (e: Exception) {
            android.util.Log.e("Provider", "Failed to load RealRootManager", e)
            FreeRootManager()
        }
    }
}
