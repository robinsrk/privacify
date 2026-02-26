package dev.robin.privacify.core.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
	Dark, System, Light
}

object ThemePreferenceManager {
	private val context = dev.robin.privacify.core.utils.AppContextProvider.context
	private val prefs = dev.robin.privacify.core.settings.UserPreferencesManager.getInstance(context)

	val themeMode: StateFlow<AppThemeMode> = prefs.themeMode

	fun setTheme(mode: AppThemeMode) {
		prefs.setTheme(mode)
	}

	fun cycleTheme() {
		prefs.cycleTheme()
	}
}
