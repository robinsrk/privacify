package dev.robin.privacify.pro

import dev.robin.privacify.core.PrivacyController
import dev.robin.privacify.pro.utils.ShellUtils

class RealPrivacyController : PrivacyController {

    override fun applyPrivacyPolicy(enabled: Boolean) {
        if (enabled) {
            // Enforce strict privacy settings requiring root
            ShellUtils.runRootCommand("settings put global adb_enabled 0")
            ShellUtils.runRootCommand("settings put global development_settings_enabled 0")
            // Additional privacy hardening
            ShellUtils.runRootCommand("pm disable-user --user 0 com.google.android.gms")
        } else {
            // Revert changes
            ShellUtils.runRootCommand("pm enable com.google.android.gms")
            ShellUtils.runRootCommand("settings put global adb_enabled 1")
        }
    }

}
