package dev.robin.privacify.data.onboarding

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dev.robin.privacify.domain.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "onboarding")

class DatastoreOnboardingRepository(
	private val context: Context
) : OnboardingRepository {

	private val keyCompleted = booleanPreferencesKey("completed")

	override val isOnboardingCompleted: Flow<Boolean> =
		context.dataStore.data.map { prefs: Preferences ->
			prefs[keyCompleted] ?: false
		}

	override suspend fun setOnboardingCompleted(completed: Boolean) {
		context.dataStore.edit { prefs ->
			prefs[keyCompleted] = completed
		}
	}
}

