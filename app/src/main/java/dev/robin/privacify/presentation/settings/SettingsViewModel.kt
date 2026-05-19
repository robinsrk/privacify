package dev.robin.privacify.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
import dev.robin.privacify.domain.firewall.FirewallManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
	val notificationsEnabled: Boolean = true,
	val scanFrequencyLabel: String = "Daily",
	val themeLabel: String = "Dark Mode",
	val vpnEnabled: Boolean = true,
	val blockDataEnabled: Boolean = false,
	val systemBlockingEnabled: Boolean = false,
	val automationEnabled: Boolean = false,
	val shellTypeLabel: String = "Auto",
	val shizukuStatus: String = "",
	val shizukuAutoStart: Boolean = false
)

class SettingsViewModel(
	private val firewallManager: FirewallManager,
	private val automationController: dev.robin.privacify.core.PermissionAutomationController,
	private val prefs: dev.robin.privacify.core.settings.UserPreferencesManager
) : ViewModel() {

	private val mutableState = MutableStateFlow(SettingsUiState())
	val state: StateFlow<SettingsUiState> = mutableState

	private val ioScope = CoroutineScope(Dispatchers.IO)

	init {
		ioScope.launch {
			kotlinx.coroutines.flow.combine<Any?, SettingsUiState>(
				prefs.themeMode,
				prefs.notificationsEnabled,
				prefs.scanFrequency,
				firewallManager.enabled,
				firewallManager.blockData,
				firewallManager.systemBlocking,
				prefs.automationEnabled,
				prefs.shellType
			) { array ->
				val theme = array[0] as AppThemeMode
				val notify = array[1] as Boolean
				val freq = array[2] as String
				val vpn = array[3] as Boolean
				val blockData = array[4] as Boolean
				val system = array[5] as Boolean
				val automation = array[6] as Boolean
				val shellType = array[7] as String

				// Keep current shizukuStatus if available
				val currentShizukuStatus = mutableState.value.shizukuStatus

				SettingsUiState(
					notificationsEnabled = notify,
					scanFrequencyLabel = freq,
					themeLabel = themeModeToLabel(theme),
					vpnEnabled = vpn,
					blockDataEnabled = blockData,
					systemBlockingEnabled = system,
					automationEnabled = automation,
					shellTypeLabel = shellTypeToLabel(shellType),
					shizukuStatus = currentShizukuStatus.ifEmpty { "Unknown" }
				)
			}.collect { newState ->
				mutableState.value = newState
			}
		}
		
		// Initialize shizuku status on start
		ioScope.launch {
			refreshShizukuStatusInternal(null)
		}
	}

	fun onNotificationsChanged(enabled: Boolean) {
		prefs.setNotificationsEnabled(enabled)
	}

	fun onAutomationChanged(enabled: Boolean) {
		prefs.setAutomationEnabled(enabled)
		automationController.automatePermissions(enabled)
	}

	fun onVpnChanged(enabled: Boolean) {
		ioScope.launch {
			if (enabled) firewallManager.enable()
			else firewallManager.disable()
		}
	}

	fun onBlockDataChanged(enabled: Boolean) {
		ioScope.launch {
			firewallManager.setBlockData(enabled)
		}
	}

	fun onSystemBlockingChanged(enabled: Boolean) {
		ioScope.launch {
			firewallManager.setSystemBlocking(enabled)
		}
	}

	fun onEditHostsClicked() {
		// In a real app, this would navigate to a text editor screen
		// For now, we'll just add a common tracker to block
		ioScope.launch(Dispatchers.IO) {
			dev.robin.privacify.data.root.HostsFileManager.addBlockRule("telemetry.google.com")
		}
	}

	fun onScanFrequencyClicked() {
		val next = when (state.value.scanFrequencyLabel) {
			"Daily" -> "Weekly"
			"Weekly" -> "Manual"
			else -> "Daily"
		}
		prefs.setScanFrequency(next)
	}

	fun onThemeClicked() {
		prefs.cycleTheme()
	}

	fun onShellTypeClicked() {
		val currentStoredValue = prefs.shellType.value
		val next = when (currentStoredValue) {
			"auto" -> "root"
			"root" -> "shizuku"
			else -> "auto"
		}
		prefs.setShellType(next)
	}

	fun setShellType(type: String) {
		android.util.Log.d("SettingsViewModel", "setShellType called with: $type")
		prefs.setShellType(type)
		dev.robin.privacify.pro.utils.ShellUtils.shellTypePreference = type
		mutableState.update { it.copy(shellTypeLabel = shellTypeToLabel(type)) }
	}

fun requestShizukuPermission() {
		android.util.Log.d("SettingsViewModel", "requestShizukuPermission called, MainActivity instance: ${dev.robin.privacify.MainActivity.instance}")
		try {
			val instance = dev.robin.privacify.MainActivity.instance
			if (instance != null) {
				instance.requestShizukuPermission()
				android.util.Log.d("SettingsViewModel", "Shizuku permission requested")
			} else {
				android.util.Log.e("SettingsViewModel", "MainActivity instance is null!")
			}
		} catch (e: Exception) {
			android.util.Log.e("SettingsViewModel", "Failed to request Shizuku", e)
		}
	}

	fun requestShizukuAutoStart(context: android.content.Context?) {
		android.util.Log.d("SettingsViewModel", "requestShizukuAutoStart called")
		try {
			context?.let {
				// Request autostart permission via Shizuku
				dev.robin.privacify.pro.utils.ShellUtils.requestShizukuAutoStart(it)
			}
		} catch (e: Exception) {
			android.util.Log.e("SettingsViewModel", "Failed to request Shizuku autostart", e)
		}
	}

	fun refreshShizukuStatus(context: android.content.Context? = null) {
		ioScope.launch {
			refreshShizukuStatusInternal(context)
		}
	}

	private suspend fun refreshShizukuStatusInternal(context: android.content.Context?) {
		val status = try {
			dev.robin.privacify.pro.utils.ShellUtils.getShizukuStatus(context)
		} catch (e: Exception) {
			"Error"
		}
		mutableState.update { it.copy(shizukuStatus = status) }
	}

	private fun themeModeToLabel(mode: AppThemeMode): String = when (mode) {
		AppThemeMode.Dark -> "Dark Mode"
		AppThemeMode.System -> "System Default"
		AppThemeMode.Light -> "Light Mode"
	}

	private fun shellTypeToLabel(type: String): String = when (type) {
		"root" -> "Root"
		"shizuku" -> "Shizuku"
		else -> "Auto"
	}

	companion object {
		fun factory(context: android.content.Context): ViewModelProvider.Factory = viewModelFactory {
			initializer {
				SettingsViewModel(
					firewallManager = PrivacyControllersProvider.firewallManager,
					automationController = dev.robin.privacify.core.provider.PermissionAutomationProvider.provide(),
					prefs = dev.robin.privacify.core.settings.UserPreferencesManager.getInstance(context)
				)
			}
		}
	}
}
