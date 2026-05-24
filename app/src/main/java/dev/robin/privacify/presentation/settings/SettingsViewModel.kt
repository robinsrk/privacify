package dev.robin.privacify.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
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
	val automationEnabled: Boolean = false,
	val shellTypeLabel: String = "Auto",
	val shizukuStatus: String = "",
	val shizukuAutoStart: Boolean = false
)

class SettingsViewModel(
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
				prefs.automationEnabled,
				prefs.shellType
			) { array ->
				val theme = array[0] as AppThemeMode
				val notify = array[1] as Boolean
				val freq = array[2] as String
				val automation = array[3] as Boolean
				val shellType = array[4] as String

				val currentShizukuStatus = mutableState.value.shizukuStatus

				SettingsUiState(
					notificationsEnabled = notify,
					scanFrequencyLabel = freq,
					themeLabel = themeModeToLabel(theme),
					automationEnabled = automation,
					shellTypeLabel = shellTypeToLabel(shellType),
					shizukuStatus = currentShizukuStatus.ifEmpty { "Unknown" }
				)
			}.collect { newState ->
				mutableState.value = newState
			}
		}

		ioScope.launch {
			refreshShizukuStatusInternal(null)
		}
	}

	fun onNotificationsChanged(enabled: Boolean) {
		prefs.setNotificationsEnabled(enabled)
	}

	fun onAutomationChanged(enabled: Boolean) {
		prefs.setAutomationEnabled(enabled)
		dev.robin.privacify.core.provider.PermissionAutomationProvider.provide().automatePermissions(enabled)
	}

	fun onEditHostsClicked() {
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
		prefs.setShellType(type)
		mutableState.update { it.copy(shellTypeLabel = shellTypeToLabel(type)) }
	}

	fun requestShizukuPermission() {
		try {
			val instance = dev.robin.privacify.MainActivity.instance
			if (instance != null) {
				instance.requestShizukuPermission()
			}
		} catch (e: Exception) {
		}
	}

	fun requestShizukuAutoStart(context: android.content.Context?) {
		try {
			context?.let {
				val clazz = Class.forName("dev.robin.privacify.pro.utils.ShellUtils")
				val method = clazz.getMethod("requestShizukuAutoStart", android.content.Context::class.java)
				method.invoke(null, it)
			}
		} catch (e: Exception) {
		}
	}

	fun refreshShizukuStatus(context: android.content.Context? = null) {
		ioScope.launch {
			refreshShizukuStatusInternal(context)
		}
	}

	private suspend fun refreshShizukuStatusInternal(context: android.content.Context?) {
		val status = try {
			val clazz = Class.forName("dev.robin.privacify.pro.utils.ShellUtils")
			val method = clazz.getMethod("getShizukuStatus", android.content.Context::class.java)
			method.invoke(null, context) as? String ?: "Error"
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
					prefs = dev.robin.privacify.core.settings.UserPreferencesManager.getInstance(context)
				)
			}
		}
	}
}
