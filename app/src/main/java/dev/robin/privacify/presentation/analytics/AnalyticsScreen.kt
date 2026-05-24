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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.ExpressiveLargeIncreased
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant

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
			Header(totalApps = state.totalApps, totalGrants = state.totalPermissionGrants)
			PermissionDistributionCard(state)
			RiskBreakdownCard(state)
			HighRiskAppsCard(state)
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
				.clip(MaterialTheme.shapes.extraSmall)
				.background(MaterialTheme.colorScheme.surfaceVariant)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(count.toFloat() / maxCount)
					.height(10.dp)
					.clip(MaterialTheme.shapes.extraSmall)
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
				.clip(ExpressiveLargeIncreased)
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
