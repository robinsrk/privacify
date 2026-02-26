package dev.robin.privacify.domain.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
	val isOnboardingCompleted: Flow<Boolean>
	suspend fun setOnboardingCompleted(completed: Boolean)
}

