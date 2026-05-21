package dev.robin.privacify.core.security

import dev.robin.privacify.core.provider.RootPrivacyControllerProvider
import dev.robin.privacify.data.lockdown.DefaultLockdownUseCase
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootPrivacyController

object PrivacyControllersProvider {
	val rootPrivacyController: RootPrivacyController by lazy { RootPrivacyControllerProvider.provide() }
	val lockdownUseCase: LockdownUseCase by lazy {
		DefaultLockdownUseCase(
			rootPrivacyController = rootPrivacyController
		)
	}
}
