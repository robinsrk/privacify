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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VpnLock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
	onNavigateToHosts: () -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = "Settings",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
			)
			GeneralSection(
				state = state,
				onNotificationsChanged = viewModel::onNotificationsChanged,
				onScanFrequencyClick = viewModel::onScanFrequencyClicked,
				onThemeClick = viewModel::onThemeClicked
			)
			FirewallSection(
				state = state,
				onVpnChanged = viewModel::onVpnChanged,
				onBlockDataChanged = viewModel::onBlockDataChanged
			)
			AdvancedSection(
				state = state,
				onSystemBlockingChanged = viewModel::onSystemBlockingChanged,
				onEditHostsClicked = viewModel::onEditHostsClicked
			)
			AboutSection()
			Footer()
			Spacer(modifier = Modifier.height(8.dp))
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
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = "GENERAL",
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(horizontal = 4.dp)
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(MaterialTheme.colorScheme.surface)
		) {
			SettingsRowSwitch(
				icon = Icons.Outlined.Notifications,
				title = "Notifications",
				subtitle = "Manage alerts & sounds",
				checked = state.notificationsEnabled,
				onCheckedChange = onNotificationsChanged
			)
			DividerRow()
			SimpleSettingsRow(
				icon = Icons.Outlined.Radar,
				title = "Scan Frequency",
				endText = state.scanFrequencyLabel,
				onClick = onScanFrequencyClick
			)
			DividerRow()
			SimpleSettingsRow(
				icon = Icons.Outlined.DarkMode,
				title = "App Theme",
				subtitle = state.themeLabel,
				onClick = onThemeClick
			)
		}
	}
}

@Composable
private fun FirewallSection(
	state: SettingsUiState,
	onVpnChanged: (Boolean) -> Unit,
	onBlockDataChanged: (Boolean) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = "FIREWALL",
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(horizontal = 4.dp)
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(MaterialTheme.colorScheme.surface)
		) {
			SettingsRowSwitch(
				icon = Icons.Outlined.VpnLock,
				title = "VPN Service",
				subtitle = if (state.vpnEnabled) "Active • Local firewall" else "Inactive",
				checked = state.vpnEnabled,
				onCheckedChange = onVpnChanged
			)
			DividerRow()
			SettingsRowSwitch(
				icon = Icons.Outlined.Shield,
				title = "Block Data",
				subtitle = "Prevent background leaks",
				checked = state.blockDataEnabled,
				onCheckedChange = onBlockDataChanged
			)
		}
	}
}

@Composable
private fun AdvancedSection(
	state: SettingsUiState,
	onSystemBlockingChanged: (Boolean) -> Unit,
	onEditHostsClicked: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "ADVANCED",
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
			)
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(999.dp))
					.background(Color(0xFF7C3AED).copy(alpha = 0.15f))
					.padding(horizontal = 8.dp, vertical = 2.dp)
			) {
				Text(
					text = "Root Only",
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
					color = Color(0xFF8B5CF6)
				)
			}
		}
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(MaterialTheme.colorScheme.surface)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.background(Color(0xFF7C3AED).copy(alpha = 0.08f))
					.padding(12.dp)
			) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.Top
				) {
					Box(
						modifier = Modifier
							.size(20.dp)
							.clip(CircleShape)
							.background(Color(0xFF7C3AED).copy(alpha = 0.2f)),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "!",
							style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
							color = Color(0xFF8B5CF6)
						)
					}
					Text(
						text = "Features in this section require granted SU privileges. Incorrect configuration may affect system stability.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
					)
				}
			}
			SettingsRowSwitch(
				icon = Icons.Outlined.AdminPanelSettings,
				title = "System-level Blocking",
				subtitle = "Modify IPTables",
				checked = state.systemBlockingEnabled,
				onCheckedChange = onSystemBlockingChanged
			)
			DividerRow()
			SimpleSettingsRow(
				icon = Icons.Outlined.Dns,
				title = "Edit Hosts File",
				subtitle = "/etc/hosts direct write",
				onClick = onEditHostsClicked
			)
		}
	}
}

@Composable
private fun AboutSection() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = "ABOUT",
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(horizontal = 4.dp)
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(MaterialTheme.colorScheme.surface)
		) {
			SimpleSettingsRow(
				icon = Icons.Outlined.Code,
				title = "Open Source",
				subtitle = "View source on GitHub"
			)
			DividerRow()
			SimpleSettingsRow(
				icon = Icons.Outlined.Policy,
				title = "Privacy Policy"
			)
			DividerRow()
			SimpleSettingsRow(
				icon = Icons.Outlined.Info,
				title = "Version",
				subtitle = "1.0.0"
			)
		}
	}
}

@Composable
private fun Footer() {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = "Privacy Control Center © 2025",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = "Sign Out",
			style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
			color = Color(0xFFEF4444),
			modifier = Modifier.clickable {}
		)
	}
}

@Composable
private fun SettingsRowSwitch(
	icon: ImageVector,
	title: String,
	subtitle: String? = null,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onCheckedChange(!checked) }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.weight(1f)
		) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surfaceVariant),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(20.dp)
				)
			}
			Column {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
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
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = MaterialTheme.colorScheme.primary
			)
		)
	}
}

@Composable
private fun SimpleSettingsRow(
	icon: ImageVector,
	title: String,
	subtitle: String? = null,
	endText: String? = null,
	onClick: () -> Unit = {}
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.weight(1f)
		) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surfaceVariant),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(20.dp)
				)
			}
			Column {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
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
		if (endText != null) {
			Text(
				text = endText,
				style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
				color = MaterialTheme.colorScheme.primary
			)
		}
	}
}

@Composable
private fun DividerRow() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.height(1.dp)
			.background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
	)
}
