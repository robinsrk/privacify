package dev.robin.privacify.free

import dev.robin.privacify.core.PermissionAutomationController

class FreePermissionAutomationController : PermissionAutomationController {
    override fun automatePermissions(enabled: Boolean) {
        // free implementation: limited permission automation
    }

    override fun isEnabled(): Boolean = false
}
