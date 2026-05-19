package dev.robin.privacify.domain.lockdown

import kotlinx.coroutines.flow.StateFlow

interface LockdownUseCase {
	suspend fun enableLockdown()
	suspend fun disableLockdown()
	val isActive: StateFlow<Boolean>
}