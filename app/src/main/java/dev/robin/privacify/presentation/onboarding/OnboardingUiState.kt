package dev.robin.privacify.presentation.onboarding

import dev.robin.privacify.domain.onboarding.OnboardingStep

data class OnboardingUiState(
	val step: OnboardingStep = OnboardingStep.Welcome,
	val isRootAvailable: Boolean = false,
	val usageAccessGranted: Boolean = false,
	val notificationPermissionGranted: Boolean = false,
	val isLoading: Boolean = false,
	val isCompleted: Boolean = false
)

