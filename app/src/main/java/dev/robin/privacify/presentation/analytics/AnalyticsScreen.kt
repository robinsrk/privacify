package dev.robin.privacify.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.core.provider.ProFeature
import dev.robin.privacify.data.sensorlog.SensorEvent
import dev.robin.privacify.data.sensorlog.SensorLogRepository
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
	onNavigateToHistory: () -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.factory(context))
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
			Header(totalApps = state.totalApps, totalGrants = state.totalPermissionGrants)
			PermissionDistributionCard(state)
			RiskBreakdownCard(state)
			HighRiskAppsCard(state)
			if (ProFeature.isAutoGuardAvailable()) {
				SensorHistoryCard(onClick = onNavigateToHistory)
			}
		}
	}
}

@Composable
private fun Header(
	totalApps: Int,
	totalGrants: Int
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Column {
			Text(
				text = "Privacy Analytics",
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = "$totalGrants grants across $totalApps apps",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
			)
		}
		PrivacifyBadge(
			text = "$totalApps apps",
			color = MaterialTheme.colorScheme.primary
		)
	}
}

@Composable
private fun PermissionDistributionCard(
	state: AnalyticsUiState
) {
	PrivacifyExpressiveCard {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Permission Distribution",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Black
			)
			Spacer(modifier = Modifier.height(4.dp))
			val maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1)
			PermissionBar(label = "Location", count = state.locationAppCount, color = BlueVibrant, maxCount = maxCount)
			PermissionBar(label = "Camera", count = state.cameraAppCount, color = GreenVibrant, maxCount = maxCount)
			PermissionBar(label = "Microphone", count = state.micAppCount, color = RedVibrant, maxCount = maxCount)
			PermissionBar(label = "Contacts", count = state.contactsAppCount, color = OrangeVibrant, maxCount = maxCount)
			PermissionBar(label = "SMS/Phone", count = state.smsAppCount, color = PurpleVibrant, maxCount = maxCount)
		}
	}
}

@Composable
private fun PermissionBar(
	label: String,
	count: Int,
	color: Color,
	maxCount: Int
) {
	Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				text = "$count app${if (count != 1) "s" else ""}",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Black,
				color = color
			)
		}
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(10.dp)
				.clip(MaterialTheme.shapes.small)
				.background(MaterialTheme.colorScheme.surfaceBright)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(count.toFloat() / maxCount)
					.height(10.dp)
					.clip(MaterialTheme.shapes.small)
					.background(
						Brush.horizontalGradient(
							listOf(color, color.copy(alpha = 0.7f))
						)
					)
			)
		}
	}
}

@Composable
private fun RiskBreakdownCard(
	state: AnalyticsUiState
) {
	PrivacifyExpressiveCard {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Risk Breakdown",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Black
			)
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				RiskBadge(label = "High", count = state.highRiskCount, color = RedVibrant)
				RiskBadge(label = "Medium", count = state.mediumRiskCount, color = OrangeVibrant)
				RiskBadge(label = "Low", count = state.lowRiskCount, color = GreenVibrant)
			}
			Text(
				text = "${state.totalPermissionGrants} total permission grants across ${state.totalApps} apps",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun RiskBadge(
	label: String,
	count: Int,
	color: Color
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = count.toString(),
			style = MaterialTheme.typography.displaySmall,
			fontWeight = FontWeight.Black,
			color = color
		)
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun HighRiskAppsCard(
	state: AnalyticsUiState
) {
	PrivacifyExpressiveCard {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "High Risk Apps",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black
				)
			}
			if (state.highRiskApps.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 16.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "No high risk apps detected",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			state.highRiskApps.forEach { app ->
				HighRiskRow(
					name = app.appName,
					description = app.permissionsSummary
				)
			}
		}
	}
}

@Composable
private fun HighRiskRow(
	name: String,
	description: String
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.large)
			.background(RedVibrant.copy(alpha = 0.06f))
			.padding(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(CircleShape)
				.background(RedVibrant.copy(alpha = 0.15f)),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = name.firstOrNull()?.uppercase() ?: "",
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Black,
				color = RedVibrant
			)
		}
		Column(modifier = Modifier.weight(1f)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)
				PrivacifyBadge(text = "HIGH", color = RedVibrant)
			}
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun SensorHistoryCard(
	onClick: () -> Unit
) {
	val context = LocalContext.current
	val repository = remember { SensorLogRepository(context.applicationContext) }
	val events = remember { repository.getEvents() }

	PrivacifyExpressiveCard(
		modifier = Modifier.clickable { onClick() }
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = Icons.Outlined.History,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Sensor Usage History",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black,
					modifier = Modifier.weight(1f)
				)
				Icon(
					imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
					contentDescription = "View all",
					tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
					modifier = Modifier.size(20.dp)
				)
			}

			if (events.isEmpty()) {
				Text(
					text = "No sensor usage recorded yet.\nEnable Auto-Guard to start tracking.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Start,
					modifier = Modifier.padding(top = 4.dp)
				)
			} else {
				val sessions = pairMiniSessions(events).take(3)
				sessions.forEach { session ->
					MiniTimelineRow(session = session)
				}
				if (events.size / 2 > 3) {
					Text(
						text = "+ ${events.size / 2 - 3} more — tap to view all",
						style = MaterialTheme.typography.labelSmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.padding(top = 4.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun MiniTimelineRow(session: MiniSession) {
	val context = LocalContext.current
	val sensorIcon = when (session.type) {
		SensorEvent.TYPE_MIC -> Icons.Outlined.MicOff
		SensorEvent.TYPE_CAMERA -> Icons.Outlined.CameraAlt
		SensorEvent.TYPE_LOCATION -> Icons.Outlined.LocationOn
		else -> Icons.Outlined.History
	}
	val sensorColor = when (session.type) {
		SensorEvent.TYPE_MIC -> RedVibrant
		SensorEvent.TYPE_CAMERA -> OrangeVibrant
		SensorEvent.TYPE_LOCATION -> AmberVibrant
		else -> MaterialTheme.colorScheme.primary
	}
	val label = when (session.type) {
		SensorEvent.TYPE_MIC -> "Mic"
		SensorEvent.TYPE_CAMERA -> "Camera"
		SensorEvent.TYPE_LOCATION -> "Location"
		else -> session.type
	}
	val durationText = if (session.stopTime != null) {
		val sec = (session.stopTime - session.startTime) / 1000
		if (sec < 60) "${sec}s" else "${sec / 60}m ${sec % 60}s"
	} else "Running"

	val pkg = session.appPackage
	val appInfo = remember(pkg) {
		if (pkg.isNullOrBlank()) null
		else try {
			val pm = context.packageManager
			val ai = pm.getApplicationInfo(pkg, 0)
			Pair(pm.getApplicationLabel(ai).toString(), pm.getApplicationIcon(ai))
		} catch (_: Exception) { Pair(pkg, null) }
	}
	val iconPainter = remember(appInfo) {
		appInfo?.second?.let { drawable ->
			val w = drawable.intrinsicWidth.coerceAtLeast(1)
			val h = drawable.intrinsicHeight.coerceAtLeast(1)
			val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
			val c = Canvas(bmp)
			drawable.setBounds(0, 0, w, h)
			drawable.draw(c)
			BitmapPainter(bmp.asImageBitmap())
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.background(MaterialTheme.colorScheme.surfaceBright)
			.padding(10.dp),
		verticalAlignment = Alignment.Top
	) {
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(sensorColor.copy(alpha = 0.12f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = sensorIcon,
				contentDescription = null,
				tint = sensorColor,
				modifier = Modifier.size(18.dp)
			)
		}
		Spacer(modifier = Modifier.width(10.dp))
		Column(modifier = Modifier.weight(1f)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (iconPainter != null) {
					Icon(
						painter = iconPainter,
						contentDescription = null,
						tint = Color.Unspecified,
						modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp))
					)
					Spacer(modifier = Modifier.width(4.dp))
				}
				Text(
					text = appInfo?.first ?: label,
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.width(6.dp))
				Text(
					text = "· $durationText",
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			Text(
				text = "$label: ${miniTimeFormat.format(Date(session.startTime))} - ${if (session.stopTime != null) miniTimeFormat.format(Date(session.stopTime)) else "now"}",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

private data class MiniSession(
	val type: String,
	val appPackage: String?,
	val startTime: Long,
	val stopTime: Long?
)

private fun pairMiniSessions(events: List<SensorEvent>): List<MiniSession> {
	val chrono = events.reversed()
	val sessions = mutableListOf<MiniSession>()
	val pending = mutableMapOf<String, Long>()
	for (event in chrono) {
		val key = "${event.type}|${event.appPackage ?: "unknown"}"
		if (event.isStart) {
			pending[key] = event.timestamp
		} else {
			val start = pending.remove(key) ?: event.timestamp
			sessions.add(MiniSession(event.type, event.appPackage, start, event.timestamp))
		}
	}
	for ((key, start) in pending) {
		val parts = key.split("|")
		sessions.add(MiniSession(parts[0], parts.getOrNull(1).takeIf { it != "unknown" }, start, null))
	}
	return sessions.sortedByDescending { it.startTime }
}

private val miniTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
