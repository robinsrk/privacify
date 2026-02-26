package dev.robin.privacify.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import dev.robin.privacify.R

sealed class BottomNavDestination(
	val route: String,
	val icon: ImageVector,
	@StringRes val labelRes: Int
) {
	data object HomeDestination : BottomNavDestination(
		route = "home",
		icon = Icons.Filled.Home,
		labelRes = R.string.nav_home
	)

	data object AppsDestination : BottomNavDestination(
		route = "apps",
		icon = Icons.Filled.Apps,
		labelRes = R.string.nav_apps
	)

	data object AnalyticsDestination : BottomNavDestination(
		route = "analytics",
		icon = Icons.Filled.Analytics,
		labelRes = R.string.nav_analytics
	)

	data object SettingsDestination : BottomNavDestination(
		route = "settings",
		icon = Icons.Filled.Settings,
		labelRes = R.string.nav_settings
	)

	companion object {
		val items = listOf(
			HomeDestination,
			AppsDestination,
			AnalyticsDestination,
			SettingsDestination
		)
	}
}

