package dev.robin.privacify.presentation.lockdown

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LockdownScreen(
	onBack: () -> Unit = {}
) {
	val viewModel: LockdownViewModel = viewModel(factory = LockdownViewModel.Factory)
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp)
		) {
			// Header with back
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onBack) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = "Back"
					)
				}
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Lockdown Mode",
					style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
				)
			}

			Spacer(modifier = Modifier.height(24.dp))

			// Panic button
			PanicButton(
				activated = state.lockdownActive,
				onToggle = { viewModel.toggleLockdown() }
			)

			Spacer(modifier = Modifier.height(24.dp))

			// Status
			StatusBanner(active = state.lockdownActive)

			Spacer(modifier = Modifier.height(24.dp))

			// Sensor toggles
			Text(
				text = "SENSOR CONTROLS",
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
				modifier = Modifier.padding(horizontal = 4.dp)
			)
			Spacer(modifier = Modifier.height(8.dp))

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(20.dp))
					.background(MaterialTheme.colorScheme.surface),
				verticalArrangement = Arrangement.spacedBy(0.dp)
			) {
				SensorToggle(
					icon = Icons.Outlined.Mic,
					title = "Microphone",
					subtitle = "Disable system microphone",
					enabled = state.micKilled,
					onToggle = { viewModel.toggleMic() }
				)
				DividerRow()
				SensorToggle(
					icon = Icons.Outlined.CameraAlt,
					title = "Camera",
					subtitle = "Disable all cameras",
					enabled = state.cameraKilled,
					onToggle = { viewModel.toggleCamera() }
				)
				DividerRow()
				SensorToggle(
					icon = Icons.Outlined.WifiOff,
					title = "Network",
					subtitle = "Block outbound traffic",
					enabled = state.networkBlocked,
					onToggle = { viewModel.toggleNetwork() }
				)
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Warning card
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(16.dp))
					.background(Color(0xFFEF4444).copy(alpha = 0.08f))
					.padding(16.dp)
			) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.Top
				) {
					Box(
						modifier = Modifier
							.size(24.dp)
							.clip(CircleShape)
							.background(Color(0xFFEF4444).copy(alpha = 0.2f)),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "⚠",
							fontSize = 12.sp
						)
					}
					Column {
						Text(
							text = "Important",
							style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
							color = Color(0xFFEF4444)
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = "Lockdown mode will disable active communications. Emergency calls may be affected. Use with caution.",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
						)
					}
				}
			}
			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

@Composable
private fun PanicButton(
	activated: Boolean,
	onToggle: () -> Unit
) {
	val bgColor by animateColorAsState(
		targetValue = if (activated) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
		animationSpec = tween(400), label = "panic_bg"
	)
	val scale by animateFloatAsState(
		targetValue = if (activated) 1.05f else 1f,
		animationSpec = tween(300),
		label = "panic_scale"
	)

	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.size(180.dp)
				.scale(scale)
				.clip(CircleShape)
				.background(
					Brush.radialGradient(
						colors = listOf(
							bgColor,
							bgColor.copy(alpha = 0.7f)
						)
					)
				)
				.clickable { onToggle() },
			contentAlignment = Alignment.Center
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					imageVector = if (activated) Icons.Outlined.GppGood else Icons.Outlined.Lock,
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier.size(48.dp)
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = if (activated) "ACTIVE" else "LOCKDOWN",
					style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
					color = Color.White
				)
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		Text(
			text = if (activated) "Tap to deactivate lockdown" else "Tap to activate lockdown mode",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun StatusBanner(active: Boolean) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(
				if (active) Color(0xFF10B981).copy(alpha = 0.12f)
				else MaterialTheme.colorScheme.surfaceVariant
			)
			.padding(16.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Outlined.Security,
			contentDescription = null,
			tint = if (active) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(24.dp)
		)
		Column {
			Text(
				text = if (active) "All sensors disabled" else "Standard mode",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
				color = if (active) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
			)
			Text(
				text = if (active) "Lockdown is active — device is secured"
				else "Privacy controls are at normal levels",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun SensorToggle(
	icon: ImageVector,
	title: String,
	subtitle: String,
	enabled: Boolean,
	onToggle: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onToggle() }
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
					.background(
						if (enabled) Color(0xFFEF4444).copy(alpha = 0.12f)
						else MaterialTheme.colorScheme.surfaceVariant
					),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = if (enabled) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(20.dp)
				)
			}
			Column {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
				)
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		Switch(
			checked = enabled,
			onCheckedChange = { onToggle() },
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = Color(0xFFEF4444)
			)
		)
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
