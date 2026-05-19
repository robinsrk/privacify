package dev.robin.privacify.data.firewall

import dev.robin.privacify.domain.firewall.FirewallManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DefaultFirewallManager : FirewallManager {
	private val _enabled = MutableStateFlow(false)
	override val enabled: StateFlow<Boolean> = _enabled

	private val _blockedApps = MutableStateFlow<Set<String>>(emptySet())
	override val blockedApps: StateFlow<Set<String>> = _blockedApps

	private val _blockData = MutableStateFlow(false)
	override val blockData: StateFlow<Boolean> = _blockData

	private val _systemBlocking = MutableStateFlow(false)
	override val systemBlocking: StateFlow<Boolean> = _systemBlocking

	// VPN integration removed for now; no external service is started

	override suspend fun enable() {
		_enabled.value = true
		// VPN orchestration removed
	}

	override suspend fun disable() {
		_enabled.value = false
		// VPN orchestration removed
	}

	override suspend fun blockApp(packageName: String) {
		_blockedApps.value = _blockedApps.value + packageName
		if (_enabled.value) startVpn() // Refresh
	}

	override suspend fun unblockApp(packageName: String) {
		_blockedApps.value = _blockedApps.value - packageName
		if (_enabled.value) startVpn() // Refresh
	}

	override suspend fun setBlockData(enabled: Boolean) {
		_blockData.value = enabled
		if (_enabled.value) startVpn()
	}

	override suspend fun setSystemBlocking(enabled: Boolean) {
		_systemBlocking.value = enabled
		if (_enabled.value) startVpn()
	}

	private fun startVpn() { /* removed VPN orchestration */ }

	private fun stopVpn() { /* removed VPN orchestration */ }
}
