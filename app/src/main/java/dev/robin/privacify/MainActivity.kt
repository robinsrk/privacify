package dev.robin.privacify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
import dev.robin.privacify.presentation.PrivacifyApp
import dev.robin.privacify.ui.theme.PrivacifyTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			val themeMode by ThemePreferenceManager.themeMode.collectAsState()
			val darkTheme = when (themeMode) {
				AppThemeMode.Dark -> true
				AppThemeMode.Light -> false
				AppThemeMode.System -> isSystemInDarkTheme()
			}
			PrivacifyTheme(darkTheme = darkTheme) {
				PrivacifyApp()
			}
		}
	}
}
