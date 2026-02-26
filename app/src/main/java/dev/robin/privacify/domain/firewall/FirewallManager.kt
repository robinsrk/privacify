package dev.robin.privacify.domain.firewall

import kotlinx.coroutines.flow.StateFlow

interface FirewallManager {
	val enabled: StateFlow<Boolean>
	val blockedApps: StateFlow<Set<String>>
	val blockData: StateFlow<Boolean>
	val systemBlocking: StateFlow<Boolean>

	suspend fun enable()
	suspend fun disable()
	suspend fun blockApp(packageName: String)
	suspend fun unblockApp(packageName: String)
	suspend fun setBlockData(enabled: Boolean)
	suspend fun setSystemBlocking(enabled: Boolean)
}
