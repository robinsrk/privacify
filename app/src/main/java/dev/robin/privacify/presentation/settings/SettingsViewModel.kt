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
	val systemBlockingEnabled: Boolean = false
)

class SettingsViewModel(
	private val firewallManager: FirewallManager,
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
				firewallManager.systemBlocking
			) { array ->
				val theme = array[0] as AppThemeMode
				val notify = array[1] as Boolean
				val freq = array[2] as String
				val vpn = array[3] as Boolean
				val blockData = array[4] as Boolean
				val system = array[5] as Boolean

				SettingsUiState(
					notificationsEnabled = notify,
					scanFrequencyLabel = freq,
					themeLabel = themeModeToLabel(theme),
					vpnEnabled = vpn,
					blockDataEnabled = blockData,
					systemBlockingEnabled = system
				)
			}.collect { newState ->
				mutableState.value = newState
			}
		}
	}

	fun onNotificationsChanged(enabled: Boolean) {
		prefs.setNotificationsEnabled(enabled)
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

	private fun themeModeToLabel(mode: AppThemeMode): String = when (mode) {
		AppThemeMode.Dark -> "Dark Mode"
		AppThemeMode.System -> "System Default"
		AppThemeMode.Light -> "Light Mode"
	}

	companion object {
		fun factory(context: android.content.Context): ViewModelProvider.Factory = viewModelFactory {
			initializer {
				SettingsViewModel(
					firewallManager = PrivacyControllersProvider.firewallManager,
					prefs = dev.robin.privacify.core.settings.UserPreferencesManager.getInstance(context)
				)
			}
		}
	}
}
