package dev.robin.privacify.core.security

import dev.robin.privacify.data.firewall.DefaultFirewallManager
import dev.robin.privacify.data.lockdown.DefaultLockdownUseCase
import dev.robin.privacify.data.root.DefaultRootPrivacyController
import dev.robin.privacify.domain.firewall.FirewallManager
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootPrivacyController

object PrivacyControllersProvider {
	val firewallManager: FirewallManager by lazy { DefaultFirewallManager() }
	val rootPrivacyController: RootPrivacyController by lazy { DefaultRootPrivacyController() }
	val lockdownUseCase: LockdownUseCase by lazy {
		DefaultLockdownUseCase(
			firewallManager = firewallManager,
			rootPrivacyController = rootPrivacyController
		)
	}
}

