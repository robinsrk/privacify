package dev.robin.privacify.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.robin.privacify.core.root.RootManagerProvider
import dev.robin.privacify.core.settings.UserPreferencesManager
import dev.robin.privacify.data.apps.SystemPermissionScanner
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootManager
import dev.robin.privacify.domain.root.RootPrivacyController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
	private val rootPrivacyController: RootPrivacyController,
	private val lockdownUseCase: LockdownUseCase,
	private val permissionScanner: PermissionScanner,
	private val rootManager: RootManager,
	private val prefs: UserPreferencesManager
) : ViewModel() {

	private val mutableState = MutableStateFlow(DashboardUiState())
	val state: StateFlow<DashboardUiState> = mutableState

	private val ioScope = CoroutineScope(Dispatchers.IO)

	init {
		ioScope.launch {
			rootManager.rootStatus.collectLatest { rooted ->
				mutableState.update { current ->
					current.copy(isRooted = rooted)
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
			rootPrivacyController.locationDisabled.collectLatest { disabled ->
				mutableState.update { current ->
					current.copy(locationDisabled = disabled)
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
		ioScope.launch {
			prefs.automationEnabled.collectLatest { enabled ->
				mutableState.update { current ->
					current.copy(automationEnabled = enabled)
				}
			}
		}
	}

	fun onQuickActionToggled(action: QuickAction) {
		when (action) {
			QuickAction.Lockdown -> toggleLockdown()
			QuickAction.MicKill -> toggleMic()
			QuickAction.CameraKill -> toggleCamera()
			QuickAction.LocationKill -> toggleLocation()
		}
	}
	fun onScanNowClicked() {
		ioScope.launch {
			mutableState.update { it.copy(isScanning = true, statusSubtitle = "Scanning system...") }
			permissionScanner.refresh()
			mutableState.update { it.copy(isScanning = false, statusSubtitle = "Scan complete. Dashboard is up to date.") }
		}
	}

	fun onAutoGuardToggled(enabled: Boolean) {
		prefs.setAutomationEnabled(enabled)
		dev.robin.privacify.core.provider.PermissionAutomationProvider.provide().automatePermissions(enabled)
	}

	private fun hasShellAccess(): Boolean {
		return mutableState.value.isRooted
	}

	private fun toggleMic() {
		if (!hasShellAccess()) return
		ioScope.launch {
			rootPrivacyController.setMicDisabled(!rootPrivacyController.micDisabled.value)
			dev.robin.privacify.core.widget.PrivacyControlsWidgetProvider.updateAllWidgets(
				dev.robin.privacify.core.utils.AppContextProvider.context
			)
		}
	}

	private fun toggleCamera() {
		if (!hasShellAccess()) return
		ioScope.launch {
			rootPrivacyController.setCameraDisabled(!rootPrivacyController.cameraDisabled.value)
			dev.robin.privacify.core.widget.PrivacyControlsWidgetProvider.updateAllWidgets(
				dev.robin.privacify.core.utils.AppContextProvider.context
			)
		}
	}

	private fun toggleLocation() {
		if (!hasShellAccess()) return
		ioScope.launch {
			rootPrivacyController.setLocationDisabled(!rootPrivacyController.locationDisabled.value)
			dev.robin.privacify.core.widget.PrivacyControlsWidgetProvider.updateAllWidgets(
				dev.robin.privacify.core.utils.AppContextProvider.context
			)
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
			dev.robin.privacify.core.widget.LockdownWidgetProvider.updateAllWidgets(
				dev.robin.privacify.core.utils.AppContextProvider.context
			)
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
		if (current.lockdownEnabled) score += 5
		if (current.micDisabled) score += 3
		if (current.cameraDisabled) score += 3
		if (current.locationDisabled) score += 3

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
					val scanner = SystemPermissionScanner.getInstance(context)
					return DashboardViewModel(
						rootPrivacyController = PrivacyControllersProvider.rootPrivacyController,
						lockdownUseCase = PrivacyControllersProvider.lockdownUseCase,
						permissionScanner = scanner,
						rootManager = RootManagerProvider.instance,
						prefs = UserPreferencesManager.getInstance(context)
					) as T
				}
			}
		}
	}
}
