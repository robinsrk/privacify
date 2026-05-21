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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyCard
import dev.robin.privacify.ui.components.PrivacifyChip
import dev.robin.privacify.ui.components.PrivacifyDivider
import dev.robin.privacify.ui.components.PrivacifySectionHeader
import dev.robin.privacify.ui.components.PrivacifySwitch
import dev.robin.privacify.ui.components.PrivacifyWarningBanner
import dev.robin.privacify.ui.theme.BluePrimary
import dev.robin.privacify.ui.theme.Purple500
import dev.robin.privacify.ui.theme.Red500

@Composable
fun SettingsScreen(
	onNavigateToHosts: () -> Unit = {}
) {
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
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			Text(
				text = "Settings",
				style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
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
				onAutomationChanged = viewModel::onAutomationChanged,
				onEditHostsClicked = viewModel::onEditHostsClicked,
				onShellTypeChange = viewModel::setShellType,
				onRefreshShizuku = { viewModel.refreshShizukuStatus(context) },
				onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
				onRequestShizukuAutoStart = { viewModel.requestShizukuAutoStart(context) },
				onOpenShizuku = {
					try {
						val intent = context.packageManager.getLaunchIntentForPackage("rikka.shizuku.privileged.api")
						if (intent != null) {
							intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
							context.startActivity(intent)
						}
					} catch (e: Exception) {
						android.util.Log.e("SettingsScreen", "Failed to open Shizuku", e)
					}
				}
			)
			AboutSection()
			Footer()
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun SettingsRow(
	title: String,
	subtitle: String? = null,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	iconTint: androidx.compose.ui.graphics.Color,
	iconBackground: androidx.compose.ui.graphics.Color,
	onClick: (() -> Unit)? = null,
	trailing: @Composable (() -> Unit)? = null
) {
	PrivacifyCard(onClick = onClick) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.weight(1f)
			) {
				Box(
					modifier = Modifier
						.size(40.dp)
						.clip(RoundedCornerShape(12.dp))
						.background(iconBackground),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = icon,
						contentDescription = null,
						tint = iconTint,
						modifier = Modifier.size(20.dp)
					)
				}
				Spacer(modifier = Modifier.width(12.dp))
				Column {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
					)
					if (subtitle != null) {
						Spacer(modifier = Modifier.height(2.dp))
						Text(
							text = subtitle,
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
			if (trailing != null) {
				trailing()
			}
		}
	}
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
		PrivacifyCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = "Notifications",
					subtitle = "Manage alerts & sounds",
					icon = Icons.Outlined.Notifications,
					iconTint = BluePrimary,
					iconBackground = BluePrimary.copy(alpha = 0.15f),
					onClick = { onNotificationsChanged(!state.notificationsEnabled) },
					trailing = {
						PrivacifySwitch(
							checked = state.notificationsEnabled,
							onCheckedChange = onNotificationsChanged
						)
					}
				)
				PrivacifyDivider()
				SettingsRow(
					title = "Scan Frequency",
					subtitle = state.scanFrequencyLabel,
					icon = Icons.Outlined.Radar,
					iconTint = Purple500,
					iconBackground = Purple500.copy(alpha = 0.15f),
					onClick = onScanFrequencyClick
				)
				PrivacifyDivider()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSection(
	context: android.content.Context,
	state: SettingsUiState,
	onAutomationChanged: (Boolean) -> Unit,
	onEditHostsClicked: () -> Unit,
	onShellTypeChange: (String) -> Unit,
	onRefreshShizuku: () -> Unit,
	onOpenShizuku: () -> Unit,
	onRequestShizukuPermission: () -> Unit,
	onRequestShizukuAutoStart: () -> Unit
) {
	var expanded by mutableStateOf(false)
	val shellOptions = listOf("Auto", "Root", "Shizuku")
	var selectedOption by mutableStateOf(state.shellTypeLabel)
	
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			PrivacifySectionHeader(title = "Advanced")
			PrivacifyChip(text = "Root/Shizuku", color = Purple500)
		}
		PrivacifyCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 12.dp)
				) {
					ExposedDropdownMenuBox(
						expanded = expanded,
						onExpandedChange = { expanded = !expanded }
					) {
						OutlinedTextField(
							value = selectedOption,
							onValueChange = {},
							readOnly = true,
							label = { Text("Shell Type") },
							leadingIcon = {
								Icon(
									imageVector = Icons.Outlined.Terminal,
									contentDescription = null
								)
							},
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
							colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
							modifier = Modifier
								.menuAnchor()
								.fillMaxWidth()
						)
						ExposedDropdownMenu(
							expanded = expanded,
							onDismissRequest = { expanded = false }
						) {
							shellOptions.forEach { option ->
								DropdownMenuItem(
									text = {
										Text(
											text = option,
											fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal
										)
									},
									onClick = {
										selectedOption = option
										val newValue = when (option) {
											"Root" -> "root"
											"Shizuku" -> "shizuku"
											else -> "auto"
										}
										onShellTypeChange(newValue)
										expanded = false
										if (option == "Shizuku" && state.shizukuStatus != "Ready") {
											onRequestShizukuPermission()
										}
										onRefreshShizuku()
									}
								)
							}
						}
					}
				}
				PrivacifyDivider()
				SettingsRow(
					title = "Shizuku Auto-Start",
					subtitle = "Start Shizuku on boot",
					icon = Icons.Outlined.AutoAwesome,
					iconTint = Purple500,
					iconBackground = Purple500.copy(alpha = 0.15f),
					onClick = {
						if (!state.shizukuAutoStart) {
							onRequestShizukuAutoStart()
						}
					},
					trailing = {
						PrivacifySwitch(
							checked = state.shizukuAutoStart,
							onCheckedChange = {
								if (it) onRequestShizukuAutoStart()
							}
						)
					}
				)
			}
		}
		PrivacifyWarningBanner(text = "Features in this section require granted SU or Shizuku privileges.")
		PrivacifyCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = "Auto-Guard",
					subtitle = "Pause kill-switches when in use",
					icon = Icons.Outlined.AutoAwesome,
					iconTint = MaterialTheme.colorScheme.secondary,
					iconBackground = MaterialTheme.colorScheme.secondaryContainer,
					onClick = { onAutomationChanged(!state.automationEnabled) },
					trailing = {
						PrivacifySwitch(
							checked = state.automationEnabled,
							onCheckedChange = onAutomationChanged
						)
					}
				)
				if (state.automationEnabled) {
					PrivacifyDivider()
					SettingsRow(
						title = "Battery Optimization",
						subtitle = "Tap to disable for reliable operation",
						icon = Icons.Outlined.Shield,
						iconTint = Purple500,
						iconBackground = Purple500.copy(alpha = 0.15f),
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
				PrivacifyDivider()
				SettingsRow(
					title = "Edit Hosts File",
					subtitle = "/etc/hosts direct write",
					icon = Icons.Outlined.Dns,
					iconTint = BluePrimary,
					iconBackground = BluePrimary.copy(alpha = 0.15f),
					onClick = onEditHostsClicked
				)
			}
		}
	}
}

@Composable
private fun AboutSection() {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		PrivacifySectionHeader(title = "About")
		PrivacifyCard {
			Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
				SettingsRow(
					title = "Open Source",
					subtitle = "View source on GitHub",
					icon = Icons.Outlined.Code,
					iconTint = MaterialTheme.colorScheme.primary,
					iconBackground = MaterialTheme.colorScheme.primaryContainer
				)
				PrivacifyDivider()
				SettingsRow(
					title = "Privacy Policy",
					icon = Icons.Outlined.Policy,
					iconTint = MaterialTheme.colorScheme.secondary,
					iconBackground = MaterialTheme.colorScheme.secondaryContainer
				)
				PrivacifyDivider()
				SettingsRow(
					title = "Version",
					subtitle = "1.0.0",
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
			text = "Privacy Control Center © 2025",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Sign Out",
			style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
			color = Red500,
			modifier = Modifier.clickable {}
		)
	}
}
