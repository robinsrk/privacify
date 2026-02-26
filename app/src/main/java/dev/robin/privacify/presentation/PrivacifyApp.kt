package dev.robin.privacify.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.robin.privacify.presentation.analytics.AnalyticsScreen
import dev.robin.privacify.presentation.apps.AppDetailRoute
import dev.robin.privacify.presentation.apps.AppsScreen
import dev.robin.privacify.presentation.home.HomeScreen
import dev.robin.privacify.presentation.lockdown.LockdownScreen
import dev.robin.privacify.presentation.navigation.BottomNavDestination
import dev.robin.privacify.presentation.onboarding.OnboardingRoute
import dev.robin.privacify.presentation.settings.HostsEditorScreen
import dev.robin.privacify.presentation.settings.SettingsScreen

@Composable
fun PrivacifyApp() {
	val navController = rememberNavController()

	NavHost(
		navController = navController,
		startDestination = "onboarding",
		enterTransition = { fadeIn(tween(200)) },
		exitTransition = { fadeOut(tween(200)) }
	) {
		composable("onboarding") {
			OnboardingRoute(
				onFinished = {
					navController.navigate("main") {
						popUpTo("onboarding") { inclusive = true }
					}
				}
			)
		}

		composable("main") {
			MainNavigationShell()
		}
	}
}

@Composable
private fun MainNavigationShell() {
	val navController = rememberNavController()
	val backstackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = backstackEntry?.destination?.route

	val destinations = BottomNavDestination.items
	val showBottomBar = currentRoute in destinations.map { it.route }

	Scaffold(
		bottomBar = {
			if (showBottomBar) {
				NavigationBar(
					containerColor = MaterialTheme.colorScheme.surface,
					tonalElevation = 0.dp
				) {
					destinations.forEach { destination ->
						val selected = currentRoute == destination.route
						NavigationBarItem(
							selected = selected,
							onClick = {
								navController.navigate(destination.route) {
									popUpTo(navController.graph.findStartDestination().id) {
										saveState = true
									}
									launchSingleTop = true
									restoreState = true
								}
							},
							icon = {
								Icon(
									imageVector = destination.icon,
									contentDescription = stringResource(destination.labelRes)
								)
							},
							label = {
								Text(
									text = stringResource(destination.labelRes),
									style = MaterialTheme.typography.labelSmall.copy(
										fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
									)
								)
							},
							colors = NavigationBarItemDefaults.colors(
								selectedIconColor = MaterialTheme.colorScheme.primary,
								selectedTextColor = MaterialTheme.colorScheme.primary,
								unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
								unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
								indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
							)
						)
					}
				}
			}
		}
	) { innerPadding ->
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding),
			color = MaterialTheme.colorScheme.background
		) {
			NavHost(
				navController = navController,
				startDestination = BottomNavDestination.HomeDestination.route,
				enterTransition = { EnterTransition.None },
				exitTransition = { ExitTransition.None }
			) {
				composable(BottomNavDestination.HomeDestination.route) {
					HomeScreen()
				}
				composable(BottomNavDestination.AppsDestination.route) {
					AppsScreen(
						onAppSelected = { app ->
							navController.navigate("app_detail/${app.packageName}")
						}
					)
				}
				composable(BottomNavDestination.AnalyticsDestination.route) {
					AnalyticsScreen()
				}
				composable(BottomNavDestination.SettingsDestination.route) {
					SettingsScreen(
						onNavigateToHosts = {
							navController.navigate("hosts_editor")
						}
					)
				}
				composable("app_detail/{packageName}") { backStackEntry ->
					val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
					AppDetailRoute(packageName = packageName)
				}
				composable("lockdown") {
					LockdownScreen(
						onBack = { navController.popBackStack() }
					)
				}
				composable("hosts_editor") {
					HostsEditorScreen(
						onBack = { navController.popBackStack() }
					)
				}
			}
		}
	}
}

