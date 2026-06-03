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
import dev.robin.privacify.data.onboarding.DatastoreOnboardingRepository
import dev.robin.privacify.domain.onboarding.OnboardingRepository
import dev.robin.privacify.domain.onboarding.OnboardingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = mutableState

    init {
        viewModelScope.launch {
            onboardingRepository.isOnboardingCompleted.collectLatest { completed ->
                mutableState.update { current ->
                    current.copy(isCompleted = completed)
                }
            }
        }
    }

    fun checkPermissions(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
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

            val rootGranted = try {
                dev.robin.privacify.pro.utils.ShellUtils.isRootAvailable()
            } catch (e: Exception) {
                false
            }

            val shizukuGranted = try {
                rikka.shizuku.Shizuku.pingBinder() && rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                false
            }

            mutableState.update { current ->
                current.copy(
                    usageAccessGranted = usageGranted,
                    notificationPermissionGranted = notificationGranted,
                    rootGranted = rootGranted,
                    shizukuGranted = shizukuGranted
                )
            }
        }
    }

    fun onWelcomeContinue() {
        mutableState.update { current ->
            current.copy(step = OnboardingStep.Acknowledgement)
        }
    }

    fun onAcknowledgementContinue() {
        mutableState.update { current ->
            current.copy(step = OnboardingStep.SystemCheck)
        }
    }

    fun onSystemCheckContinue() {
        mutableState.update { current ->
            current.copy(step = OnboardingStep.FeatureIntro)
        }
    }

    fun onFeatureIntroContinue() {
        completeOnboarding()
    }

    fun onBack() {
        val currentStep = state.value.step
        val previousStep = when (currentStep) {
            OnboardingStep.Acknowledgement -> OnboardingStep.Welcome
            OnboardingStep.SystemCheck -> OnboardingStep.Acknowledgement
            OnboardingStep.FeatureIntro -> OnboardingStep.SystemCheck
            else -> return
        }
        mutableState.update { it.copy(step = previousStep) }
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

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val onboardingRepository = DatastoreOnboardingRepository(context.applicationContext)
                    return OnboardingViewModel(onboardingRepository) as T
                }
            }
        }
    }
}
