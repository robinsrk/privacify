package dev.robin.privacify.presentation.home

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.theme.Green500
import dev.robin.privacify.ui.theme.Orange500
import dev.robin.privacify.ui.theme.Red500

@Composable
fun HomeScreen() {
	val context = LocalContext.current
	val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(bottom = 16.dp)
		) {
			HeaderSection(
				score = state.privacyScore,
				statusText = when {
					state.privacyScore >= 90 -> "Secure"
					state.privacyScore >= 75 -> "Moderate"
					else -> "At Risk"
				},
				subtitle = state.statusSubtitle
			)
			SystemMonitorSection(state = state, viewModel = viewModel)
			Spacer(modifier = Modifier.height(8.dp))
			QuickActionsSection(
				state = state,
				onActionToggle = { action ->
					viewModel.onQuickActionToggled(action)
				}
			)
			Spacer(modifier = Modifier.height(16.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			) {
				if (state.isScanning) {
					androidx.compose.material3.CircularProgressIndicator(
						modifier = Modifier.size(24.dp),
						strokeWidth = 2.dp,
						color = MaterialTheme.colorScheme.primary
					)
					Spacer(modifier = Modifier.width(12.dp))
				}
				ScanNowButton(
					modifier = Modifier,
					onClick = { viewModel.onScanNowClicked() }
				)
			}
		}
	}
}

@Composable
private fun HeaderSection(
	score: Int,
	statusText: String,
	subtitle: String
) {
	val statusColor = when {
		score >= 90 -> Green500
		score >= 75 -> Orange500
		else -> Red500
	}

	var animationPlayed by remember { mutableStateOf(false) }
	val animatedScore by animateFloatAsState(
		targetValue = if (animationPlayed) score / 100f else 0f,
		animationSpec = tween(durationMillis = 1200),
		label = "scoreArc"
	)
	LaunchedEffect(Unit) { animationPlayed = true }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 48.dp, bottom = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.size(200.dp)
		) {
			Canvas(modifier = Modifier.size(180.dp)) {
				val strokeWidth = 12.dp.toPx()
				// Background arc
				drawArc(
					color = statusColor.copy(alpha = 0.15f),
					startAngle = 135f,
					sweepAngle = 270f,
					useCenter = false,
					style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
				)
				// Progress arc
				drawArc(
					color = statusColor,
					startAngle = 135f,
					sweepAngle = 270f * animatedScore,
					useCenter = false,
					style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
				)
			}
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Text(
					text = score.toString(),
					style = MaterialTheme.typography.displaySmall.copy(
						fontWeight = FontWeight.ExtraBold,
						fontSize = 48.sp
					),
					color = MaterialTheme.colorScheme.onBackground
				)
				Box(
					modifier = Modifier
						.clip(CircleShape)
						.background(statusColor.copy(alpha = 0.15f))
						.padding(horizontal = 12.dp, vertical = 4.dp)
				) {
					Text(
						text = statusText,
						color = statusColor,
						style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
					)
				}
			}
		}
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Privacy Status: $statusText",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(horizontal = 24.dp)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = subtitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(horizontal = 32.dp)
		)
	}
}

@Composable
private fun SystemMonitorSection(state: DashboardUiState, viewModel: DashboardViewModel) {
	val context = LocalContext.current
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		Text(
			text = "System Monitor",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				SensorCard(
					icon = Icons.Outlined.Mic,
					iconTint = Color(0xFF8B5CF6),
					iconBackground = Color(0xFF8B5CF6).copy(alpha = 0.12f),
					title = "Microphone",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
					description = "${state.micAccessCount} apps with access",
					onClick = { Toast.makeText(context, "${state.micAccessCount} apps have mic permission", Toast.LENGTH_SHORT).show() }
				)
				SensorCard(
					icon = Icons.Outlined.LocationOn,
					iconTint = Color(0xFFF59E0B),
					iconBackground = Color(0xFFF59E0B).copy(alpha = 0.12f),
					title = "Location",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
					description = "${state.locationAccessCount} apps with access",
					onClick = { Toast.makeText(context, "${state.locationAccessCount} apps have location permission", Toast.LENGTH_SHORT).show() }
				)
			}
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				SensorCard(
					icon = Icons.Outlined.CameraAlt,
					iconTint = Color(0xFFEF4444),
					iconBackground = Color(0xFFEF4444).copy(alpha = 0.12f),
					title = "Camera",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
					description = "${state.cameraAccessCount} apps with access",
					onClick = { Toast.makeText(context, "${state.cameraAccessCount} apps have camera permission", Toast.LENGTH_SHORT).show() }
				)
				SensorCard(
					icon = Icons.Outlined.Security,
					iconTint = Color(0xFF3B82F6),
					iconBackground = Color(0xFF3B82F6).copy(alpha = 0.12f),
					title = "Network",
					status = if (state.firewallEnabled) "Protected" else "Open",
					statusColor = if (state.firewallEnabled) Green500 else Orange500,
					description = state.secureNetworkSummary,
					onClick = { viewModel.onQuickActionToggled(QuickAction.Firewall) }
				)
			}
		}
	}
}

@Composable
private fun SensorCard(
	icon: ImageVector,
	iconTint: Color,
	iconBackground: Color,
	title: String,
	status: String,
	statusColor: Color,
	description: String,
	onClick: () -> Unit = {}
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
			.clickable { onClick() }
			.padding(16.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(12.dp))
					.background(iconBackground)
					.padding(10.dp),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = iconTint,
					modifier = Modifier.size(20.dp)
				)
			}
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(999.dp))
					.background(statusColor.copy(alpha = 0.12f))
					.padding(horizontal = 10.dp, vertical = 4.dp)
			) {
				Text(
					text = status,
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
					color = statusColor
				)
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onSurface
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = description,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
		)
	}
}

@Composable
private fun QuickActionsSection(
	state: DashboardUiState,
	onActionToggle: (QuickAction) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Quick Actions",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier.padding(horizontal = 4.dp)
		)
		QuickActionRow(
			icon = Icons.Outlined.Lock,
			title = "Lockdown Mode",
			description = if (state.isRooted) "Block all sensors immediately" else "Requires root access",
			checked = state.lockdownEnabled,
			enabled = state.isRooted,
			onToggle = { onActionToggle(QuickAction.Lockdown) }
		)
		QuickActionRow(
			icon = Icons.Outlined.MicOff,
			title = "Mic Kill Switch",
			description = if (state.isRooted) "Root-level driver disable" else "Requires root access",
			checked = state.micDisabled,
			enabled = state.isRooted,
			onToggle = { onActionToggle(QuickAction.MicKill) }
		)
		QuickActionRow(
			icon = Icons.Outlined.CameraAlt,
			title = "Camera Kill",
			description = if (state.isRooted) "Physical sensor disconnect" else "Requires root access",
			checked = state.cameraDisabled,
			enabled = state.isRooted,
			onToggle = { onActionToggle(QuickAction.CameraKill) }
		)
		QuickActionRow(
			icon = Icons.Outlined.Security,
			title = "Global Firewall",
			description = "Block trackers & ads",
			checked = state.firewallEnabled,
			enabled = true,
			onToggle = { onActionToggle(QuickAction.Firewall) }
		)
	}
}

@Composable
private fun QuickActionRow(
	icon: ImageVector,
	title: String,
	description: String,
	checked: Boolean,
	enabled: Boolean = true,
	onToggle: () -> Unit
) {
	val alpha = if (enabled) 1f else 0.45f
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
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
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surfaceVariant)
					.padding(10.dp),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
					modifier = Modifier.size(20.dp)
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			Column {
				Text(
					text = title,
					style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
				)
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = description,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f * alpha)
				)
			}
		}
		Switch(
			checked = checked,
			onCheckedChange = { if (enabled) onToggle() },
			enabled = enabled,
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = MaterialTheme.colorScheme.primary
			)
		)
	}
}

@Composable
private fun ScanNowButton(
	modifier: Modifier,
	onClick: () -> Unit
) {
	Button(
		onClick = onClick,
		shape = RoundedCornerShape(50),
		modifier = modifier,
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.primary,
			contentColor = Color.White
		),
		elevation = ButtonDefaults.buttonElevation(
			defaultElevation = 6.dp,
			pressedElevation = 2.dp
		)
	) {
		Text(
			text = "Scan Now",
			style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
		)
	}
}
