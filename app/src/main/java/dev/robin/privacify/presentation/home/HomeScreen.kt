package dev.robin.privacify.presentation.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyAutoGuardCard
import dev.robin.privacify.ui.components.PrivacifyStatusIndicator
import dev.robin.privacify.ui.components.SensorCard
import dev.robin.privacify.ui.theme.MdSpacing
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.GradientEnd
import dev.robin.privacify.ui.theme.GradientMid
import dev.robin.privacify.ui.theme.GradientStart
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.LockdownRed
import dev.robin.privacify.ui.theme.OrangeVibrant
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

			Spacer(modifier = Modifier.height(24.dp))

			SensorControlsSection(
				state = state,
				onActionToggle = { action -> viewModel.onQuickActionToggled(action) }
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
		animationSpec = spring(
			stiffness = Spring.StiffnessLow,
			dampingRatio = Spring.DampingRatioMediumBouncy
		),
		label = "scoreArc"
	)
	LaunchedEffect(Unit) { animationPlayed = true }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 16.dp)
			.semantics {
				contentDescription = "Privacy Score: $score out of 100, status: $statusText"
			},
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Surface(
			modifier = Modifier.size(200.dp),
			shape = CircleShape,
			color = MaterialTheme.colorScheme.surfaceBright,
			tonalElevation = 2.dp
		) {
			Box(contentAlignment = Alignment.Center) {
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
							.background(Brush.linearGradient(gradientColors))
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
		}
		Spacer(modifier = Modifier.height(16.dp))
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
private fun SensorControlsSection(
	state: DashboardUiState,
	onActionToggle: (QuickAction) -> Unit
) {
	val hasAccess = state.isRooted

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "CONTROLS",
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
				modifier = Modifier.semantics { heading() }
			)
			Row(verticalAlignment = Alignment.CenterVertically) {
				PrivacifyStatusIndicator(
					status = if (state.isRooted) "Root Ready" else "No Root",
					color = if (state.isRooted) GreenVibrant else RedVibrant
				)
			}
		}

		LockdownCard(
			hasAccess = hasAccess,
			isActive = state.lockdownEnabled,
			shellType = state.shellType,
			onToggle = { onActionToggle(QuickAction.Lockdown) }
		)

		Text(
			text = "SENSOR CONTROLS",
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
			modifier = Modifier
				.padding(start = 4.dp, end = 4.dp, top = MdSpacing.xs)
				.semantics { heading() }
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)
		) {
			SensorCard(
				icon = Icons.Outlined.MicOff,
				title = "Mic",
				active = state.micDisabled,
				activeColor = RedVibrant,
				onClick = { if (hasAccess) onActionToggle(QuickAction.MicKill) }
			)
			SensorCard(
				icon = Icons.Outlined.CameraAlt,
				title = "Camera",
				active = state.cameraDisabled,
				activeColor = OrangeVibrant,
				onClick = { if (hasAccess) onActionToggle(QuickAction.CameraKill) }
			)
			SensorCard(
				icon = Icons.Outlined.LocationOn,
				title = "Location",
				active = state.locationDisabled,
				activeColor = AmberVibrant,
				onClick = { if (hasAccess) onActionToggle(QuickAction.LocationKill) }
			)
		}
	}
}

@Composable
private fun LockdownCard(
	hasAccess: Boolean,
	isActive: Boolean,
	shellType: String,
	onToggle: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.semantics {
				contentDescription = "Lockdown Mode, ${if (isActive) "Active" else "Inactive"}"
				stateDescription = if (isActive) "Active" else "Inactive"
			}
			.clickable(enabled = hasAccess) { onToggle() },
		shape = MaterialTheme.shapes.large,
		colors = CardDefaults.cardColors(
			containerColor = when {
				isActive -> LockdownRed
				hasAccess -> MaterialTheme.colorScheme.surfaceBright
				else -> MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f)
			}
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(MdSpacing.sm),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(MaterialTheme.shapes.medium)
					.background(
						if (isActive) Color.White.copy(alpha = 0.2f)
						else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
					),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = if (isActive) Icons.Filled.Security else Icons.Outlined.Lock,
					contentDescription = null,
					tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = "Lockdown Mode",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black,
					color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = when {
						isActive -> "All sensors blocked · DND enabled"
						hasAccess -> "Instantly block all sensors"
						else -> "Requires ${if (shellType == "shizuku") "Shizuku" else "Root"} access"
					},
					style = MaterialTheme.typography.bodyMedium,
					color = if (isActive) Color.White.copy(alpha = 0.8f)
					else MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun ScanNowButton(
	modifier: Modifier,
	onClick: () -> Unit
) {
	Button(
		onClick = onClick,
		shape = MaterialTheme.shapes.large,
		modifier = modifier
			.height(48.dp)
			.semantics {
				contentDescription = "Scan Now"
			},
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
