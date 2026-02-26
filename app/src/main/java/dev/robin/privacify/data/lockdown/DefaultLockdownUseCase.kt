package dev.robin.privacify.data.lockdown

import dev.robin.privacify.domain.firewall.FirewallManager
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootPrivacyController

class DefaultLockdownUseCase(
	private val firewallManager: FirewallManager,
	private val rootPrivacyController: RootPrivacyController
) : LockdownUseCase {
	private val context = dev.robin.privacify.core.utils.AppContextProvider.context
	private val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

	override suspend fun enableLockdown() {
		firewallManager.enable()
		rootPrivacyController.setMicDisabled(true)
		rootPrivacyController.setCameraDisabled(true)
		enableDnd(true)
	}

	override suspend fun disableLockdown() {
		rootPrivacyController.setMicDisabled(false)
		rootPrivacyController.setCameraDisabled(false)
		firewallManager.disable()
		enableDnd(false)
	}

	private fun enableDnd(enable: Boolean) {
		try {
			if (notificationManager.isNotificationPolicyAccessGranted) {
				val filter = if (enable) {
					android.app.NotificationManager.INTERRUPTION_FILTER_NONE
				} else {
					android.app.NotificationManager.INTERRUPTION_FILTER_ALL
				}
				notificationManager.setInterruptionFilter(filter)
			}
		} catch (e: Exception) {
			// Permission not granted or other error
		}
	}
}

