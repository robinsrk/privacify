package dev.robin.privacify.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.core.provider.ProFeature
import dev.robin.privacify.ui.components.PrivacifyAutoGuardCard
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyChip
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.components.PrivacifyProDialog
import dev.robin.privacify.ui.components.PrivacifySectionHeader
import dev.robin.privacify.ui.components.PrivacifyStatusIndicator
import dev.robin.privacify.ui.components.PrivacifySwitch
import dev.robin.privacify.ui.components.PrivacifyWarningBanner
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant

@Composable
fun SettingsScreen() {
	val context = LocalContext.current
	val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.refreshShizukuStatus(context)
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = "Settings",
				style = MaterialTheme.typography.headlineLarge,
				fontWeight = FontWeight.Black,
				modifier = Modifier.padding(horizontal = 4.dp)
			)

		ProtectionSection(
			enabled = state.automationEnabled,
			onToggle = viewModel::onAutomationChanged,
			autostartEnabled = state.autostartEnabled,
			onAutostartToggle = viewModel::onAutostartChanged
		)

			GeneralSection(
				state = state,
				onNotificationsChanged = viewModel::onNotificationsChanged,
				onScanFrequencyClick = viewModel::onScanFrequencyClicked,
				onThemeClick = viewModel::onThemeClicked
			)
		AdvancedSection(
				context = context,
				state = state,
				onShellTypeChange = viewModel::setShellType,
				onRefreshShizuku = { viewModel.refreshShizukuStatus(context) },
				onRequestShizukuPermission = { viewModel.requestShizukuPermission() }
			)
			AboutSection()
			Footer()

			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

@Composable
private fun ProtectionSection(
	enabled: Boolean,
	onToggle: (Boolean) -> Unit,
	autostartEnabled: Boolean,
	onAutostartToggle: (Boolean) -> Unit
) {
	var showAutostartProDialog by remember { mutableStateOf(false) }
	val isPro = ProFeature.isAutoGuardAvailable()

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "PROTECTION",
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
			PrivacifyBadge(text = "FEATURED", color = AutoGuardPrimary)
		}
		PrivacifyAutoGuardCard(
			enabled = enabled,
			onToggle = onToggle
		)
		PrivacifyExpressiveCard {
			SettingsRow(
				title = "App Autostart",
				subtitle = "Automatically start at boot",
				icon = Icons.Outlined.PowerSettingsNew,
				iconTint = AutoGuardPrimary,
				iconBackground = AutoGuardPrimary.copy(alpha = 0.12f),
				trailing = {
					PrivacifySwitch(
						checked = autostartEnabled,
						onCheckedChange = { newValue ->
							if (isPro) {
								onAutostartToggle(newValue)
							} else {
								showAutostartProDialog = true
							}
						}
					)
				}
			)
		}
	}

	if (showAutostartProDialog) {
		PrivacifyProDialog(
			featureName = "App Autostart",
			description = "Automatically start the app at boot for persistent protection.",
			onDismiss = { showAutostartProDialog = false }
		)
	}
}

@Composable
private fun SettingsRow(
	title: String,
	subtitle: String? = null,
	icon: ImageVector,
	iconTint: Color,
	iconBackground: Color,
	onClick: (() -> Unit)? = null,
	trailing: @Composable (() -> Unit)? = null
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.then(
				if (onClick != null) Modifier.clickable { onClick() }
				else Modifier
			)
			.padding(horizontal = 16.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = iconTint,
			background = iconBackground,
			size = 48,
			iconSize = 24
		)
		Spacer(modifier = Modifier.width(16.dp))
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
			if (subtitle != null) {
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		if (trailing != null) {
			Spacer(modifier = Modifier.width(12.dp))
			trailing()
		}
	}
}

@Composable
private fun GradientDivider() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(1.dp)
			.padding(start = 72.dp, end = 16.dp)
			.background(
				Brush.horizontalGradient(
					colors = listOf(
						MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
						Color.Transparent
					)
				)
			)
	)
}

@Composable
private fun GeneralSection(
	state: SettingsUiState,
	onNotificationsChanged: (Boolean) -> Unit,
	onScanFrequencyClick: () -> Unit,
	onThemeClick: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		PrivacifySectionHeader(title = "General")
		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = "Notifications",
					subtitle = "Receive alerts when sensors are accessed",
					icon = Icons.Outlined.Notifications,
					iconTint = BlueVibrant,
					iconBackground = BlueVibrant.copy(alpha = 0.12f),
					trailing = {
						PrivacifySwitch(
							checked = state.notificationsEnabled,
							onCheckedChange = onNotificationsChanged
						)
					}
				)
				GradientDivider()
				SettingsRow(
					title = "Scan Frequency",
					subtitle = state.scanFrequencyLabel,
					icon = Icons.Outlined.Radar,
					iconTint = PurpleVibrant,
					iconBackground = PurpleVibrant.copy(alpha = 0.12f),
					onClick = onScanFrequencyClick
				)
				GradientDivider()
				SettingsRow(
					title = "App Theme",
					subtitle = state.themeLabel,
					icon = Icons.Outlined.DarkMode,
					iconTint = MaterialTheme.colorScheme.tertiary,
					iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
					onClick = onThemeClick
				)
			}
		}
	}
}

@Composable
private fun AdvancedSection(
	context: android.content.Context,
	state: SettingsUiState,
	onShellTypeChange: (String) -> Unit,
	onRefreshShizuku: () -> Unit,
	onRequestShizukuPermission: () -> Unit
) {
	val shellOptions = listOf("Auto", "Root", "Shizuku")

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "ADVANCED",
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (state.shizukuStatus.isNotEmpty()) {
					val statusColor = when {
						state.shizukuStatus == "Ready" -> GreenVibrant
						state.shizukuStatus == "Unknown" -> MaterialTheme.colorScheme.onSurfaceVariant
						else -> RedVibrant
					}
					PrivacifyStatusIndicator(
						status = state.shizukuStatus,
						color = statusColor
					)
				}
				PrivacifyChip(text = "Root/Shizuku", color = PurpleVibrant)
			}
		}

		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 14.dp)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						PrivacifyIconBox(
							icon = Icons.Outlined.Terminal,
							tint = PurpleVibrant,
							background = PurpleVibrant.copy(alpha = 0.12f),
							size = 48,
							iconSize = 24
						)
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Shell Type",
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold
							)
							Spacer(modifier = Modifier.height(6.dp))
							Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								shellOptions.forEach { option ->
									val isSelected = option == state.shellTypeLabel
									Box(
										modifier = Modifier
											.clip(MaterialTheme.shapes.small)
											.background(
												if (isSelected) PurpleVibrant
												else MaterialTheme.colorScheme.surfaceVariant
											)
											.clickable {
												val newValue = when (option) {
													"Root" -> "root"
													"Shizuku" -> "shizuku"
													else -> "auto"
												}
												onShellTypeChange(newValue)
												if (option == "Shizuku" && state.shizukuStatus != "Ready") {
													onRequestShizukuPermission()
												}
												onRefreshShizuku()
											}
											.padding(horizontal = 16.dp, vertical = 8.dp)
									) {
										Text(
											text = option,
											style = MaterialTheme.typography.labelLarge,
											fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
											color = if (isSelected) Color.White
											else MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								}
							}
						}
					}
				}
				}
		}

		PrivacifyWarningBanner(
			text = "Root or Shizuku privileges required for advanced features."
		)

		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				if (state.automationEnabled) {
					SettingsRow(
						title = "Battery Optimization",
						subtitle = "Disable for reliable background operation",
						icon = Icons.Outlined.Shield,
						iconTint = PurpleVibrant,
						iconBackground = PurpleVibrant.copy(alpha = 0.12f),
						onClick = {
							try {
								val intent = android.content.Intent().apply {
									action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
									data = android.net.Uri.parse("package:${context.packageName}")
								}
								context.startActivity(intent)
							} catch (e: Exception) {
								try {
									val intent = android.content.Intent().apply {
										action = android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
									}
									context.startActivity(intent)
								} catch (e2: Exception) {
									android.util.Log.e("SettingsScreen", "Failed to open battery settings", e2)
								}
							}
						}
					)
					}
			}
		}
	}
}

@Composable
private fun AboutSection() {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current
	val versionName = remember {
		try {
			context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
		} catch (_: Exception) { "1.0.0" }
	}

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		PrivacifySectionHeader(title = "About")
		PrivacifyExpressiveCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = "Open Source",
					subtitle = "View source on GitHub",
					icon = Icons.Outlined.Code,
					iconTint = MaterialTheme.colorScheme.primary,
					iconBackground = MaterialTheme.colorScheme.primaryContainer,
					onClick = {
						try {
							uriHandler.openUri("https://github.com/robinsrk/privacify")
						} catch (_: Exception) {}
					}
				)
				GradientDivider()
				SettingsRow(
					title = "Version",
					subtitle = versionName,
					icon = Icons.Outlined.Info,
					iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
					iconBackground = MaterialTheme.colorScheme.surfaceVariant
				)
			}
		}
	}
}

@Composable
private fun Footer() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = "Privacify Control Center",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
		)

	}
}
