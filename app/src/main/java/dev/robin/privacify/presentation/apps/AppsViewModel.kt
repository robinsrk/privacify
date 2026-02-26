package dev.robin.privacify.presentation.apps

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

data class AppsUiState(
	val apps: List<AppPrivacyInfo> = emptyList(),
	val query: String = "",
	val filter: RiskFilter = RiskFilter.All
) {
	val filteredApps: List<AppPrivacyInfo>
		get() {
			val base = when (filter) {
				RiskFilter.All -> apps
				RiskFilter.High -> apps.filter { it.riskLevel == AppRiskLevel.High }
				RiskFilter.Medium -> apps.filter { it.riskLevel == AppRiskLevel.Medium }
				RiskFilter.Low -> apps.filter { it.riskLevel == AppRiskLevel.Low }
			}
			if (query.isBlank()) return base
			val q = query.lowercase()
			return base.filter { app ->
				app.appName.lowercase().contains(q) ||
						app.permissionsSummary.lowercase().contains(q)
			}
		}
}

enum class RiskFilter {
	All,
	High,
	Medium,
	Low
}

class AppsViewModel(
	private val scanner: PermissionScanner
) : ViewModel() {

	private val mutableState = MutableStateFlow(AppsUiState())
	val state: StateFlow<AppsUiState> = mutableState

	init {
		viewModelScope.launch {
			scanner.apps.collectLatest { apps ->
				mutableState.update { current ->
					current.copy(apps = apps)
				}
			}
		}
	}

	fun onQueryChanged(query: String) {
		mutableState.update { current ->
			current.copy(query = query)
		}
	}

	fun onFilterChanged(filter: RiskFilter) {
		mutableState.update { current ->
			current.copy(filter = filter)
		}
	}

	companion object {
		fun factory(context: Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					val scanner = SystemPermissionScanner(context.applicationContext)
					return AppsViewModel(scanner) as T
				}
			}
		}
	}
}
