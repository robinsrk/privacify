package dev.robin.privacify.core.provider

object ProFeature {

    private val autoGuardAvailable by lazy {
        try {
            Class.forName("dev.robin.privacify.pro.RealPermissionAutomationController")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isAutoGuardAvailable(): Boolean = autoGuardAvailable
}
