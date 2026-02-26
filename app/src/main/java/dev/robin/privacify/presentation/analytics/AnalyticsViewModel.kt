package dev.robin.privacify.presentation.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.data.apps.SystemPermissionScanner
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import dev.robin.privacify.domain.firewall.FirewallManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
	val micUsageMinutesToday: Int = 0,
	val micUsageChangePercent: Int = 0,
	val lastMicActiveLabel: String = "Inactive",
	val totalPermissionRequests: Int = 0,
	val locationSharePercent: Int = 0,
	val cameraSharePercent: Int = 0,
	val micSharePercent: Int = 0,
	val contactsSharePercent: Int = 0,
	val highRiskApps: List<AppPrivacyInfo> = emptyList(),
	val barChartData: List<Float> = List(12) { 0f }
)

class AnalyticsViewModel(
	private val scanner: PermissionScanner,
	private val firewallManager: FirewallManager
) : ViewModel() {

	private val mutableState = MutableStateFlow(AnalyticsUiState())
	val state: StateFlow<AnalyticsUiState> = mutableState

	init {
		viewModelScope.launch {
			scanner.apps.collectLatest { apps ->
				val firewallEnabled = firewallManager.enabled.value
				val updated = computeState(apps, firewallEnabled)
				mutableState.update { updated }
			}
		}
	}

	private fun computeState(
		apps: List<AppPrivacyInfo>,
		firewallEnabled: Boolean
	): AnalyticsUiState {
		var locationCount = 0
		var cameraCount = 0
		var micCount = 0
		var contactsCount = 0

		apps.forEach { app ->
			app.permissionIcons.forEach { icon ->
				when (icon) {
					"location_on" -> locationCount++
					"photo_camera" -> cameraCount++
					"mic" -> micCount++
					"contacts" -> contactsCount++
				}
			}
		}

		val totalSignals = locationCount + cameraCount + micCount + contactsCount
		val totalRequests = if (totalSignals == 0) 0 else totalSignals * 8

		val locationPercent = if (totalSignals > 0) locationCount * 100 / totalSignals else 0
		val cameraPercent = if (totalSignals > 0) cameraCount * 100 / totalSignals else 0
		val micPercent = if (totalSignals > 0) micCount * 100 / totalSignals else 0
		val contactsPercent = if (totalSignals > 0) contactsCount * 100 / totalSignals else 0

		val micMinutes = micCount * 15
		val micChange = if (firewallEnabled) -20 else 12

		val lastMicLabel = if (micCount > 0) {
			"Active recently"
		} else {
			"Inactive"
		}

		val highRiskApps = apps.filter { it.riskLevel == AppRiskLevel.High }

		// Generate bar chart data proportional to actual permission counts
		val maxSignal = maxOf(locationCount, cameraCount, micCount, contactsCount, 1)
		val barChartData = if (totalSignals > 0) {
			List(12) { index ->
				when (index % 4) {
					0 -> locationCount.toFloat() / maxSignal
					1 -> micCount.toFloat() / maxSignal
					2 -> cameraCount.toFloat() / maxSignal
					3 -> contactsCount.toFloat() / maxSignal
					else -> 0f
				}.coerceIn(0.05f, 1f)
			}
		} else {
			List(12) { 0.05f }
		}

		return AnalyticsUiState(
			micUsageMinutesToday = micMinutes,
			micUsageChangePercent = micChange,
			lastMicActiveLabel = lastMicLabel,
			totalPermissionRequests = totalRequests,
			locationSharePercent = locationPercent,
			cameraSharePercent = cameraPercent,
			micSharePercent = micPercent,
			contactsSharePercent = contactsPercent,
			highRiskApps = highRiskApps,
			barChartData = barChartData
		)
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					val scanner = SystemPermissionScanner(context.applicationContext)
					return AnalyticsViewModel(
						scanner = scanner,
						firewallManager = PrivacyControllersProvider.firewallManager
					) as T
				}
			}
		}
	}
}
