package dev.robin.privacify.presentation.analytics

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
import androidx.compose.ui.graphics.Color
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
			Header(totalApps = state.totalApps)
			PermissionDistributionCard(state)
			RiskBreakdownCard(state)
			HighRiskAppsCard(state)
		}
	}
}

@Composable
private fun Header(
	totalApps: Int
) {
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
				text = "Permission overview & risk summary",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
			)
		}
		Box(
			modifier = Modifier
				.clip(MaterialTheme.shapes.small)
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = "$totalApps apps",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
			)
		}
	}
}

@Composable
private fun PermissionDistributionCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.large)
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Permission Distribution",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
		)
		Spacer(modifier = Modifier.height(4.dp))
		PermissionBar(label = "Location", count = state.locationAppCount, color = Color(0xFF3B82F6), maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1))
		PermissionBar(label = "Camera", count = state.cameraAppCount, color = Color(0xFF10B981), maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1))
		PermissionBar(label = "Microphone", count = state.micAppCount, color = Color(0xFFEF4444), maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1))
		PermissionBar(label = "Contacts", count = state.contactsAppCount, color = Color(0xFFF97316), maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1))
		PermissionBar(label = "SMS/Phone", count = state.smsAppCount, color = Color(0xFFA855F7), maxCount = maxOf(state.locationAppCount, state.cameraAppCount, state.micAppCount, state.contactsAppCount, state.smsAppCount, 1))
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
				style = MaterialTheme.typography.bodyMedium
			)
			Text(
				text = "$count app${if (count != 1) "s" else ""}",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold,
				color = color
			)
		}
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(8.dp)
				.clip(RoundedCornerShape(4.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(count.toFloat() / maxCount)
					.height(8.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(color)
			)
		}
	}
}

@Composable
private fun RiskBreakdownCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.large)
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Text(
			text = "Risk Breakdown",
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			RiskBadge(label = "High", count = state.highRiskCount, color = Color(0xFFEF4444))
			RiskBadge(label = "Medium", count = state.mediumRiskCount, color = Color(0xFFF97316))
			RiskBadge(label = "Low", count = state.lowRiskCount, color = Color(0xFF10B981))
		}
		Text(
			text = "${state.totalPermissionGrants} total permission grants across ${state.totalApps} apps",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
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
			style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
			color = color
		)
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun HighRiskAppsCard(
	state: AnalyticsUiState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.large)
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
			.clip(MaterialTheme.shapes.medium)
			.background(Color(0xFFEF4444).copy(alpha = 0.06f))
			.padding(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(MaterialTheme.shapes.small)
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
						.clip(MaterialTheme.shapes.small)
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
