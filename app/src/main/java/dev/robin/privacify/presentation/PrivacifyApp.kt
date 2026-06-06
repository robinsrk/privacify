package dev.robin.privacify.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.robin.privacify.data.onboarding.DatastoreOnboardingRepository
import dev.robin.privacify.presentation.analytics.AnalyticsScreen
import dev.robin.privacify.presentation.apps.AppsScreen
import dev.robin.privacify.presentation.home.HomeScreen
import dev.robin.privacify.presentation.lockdown.LockdownScreen
import dev.robin.privacify.presentation.navigation.BottomNavDestination
import dev.robin.privacify.presentation.onboarding.OnboardingRoute
import dev.robin.privacify.presentation.settings.HostsEditorScreen
import dev.robin.privacify.presentation.settings.SettingsScreen
import kotlinx.coroutines.flow.first

@Composable
fun PrivacifyApp() {
	val navController = rememberNavController()
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		val repo = DatastoreOnboardingRepository(context.applicationContext)
		if (!repo.isOnboardingCompleted.first()) {
			navController.navigate("onboarding")
		}
	}

	val backstackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = backstackEntry?.destination?.route

	val mainRoutes = BottomNavDestination.items.map { it.route }
	val showBottomBar = currentRoute in mainRoutes

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Scaffold(
			containerColor = MaterialTheme.colorScheme.background,
			bottomBar = {
				AnimatedVisibility(
					visible = showBottomBar,
					enter = slideInVertically(animationSpec = tween(300)) { it },
					exit = slideOutVertically(animationSpec = tween(300)) { it }
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(bottom = 8.dp),
						contentAlignment = Alignment.Center
					) {
						PrivacifyFloatingToolbar(
							currentRoute = currentRoute,
							onDestinationSelected = { destination ->
								navController.navigate(destination.route) {
									popUpTo(navController.graph.findStartDestination().id) {
										saveState = true
									}
									launchSingleTop = true
									restoreState = true
								}
							}
						)
					}
				}
			}
		) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = "home",
				modifier = Modifier.padding(innerPadding),
				enterTransition = { fadeIn(tween(250)) },
				exitTransition = { fadeOut(tween(250)) }
			) {
				composable(BottomNavDestination.HomeDestination.route) {
					HomeScreen()
				}
				composable(BottomNavDestination.AppsDestination.route) {
					AppsScreen()
				}
				composable(BottomNavDestination.AnalyticsDestination.route) {
					AnalyticsScreen()
				}
				composable(BottomNavDestination.SettingsDestination.route) {
					SettingsScreen()
				}
				composable("onboarding") {
					OnboardingRoute(
						onFinished = {
							navController.popBackStack("home", inclusive = false)
						}
					)
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

@Composable
private fun PrivacifyFloatingToolbar(
	currentRoute: String?,
	onDestinationSelected: (BottomNavDestination) -> Unit
) {
	val destinations = BottomNavDestination.items

	Surface(
		modifier = Modifier
			.windowInsetsPadding(WindowInsets.navigationBars)
			.padding(start = 12.dp, end = 12.dp, bottom = 0.dp),
		shape = RoundedCornerShape(999.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		tonalElevation = 3.dp,
		shadowElevation = 6.dp
	) {
		Row(
			modifier = Modifier
				.padding(horizontal = 12.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			destinations.forEach { destination ->
				FloatingToolbarItem(
					destination = destination,
					selected = currentRoute == destination.route,
					onClick = { onDestinationSelected(destination) }
				)
			}
		}
	}
}

@Composable
private fun FloatingToolbarItem(
	destination: BottomNavDestination,
	selected: Boolean,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.clickable { onClick() }
			.widthIn(min = 56.dp)
			.animateContentSize(
				spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			)
			.background(
				if (selected) MaterialTheme.colorScheme.primary
				else Color.Transparent
			)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Icon(
				imageVector = destination.icon,
				contentDescription = stringResource(destination.labelRes),
				modifier = Modifier.size(24.dp),
				tint = if (selected) MaterialTheme.colorScheme.onPrimary
				else MaterialTheme.colorScheme.onSurfaceVariant
			)
			if (selected) {
				Text(
					text = stringResource(destination.labelRes),
					style = MaterialTheme.typography.labelMedium.copy(
						fontWeight = FontWeight.Black,
						fontSize = 12.sp
					),
					color = MaterialTheme.colorScheme.onPrimary,
					maxLines = 1
				)
			}
		}
	}
}
