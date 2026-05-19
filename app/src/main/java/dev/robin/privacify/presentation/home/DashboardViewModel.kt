package dev.robin.privacify.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.robin.privacify.core.root.RootManagerProvider
import dev.robin.privacify.data.apps.SystemPermissionScanner
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.domain.firewall.FirewallManager
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootManager
import dev.robin.privacify.domain.root.RootPrivacyController
import dev.robin.privacify.pro.utils.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
	private val firewallManager: FirewallManager,
	private val rootPrivacyController: RootPrivacyController,
	private val lockdownUseCase: LockdownUseCase,
	private val permissionScanner: PermissionScanner,
	private val rootManager: RootManager
) : ViewModel() {

	private val mutableState = MutableStateFlow(DashboardUiState())
	val state: StateFlow<DashboardUiState> = mutableState

	private val ioScope = CoroutineScope(Dispatchers.IO)

	init {
		ioScope.launch {
			updateShellAccessStatus(ShellUtils.shellTypePreference)
		}
		ioScope.launch {
			for (i in 0..9) {
				kotlinx.coroutines.delay(200)
				val currentShellType = ShellUtils.shellTypePreference
				if (currentShellType != mutableState.value.shellType) {
					updateShellAccessStatus(currentShellType)
				}
			}
			while (true) {
				kotlinx.coroutines.delay(2000)
				val currentShellType = ShellUtils.shellTypePreference
				if (currentShellType != mutableState.value.shellType) {
					updateShellAccessStatus(currentShellType)
				}
			}
		}
		ioScope.launch {
			firewallManager.enabled.collectLatest { enabled ->
				mutableState.update { current ->
					val summary = if (enabled) {
						"Firewall active"
					} else {
						"Firewall disabled"
					}
					current.copy(
						firewallEnabled = enabled,
						secureNetworkSummary = summary
					)
				}
			}
		}
		ioScope.launch {
			rootPrivacyController.micDisabled.collectLatest { disabled ->
				mutableState.update { current ->
					current.copy(micDisabled = disabled)
				}
			}
		}
		ioScope.launch {
			rootPrivacyController.cameraDisabled.collectLatest { disabled ->
				mutableState.update { current ->
					current.copy(cameraDisabled = disabled)
				}
			}
		}
		ioScope.launch {
			permissionScanner.apps.collectLatest { apps ->
				mutableState.update { current ->
					computeFromApps(current, apps)
				}
			}
		}
	}

	fun onQuickActionToggled(action: QuickAction) {
		when (action) {
			QuickAction.Lockdown -> toggleLockdown()
			QuickAction.MicKill -> toggleMic()
			QuickAction.CameraKill -> toggleCamera()
			QuickAction.Firewall -> toggleFirewall()
		}
	}
	fun onScanNowClicked() {
		ioScope.launch {
			mutableState.update { it.copy(isScanning = true, statusSubtitle = "Scanning system...") }
			permissionScanner.refresh()
			// The init block's observer will pick up the new data automatically
			// Give a brief delay for the UI to show the scanning state
			delay(500)
			mutableState.update { it.copy(isScanning = false, statusSubtitle = "Scan complete. Dashboard is up to date.") }
		}
	}

	private fun toggleFirewall() {
		ioScope.launch {
			if (firewallManager.enabled.value) {
				firewallManager.disable()
			} else {
				firewallManager.enable()
			}
		}
	}

	private fun hasShellAccess(): Boolean {
		return mutableState.value.isRooted
	}

	private fun updateShellAccessStatus(shellType: String) {
		val hasShellAccess = when (shellType) {
			"shizuku" -> {
				try {
					val available = ShellUtils.isShizukuAvailable()
					val granted = ShellUtils.isShizukuGranted()
					android.util.Log.d("DashboardViewModel", "Shizuku check: available=$available, granted=$granted")
					available && granted
				} catch (e: Exception) {
					android.util.Log.d("DashboardViewModel", "Shizuku check failed: ${e.message}")
					false
				}
			}
			"root" -> {
				try {
					ShellUtils.isRootAvailable()
				} catch (e: Exception) {
					false
				}
			}
			else -> {
				try {
					ShellUtils.isShizukuAvailable() && ShellUtils.isShizukuGranted()
				} catch (e: Exception) {
					false
				} || try {
					ShellUtils.isRootAvailable()
				} catch (e: Exception) {
					false
				}
			}
		}
		android.util.Log.d("DashboardViewModel", "Shell type: $shellType, hasShellAccess: $hasShellAccess")
		mutableState.update { it.copy(isRooted = hasShellAccess, shellType = shellType) }
	}

	private fun toggleMic() {
		if (!hasShellAccess()) return
		ioScope.launch {
			rootPrivacyController.setMicDisabled(!rootPrivacyController.micDisabled.value)
		}
	}

	private fun toggleCamera() {
		if (!hasShellAccess()) return
		ioScope.launch {
			rootPrivacyController.setCameraDisabled(!rootPrivacyController.cameraDisabled.value)
		}
	}

	private fun toggleLockdown() {
		if (!hasShellAccess()) return
		ioScope.launch {
			if (state.value.lockdownEnabled) {
				lockdownUseCase.disableLockdown()
				mutableState.update { current ->
					current.copy(lockdownEnabled = false)
				}
			} else {
				lockdownUseCase.enableLockdown()
				mutableState.update { current ->
					current.copy(lockdownEnabled = true)
				}
			}
		}
	}

	private fun computeFromApps(
		current: DashboardUiState,
		apps: List<AppPrivacyInfo>
	): DashboardUiState {
		val highRiskApps = apps.filter { it.riskLevel == AppRiskLevel.High }
		val micApps = apps.filter { app -> app.permissionIcons.contains("mic") }
		val cameraApps = apps.filter { app -> app.permissionIcons.contains("photo_camera") }
		val locationApps = apps.filter { app -> app.permissionIcons.contains("location_on") }

		var score = 100
		score -= highRiskApps.size * 5
		if (micApps.isNotEmpty()) score -= 5
		if (cameraApps.isNotEmpty()) score -= 5
		if (locationApps.size > 2) score -= 5
		if (current.firewallEnabled) score += 5
		if (current.lockdownEnabled) score += 5
		if (current.micDisabled) score += 3
		if (current.cameraDisabled) score += 3

		if (score > 100) score = 100
		if (score < 0) score = 0

		val status = when {
			score >= 90 -> "System integrity verified. No unauthorized access detected."
			score >= 75 -> "Moderate risk detected. Review high permission apps."
			else -> "High risk detected. Lockdown recommended."
		}

		return current.copy(
			privacyScore = score,
			statusSubtitle = status,
			micAccessCount = micApps.size,
			cameraAccessCount = cameraApps.size,
			locationAccessCount = locationApps.size
		)
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					val scanner = SystemPermissionScanner(context.applicationContext)
					return DashboardViewModel(
						firewallManager = PrivacyControllersProvider.firewallManager,
						rootPrivacyController = PrivacyControllersProvider.rootPrivacyController,
						lockdownUseCase = PrivacyControllersProvider.lockdownUseCase,
						permissionScanner = scanner,
						rootManager = RootManagerProvider.instance
					) as T
				}
			}
		}
	}
}
