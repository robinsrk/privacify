package dev.robin.privacify.presentation.onboarding

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.robin.privacify.core.root.RootManagerProvider
import dev.robin.privacify.data.onboarding.DatastoreOnboardingRepository
import dev.robin.privacify.domain.onboarding.OnboardingRepository
import dev.robin.privacify.domain.onboarding.OnboardingStep
import dev.robin.privacify.domain.root.RootManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
	private val rootManager: RootManager,
	private val onboardingRepository: OnboardingRepository
) : ViewModel() {

	private val mutableState = MutableStateFlow(OnboardingUiState())
	val state: StateFlow<OnboardingUiState> = mutableState

	private var rootJob: Job? = null

	init {
		rootJob = viewModelScope.launch {
			rootManager.rootStatus.collectLatest { rooted ->
				mutableState.update { current ->
					current.copy(isRootAvailable = rooted)
				}
			}
		}

		viewModelScope.launch {
			onboardingRepository.isOnboardingCompleted.collectLatest { completed ->
				mutableState.update { current ->
					current.copy(isCompleted = completed)
				}
			}
		}
	}

	fun checkPermissions(context: Context) {
		val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
		val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			appOps.unsafeCheckOpNoThrow(
				AppOpsManager.OPSTR_GET_USAGE_STATS,
				android.os.Process.myUid(),
				context.packageName
			)
		} else {
			appOps.checkOpNoThrow(
				AppOpsManager.OPSTR_GET_USAGE_STATS,
				android.os.Process.myUid(),
				context.packageName
			)
		}
		val usageGranted = mode == AppOpsManager.MODE_ALLOWED

		val notificationGranted = if (Build.VERSION.SDK_INT >= 33) {
			ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
		} else {
			true
		}

		mutableState.update { current ->
			current.copy(
				usageAccessGranted = usageGranted,
				notificationPermissionGranted = notificationGranted
			)
		}
	}

	fun onWelcomeContinue() {
		mutableState.update { current ->
			current.copy(step = OnboardingStep.FeaturesOverview)
		}
	}

	fun onFeaturesContinue() {
		mutableState.update { current ->
			current.copy(step = OnboardingStep.SystemCheck)
		}
	}

	fun onSystemCheckContinue() {
		val nextStep = if (state.value.isRootAvailable) {
			OnboardingStep.RootDetection
		} else {
			null
		}

		if (nextStep != null) {
			mutableState.update { current ->
				current.copy(step = nextStep)
			}
		} else {
			completeOnboarding()
		}
	}

	fun onRootDetectionContinue() {
		completeOnboarding()
	}

	private fun completeOnboarding() {
		viewModelScope.launch {
			mutableState.update { current ->
				current.copy(isLoading = true)
			}

			onboardingRepository.setOnboardingCompleted(true)

			mutableState.update { current ->
				current.copy(isLoading = false, isCompleted = true)
			}
		}
	}

	override fun onCleared() {
		rootJob?.cancel()
		super.onCleared()
	}

	companion object {
		fun factory(context: android.content.Context): ViewModelProvider.Factory {
			return object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel> create(modelClass: Class<T>): T {
					val rootManager = RootManagerProvider.instance
					val onboardingRepository = DatastoreOnboardingRepository(context.applicationContext)
					return OnboardingViewModel(rootManager, onboardingRepository) as T
				}
			}
		}
	}
}

