package dev.robin.privacify.presentation.home

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyAutoGuardCard
import dev.robin.privacify.ui.components.PrivacifyChip
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.components.PrivacifySectionHeader
import dev.robin.privacify.ui.components.PrivacifySwitch
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GradientEnd
import dev.robin.privacify.ui.theme.GradientMid
import dev.robin.privacify.ui.theme.GradientStart
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import dev.robin.privacify.ui.theme.ScoreGreen
import dev.robin.privacify.ui.theme.ScoreOrange
import dev.robin.privacify.ui.theme.ScoreRed

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
			Spacer(modifier = Modifier.height(16.dp))

			PrivacifyAutoGuardCard(
				enabled = state.automationEnabled,
				onToggle = { viewModel.onAutoGuardToggled(it) },
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			HeaderSection(
				score = state.privacyScore,
				statusText = when {
					state.privacyScore >= 90 -> "Secure"
					state.privacyScore >= 75 -> "Moderate"
					else -> "At Risk"
				},
				subtitle = state.statusSubtitle
			)
			SystemMonitorSection(state = state)
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
					CircularProgressIndicator(
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
		score >= 90 -> ScoreGreen
		score >= 75 -> ScoreOrange
		else -> ScoreRed
	}

	val gradientColors = when {
		score >= 90 -> listOf(GradientStart, GradientMid)
		score >= 75 -> listOf(OrangeVibrant, AmberVibrant)
		else -> listOf(RedVibrant, Color(0xFF991B1B))
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
			.padding(bottom = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.size(200.dp)
		) {
			Canvas(modifier = Modifier.size(180.dp)) {
				val strokeWidth = 14.dp.toPx()
				drawArc(
					color = statusColor.copy(alpha = 0.1f),
					startAngle = 135f,
					sweepAngle = 270f,
					useCenter = false,
					style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
				)
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
					style = MaterialTheme.typography.displayLarge.copy(
						fontWeight = FontWeight.Black,
						fontSize = 48.sp
					),
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.height(8.dp))
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(999.dp))
						.background(
							Brush.linearGradient(gradientColors)
						)
						.padding(horizontal = 16.dp, vertical = 6.dp)
				) {
					Text(
						text = statusText,
						style = MaterialTheme.typography.labelLarge,
						fontWeight = FontWeight.Black,
						color = Color.White
					)
				}
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		Text(
			text = subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(horizontal = 32.dp)
		)
	}
}

@Composable
private fun SystemMonitorSection(state: DashboardUiState) {
	val context = LocalContext.current
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		PrivacifySectionHeader(title = "System Monitor")
		Spacer(modifier = Modifier.height(8.dp))
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
					iconTint = MaterialTheme.colorScheme.primary,
					iconBackground = MaterialTheme.colorScheme.primaryContainer,
					title = "Microphone",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
					description = "${state.micAccessCount} apps with access",
					onClick = { Toast.makeText(context, "${state.micAccessCount} apps have mic permission", Toast.LENGTH_SHORT).show() }
				)
				SensorCard(
					icon = Icons.Outlined.LocationOn,
					iconTint = AmberVibrant,
					iconBackground = AmberVibrant.copy(alpha = 0.12f),
					title = "Location",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
					iconTint = RedVibrant,
					iconBackground = RedVibrant.copy(alpha = 0.12f),
					title = "Camera",
					status = "Not in use",
					statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
					description = "${state.cameraAccessCount} apps with access",
					onClick = { Toast.makeText(context, "${state.cameraAccessCount} apps have camera permission", Toast.LENGTH_SHORT).show() }
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
	PrivacifyExpressiveCard(onClick = onClick) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				PrivacifyIconBox(
					icon = icon,
					tint = iconTint,
					background = iconBackground,
					size = 40,
					iconSize = 20
				)
				PrivacifyChip(text = status, color = statusColor)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurface
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
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
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "QUICK ACTIONS",
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(6.dp)
						.clip(CircleShape)
						.background(if (state.isRooted) GreenVibrant else RedVibrant)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = if (state.isRooted) "Root Ready" else "No Root",
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Bold,
					color = if (state.isRooted) GreenVibrant else MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		PrivacifyExpressiveCard {
			Column(modifier = Modifier.padding(8.dp)) {
				Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
					QuickActionRow(
						icon = Icons.Outlined.Lock,
						iconTint = MaterialTheme.colorScheme.primary,
						iconBackground = MaterialTheme.colorScheme.primaryContainer,
						title = "Lockdown Mode",
						description = if (state.isRooted) "Block all sensors immediately" else "Requires ${
							if (state.shellType == "shizuku") "Shizuku" else "Root"
						} access",
						checked = state.lockdownEnabled,
						enabled = state.isRooted,
						onToggle = { onActionToggle(QuickAction.Lockdown) }
					)
					GradientDivider()
					QuickActionRow(
						icon = Icons.Outlined.MicOff,
						iconTint = RedVibrant,
						iconBackground = RedVibrant.copy(alpha = 0.12f),
						title = "Mic Kill Switch",
						description = if (state.isRooted) "Disable microphone at system level" else "Requires ${
							if (state.shellType == "shizuku") "Shizuku" else "Root"
						} access",
						checked = state.micDisabled,
						enabled = state.isRooted,
						onToggle = { onActionToggle(QuickAction.MicKill) }
					)
					GradientDivider()
					QuickActionRow(
						icon = Icons.Outlined.CameraAlt,
						iconTint = OrangeVibrant,
						iconBackground = OrangeVibrant.copy(alpha = 0.12f),
						title = "Camera Kill",
						description = if (state.isRooted) "Disable camera at system level" else "Requires ${
							if (state.shellType == "shizuku") "Shizuku" else "Root"
						} access",
						checked = state.cameraDisabled,
						enabled = state.isRooted,
						onToggle = { onActionToggle(QuickAction.CameraKill) }
					)
				}
			}
		}
	}
}

@Composable
private fun QuickActionRow(
	icon: ImageVector,
	iconTint: Color,
	iconBackground: Color,
	title: String,
	description: String,
	checked: Boolean,
	enabled: Boolean = true,
	onToggle: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) { onToggle() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = iconTint,
			background = iconBackground,
			size = 40,
			iconSize = 20
		)
		Spacer(modifier = Modifier.width(12.dp))
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Bold,
				color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f)
			)
		}
		PrivacifySwitch(
			checked = checked,
			onCheckedChange = { if (enabled) onToggle() },
			enabled = enabled
		)
	}
}

@Composable
private fun GradientDivider() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(1.dp)
			.padding(start = 68.dp, end = 16.dp)
			.background(
				Brush.horizontalGradient(
					colors = listOf(
						MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
						Color.Transparent
					)
				)
			)
	)
}

@Composable
private fun ScanNowButton(
	modifier: Modifier,
	onClick: () -> Unit
) {
	Button(
		onClick = onClick,
		shape = RoundedCornerShape(16.dp),
		modifier = modifier.height(48.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.primary,
			contentColor = MaterialTheme.colorScheme.onPrimary
		),
		elevation = ButtonDefaults.buttonElevation(
			defaultElevation = 4.dp,
			pressedElevation = 8.dp
		)
	) {
		Icon(
			imageVector = Icons.Filled.Security,
			contentDescription = null,
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = "Scan Now",
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Black
		)
	}
}
