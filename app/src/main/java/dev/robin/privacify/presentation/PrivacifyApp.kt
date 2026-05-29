package dev.robin.privacify.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.robin.privacify.presentation.analytics.AnalyticsScreen
import dev.robin.privacify.presentation.apps.AppsScreen
import dev.robin.privacify.presentation.home.HomeScreen
import dev.robin.privacify.presentation.lockdown.LockdownScreen
import dev.robin.privacify.presentation.navigation.BottomNavDestination
import dev.robin.privacify.presentation.onboarding.OnboardingRoute
import dev.robin.privacify.presentation.settings.HostsEditorScreen
import dev.robin.privacify.presentation.settings.SettingsScreen
import dev.robin.privacify.ui.theme.MdSpacing

private val COMPACT_THRESHOLD = 600

@Composable
fun PrivacifyApp() {
	val navController = rememberNavController()

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
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
}

@Composable
private fun MainNavigationShell() {
	val navController = rememberNavController()
	val backstackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = backstackEntry?.destination?.route
	val configuration = LocalConfiguration.current
	val isCompact = configuration.screenWidthDp < COMPACT_THRESHOLD

	val destinations = BottomNavDestination.items
	val showBottomBar = currentRoute in destinations.map { it.route }

	Box(modifier = Modifier.fillMaxSize()) {
		Scaffold(
			containerColor = MaterialTheme.colorScheme.background
		) { innerPadding ->
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				Surface(
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight(),
					color = MaterialTheme.colorScheme.background
				) {
					NavHost(
						navController = navController,
						startDestination = BottomNavDestination.HomeDestination.route,
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

		AnimatedVisibility(
			visible = showBottomBar && isCompact,
			enter = slideInVertically(animationSpec = tween(300)) { it },
			exit = slideOutVertically(animationSpec = tween(300)) { it },
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.zIndex(1f)
		) {
			PrivacifyFloatingToolbar(
				destinations = destinations,
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

@Composable
private fun PrivacifyFloatingToolbar(
	destinations: List<BottomNavDestination>,
	currentRoute: String?,
	onDestinationSelected: (BottomNavDestination) -> Unit
) {
	Surface(
		modifier = Modifier
			.windowInsetsPadding(WindowInsets.navigationBars)
			.padding(start = 12.dp, end = 12.dp, bottom = 0.dp),
		shape = RoundedCornerShape(999.dp),
		color = MaterialTheme.colorScheme.primary,
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
	val itemWidth by animateDpAsState(
		targetValue = if (selected) 104.dp else 56.dp,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessLow
		),
		label = "nav_width"
	)

	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.clickable { onClick() }
			.width(itemWidth)
			.background(
				if (selected) Color.White.copy(alpha = 0.2f)
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
				tint = if (selected) Color.White
				else Color.White.copy(alpha = 0.6f)
			)
			if (selected) {
				Text(
					text = stringResource(destination.labelRes),
					style = MaterialTheme.typography.labelMedium.copy(
						fontWeight = FontWeight.Black,
						fontSize = 12.sp
					),
					color = Color.White,
					maxLines = 1
				)
			}
		}
	}
}
