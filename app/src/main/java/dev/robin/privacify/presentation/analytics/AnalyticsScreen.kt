package dev.robin.privacify.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalyticsScreen() {
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
			Header()
			MicrophoneTimelineCard(state)
			PermissionRequestsCard(state)
			HighRiskAppsCard(state)
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun Header() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Column {
			Text(
				text = "Privacy Analytics",
				style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = "Trends & Insights",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
			)
		}
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(999.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = "This Week",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
			)
		}
	}
}

@Composable
private fun MicrophoneTimelineCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Microphone Timeline",
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
			)
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(999.dp))
					.background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
					.padding(horizontal = 10.dp, vertical = 4.dp)
			) {
				Text(
					text = state.lastMicActiveLabel,
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
					color = Color(0xFF8B5CF6)
				)
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column {
				Text(
					text = "${state.micUsageMinutesToday}m",
					style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
				)
				Text(
					text = "Total usage today",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			Column(horizontalAlignment = Alignment.End) {
				Text(
					text = buildString {
						if (state.micUsageChangePercent >= 0) append("+")
						append(state.micUsageChangePercent)
						append("%")
					},
					style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
					color = if (state.micUsageChangePercent < 0) Color(0xFF10B981) else Color(0xFFEF4444)
				)
				Text(
					text = "vs yesterday",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		Spacer(modifier = Modifier.height(16.dp))
		// Bar chart
		val barColor = Color(0xFF8B5CF6)
		val barData = state.barChartData
		Canvas(
			modifier = Modifier
				.fillMaxWidth()
				.height(100.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
				.padding(8.dp)
		) {
			val barWidth = (size.width - (barData.size - 1) * 4.dp.toPx()) / barData.size
			barData.forEachIndexed { index, value ->
				val barHeight = size.height * value
				val x = index * (barWidth + 4.dp.toPx())
				drawRoundRect(
					color = barColor.copy(alpha = 0.3f + value * 0.7f),
					topLeft = Offset(x, size.height - barHeight),
					size = Size(barWidth, barHeight),
					cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
				)
			}
		}
		Spacer(modifier = Modifier.height(8.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			listOf("12 AM", "6 AM", "12 PM", "6 PM", "Now").forEach { label ->
				Text(
					text = label,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun PermissionRequestsCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Permission Requests",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// Donut chart
			Box(
				modifier = Modifier.size(120.dp),
				contentAlignment = Alignment.Center
			) {
				val locationColor = Color(0xFF3B82F6)
				val cameraColor = Color(0xFF10B981)
				val micColor = Color(0xFFEF4444)
				val contactsColor = Color(0xFFF97316)
				val emptyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
				val total = (state.locationSharePercent + state.cameraSharePercent + state.micSharePercent + state.contactsSharePercent).toFloat()

				Canvas(modifier = Modifier.size(120.dp)) {
					val strokeWidth = 16.dp.toPx()
					if (total > 0) {
						var startAngle = -90f
						val sweep1 = 360f * state.locationSharePercent / total
						drawArc(locationColor, startAngle, sweep1, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
						startAngle += sweep1
						val sweep2 = 360f * state.cameraSharePercent / total
						drawArc(cameraColor, startAngle, sweep2, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
						startAngle += sweep2
						val sweep3 = 360f * state.micSharePercent / total
						drawArc(micColor, startAngle, sweep3, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
						startAngle += sweep3
						val sweep4 = 360f * state.contactsSharePercent / total
						drawArc(contactsColor, startAngle, sweep4, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
					} else {
						drawArc(
							emptyColor,
							0f, 360f, false,
							style = Stroke(strokeWidth, cap = StrokeCap.Butt)
						)
					}
				}
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						text = state.totalPermissionRequests.toString(),
						style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
					)
					Text(
						text = "Requests",
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				LegendRow(color = Color(0xFF3B82F6), label = "Location", value = "${state.locationSharePercent}%")
				LegendRow(color = Color(0xFF10B981), label = "Camera", value = "${state.cameraSharePercent}%")
				LegendRow(color = Color(0xFFEF4444), label = "Mic", value = "${state.micSharePercent}%")
				LegendRow(color = Color(0xFFF97316), label = "Contacts", value = "${state.contactsSharePercent}%")
			}
		}
	}
}

@Composable
private fun LegendRow(
	color: Color,
	label: String,
	value: String
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(10.dp)
				.clip(RoundedCornerShape(999.dp))
				.background(color)
		)
		Column {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium
			)
			Text(
				text = value,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun HighRiskAppsCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
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
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
			)
			Text(
				text = "View All",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
				color = MaterialTheme.colorScheme.primary
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

@Composable
private fun HighRiskRow(
	name: String,
	description: String
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(14.dp))
			.background(Color(0xFFEF4444).copy(alpha = 0.06f))
			.padding(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(Color(0xFFEF4444).copy(alpha = 0.15f)),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = name.firstOrNull()?.uppercase() ?: "",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
				color = Color(0xFFEF4444)
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
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
				)
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(999.dp))
						.background(Color(0xFFEF4444).copy(alpha = 0.12f))
						.padding(horizontal = 6.dp, vertical = 2.dp)
				) {
					Text(
						text = "HIGH",
						style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
						color = Color(0xFFEF4444)
					)
				}
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
