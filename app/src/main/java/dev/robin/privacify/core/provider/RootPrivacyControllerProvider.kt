package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.RootPrivacyController
import dev.robin.privacify.free.root.FreeRootPrivacyController

object RootPrivacyControllerProvider {
    fun provide(): RootPrivacyController {
        return try {
            android.util.Log.d("RootPrivacyControllerProvider", "Attempting to load pro version...")
            val clazz = Class.forName("dev.robin.privacify.pro.root.RealRootPrivacyController")
            val instance = clazz.getDeclaredConstructor().newInstance() as RootPrivacyController
            android.util.Log.d("RootPrivacyControllerProvider", "Pro version loaded successfully")
            instance
        } catch (e: Exception) {
            android.util.Log.e("RootPrivacyControllerProvider", "Failed to load RealRootPrivacyController: ${e.message}")
            android.util.Log.d("RootPrivacyControllerProvider", "Falling back to free version")
            FreeRootPrivacyController()
        }
    }
}
