package dev.robin.privacify.core

interface PermissionAutomationController {
    fun automatePermissions(enabled: Boolean)
    fun isEnabled(): Boolean
}
