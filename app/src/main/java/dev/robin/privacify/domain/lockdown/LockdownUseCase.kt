package dev.robin.privacify.domain.lockdown

interface LockdownUseCase {
	suspend fun enableLockdown()
	suspend fun disableLockdown()
}

