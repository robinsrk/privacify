package dev.robin.privacify.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
import dev.robin.privacify.pro.utils.ShellUtils
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
	val autostartEnabled: Boolean = false,
	val shellTypeLabel: String = "Auto",
	val shizukuStatus: String = "",
	val shizukuAutoStart: Boolean = false,
	val batteryOptimizationGranted: Boolean = false,
	val rootAvailable: Boolean = false,
	val shizukuReady: Boolean = false,
	val activeShellMethod: String = ""
)

class SettingsViewModel(
	private val prefs: dev.robin.privacify.core.settings.UserPreferencesManager,
	private val appContext: android.content.Context
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
				prefs.autostartEnabled,
				prefs.shellType
			) { array ->
				val theme = array[0] as AppThemeMode
				val notify = array[1] as Boolean
				val freq = array[2] as String
				val automation = array[3] as Boolean
				val autostart = array[4] as Boolean
				val shellType = array[5] as String

				val currentShizukuStatus = mutableState.value.shizukuStatus
				val currentRootAvailable = mutableState.value.rootAvailable
				val currentShizukuReady = mutableState.value.shizukuReady

				SettingsUiState(
					notificationsEnabled = notify,
					scanFrequencyLabel = freq,
					themeLabel = themeModeToLabel(theme),
					automationEnabled = automation,
					autostartEnabled = autostart,
					shellTypeLabel = shellTypeToLabel(shellType),
					shizukuStatus = currentShizukuStatus.ifEmpty { "Unknown" },
					batteryOptimizationGranted = mutableState.value.batteryOptimizationGranted,
					rootAvailable = currentRootAvailable,
					shizukuReady = currentShizukuReady,
					activeShellMethod = getActiveShellMethod(shellType, currentRootAvailable, currentShizukuReady)
				)
			}.collect { newState ->
				mutableState.value = newState
			}
		}

		ioScope.launch {
			refreshRuntimeStatusInternal()
		}
	}

	fun onNotificationsChanged(enabled: Boolean) {
		prefs.setNotificationsEnabled(enabled)
	}

	fun onAutomationChanged(enabled: Boolean) {
		prefs.setAutomationEnabled(enabled)
		dev.robin.privacify.core.provider.PermissionAutomationProvider.provide().automatePermissions(enabled)
	}

	fun onAutostartChanged(enabled: Boolean) {
		prefs.setAutostartEnabled(enabled)
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
				ShellUtils.requestShizukuAutoStart(it)
			}
		} catch (e: Exception) {
		}
	}

	fun refreshRuntimeStatus() {
		ioScope.launch {
			refreshRuntimeStatusInternal()
		}
	}

	private suspend fun refreshRuntimeStatusInternal() {
		val batteryOpt = isBatteryOptimizationGranted()
		val rootAvail = try { ShellUtils.isRootAvailable() } catch (_: Exception) { false }
		val shizukuStatusString = try { ShellUtils.getShizukuStatus(appContext) } catch (_: Exception) { "Error" }
		val shizukuReady = shizukuStatusString == "Ready"
		val shellType = prefs.shellType.value
		val activeMethod = getActiveShellMethod(shellType, rootAvail, shizukuReady)
		mutableState.update {
			it.copy(
				batteryOptimizationGranted = batteryOpt,
				rootAvailable = rootAvail,
				shizukuStatus = shizukuStatusString,
				shizukuReady = shizukuReady,
				activeShellMethod = activeMethod
			)
		}
	}

	private fun isBatteryOptimizationGranted(): Boolean {
		return try {
			val pm = appContext.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
			pm.isIgnoringBatteryOptimizations(appContext.packageName)
		} catch (_: Exception) { false }
	}

	private fun getActiveShellMethod(shellType: String, rootAvail: Boolean, shizukuReady: Boolean): String {
		return when (shellType) {
			"auto" -> when {
				rootAvail -> "Root"
				shizukuReady -> "Shizuku"
				else -> ""
			}
			"root" -> if (rootAvail) "Root" else ""
			"shizuku" -> if (shizukuReady) "Shizuku" else ""
			else -> ""
		}
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
					prefs = dev.robin.privacify.core.settings.UserPreferencesManager.getInstance(context),
					appContext = context.applicationContext
				)
			}
		}
	}
}
