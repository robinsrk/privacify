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

	private val context = dev.robin.privacify.core.utils.AppContextProvider.context

	override suspend fun enable() {
		_enabled.value = true
		startVpn()
	}

	override suspend fun disable() {
		_enabled.value = false
		stopVpn()
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

	private fun startVpn() {
		val intent = android.content.Intent(context, PrivacifyVpnService::class.java)
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			context.startForegroundService(intent)
		} else {
			context.startService(intent)
		}
	}

	private fun stopVpn() {
		val intent = android.content.Intent(context, PrivacifyVpnService::class.java).apply {
			action = PrivacifyVpnService.ACTION_STOP
		}
		context.startService(intent)
	}
}
