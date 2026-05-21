package dev.robin.privacify.presentation.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.robin.privacify.data.apps.SystemPermissionScanner
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
	val totalApps: Int = 0,
	val highRiskCount: Int = 0,
	val mediumRiskCount: Int = 0,
	val lowRiskCount: Int = 0,
	val locationAppCount: Int = 0,
	val cameraAppCount: Int = 0,
	val micAppCount: Int = 0,
	val contactsAppCount: Int = 0,
	val smsAppCount: Int = 0,
	val totalPermissionGrants: Int = 0,
	val highRiskApps: List<AppPrivacyInfo> = emptyList()
)

class AnalyticsViewModel(
	private val scanner: PermissionScanner
) : ViewModel() {

	private val mutableState = MutableStateFlow(AnalyticsUiState())
	val state: StateFlow<AnalyticsUiState> = mutableState

	init {
		viewModelScope.launch {
			scanner.apps.collectLatest { apps ->
				val updated = computeState(apps)
				mutableState.update { updated }
			}
		}
	}

	private fun computeState(
		apps: List<AppPrivacyInfo>
	): AnalyticsUiState {
		var locationCount = 0
		var cameraCount = 0
		var micCount = 0
		var contactsCount = 0
		var smsCount = 0
		var highRisk = 0
		var mediumRisk = 0
		var lowRisk = 0

		apps.forEach { app ->
			when (app.riskLevel) {
				AppRiskLevel.High -> highRisk++
				AppRiskLevel.Medium -> mediumRisk++
				AppRiskLevel.Low -> lowRisk++
			}
			app.permissionIcons.forEach { icon ->
				when (icon) {
					"location_on" -> locationCount++
					"photo_camera" -> cameraCount++
					"mic" -> micCount++
					"contacts" -> contactsCount++
					"phone" -> smsCount++
				}
			}
		}

		val totalPermissions = locationCount + cameraCount + micCount + contactsCount + smsCount
		val highRiskApps = apps.filter { it.riskLevel == AppRiskLevel.High }

		return AnalyticsUiState(
			totalApps = apps.size,
			highRiskCount = highRisk,
			mediumRiskCount = mediumRisk,
			lowRiskCount = lowRisk,
			locationAppCount = locationCount,
			cameraAppCount = cameraCount,
			micAppCount = micCount,
			contactsAppCount = contactsCount,
			smsAppCount = smsCount,
			totalPermissionGrants = totalPermissions,
			highRiskApps = highRiskApps
		)
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					val scanner = SystemPermissionScanner(context.applicationContext)
					return AnalyticsViewModel(
						scanner = scanner
					) as T
				}
			}
		}
	}
}
