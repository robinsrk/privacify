package dev.robin.privacify.presentation.apps

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant

@Composable
fun AppsScreen(
	onAppSelected: (AppPrivacyInfo) -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel: AppsViewModel = viewModel(factory = AppsViewModel.factory(context))
	val state by viewModel.state.collectAsState()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(top = 16.dp)
		) {
			Column(
				modifier = Modifier.padding(horizontal = 16.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "Permission Scanner",
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.Black
					)
					PrivacifyBadge(
						text = "${state.apps.size} apps",
						color = MaterialTheme.colorScheme.primary
					)
				}
				Spacer(modifier = Modifier.height(12.dp))
				OutlinedTextField(
					value = state.query,
					onValueChange = { viewModel.onQueryChanged(it) },
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(16.dp),
					singleLine = true,
					placeholder = {
						Text(text = "Search apps or permissions")
					},
					leadingIcon = {
						Icon(
							imageVector = Icons.Filled.Search,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					},
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline,
						focusedContainerColor = MaterialTheme.colorScheme.surface,
						unfocusedContainerColor = MaterialTheme.colorScheme.surface
					)
				)
				Spacer(modifier = Modifier.height(12.dp))
			}
			LazyRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				item {
					FilterChip(
						label = "All",
						selected = state.filter == RiskFilter.All,
						onClick = { viewModel.onFilterChanged(RiskFilter.All) }
					)
				}
				item {
					FilterChip(
						label = "High Risk",
						selected = state.filter == RiskFilter.High,
						onClick = { viewModel.onFilterChanged(RiskFilter.High) }
					)
				}
				item {
					FilterChip(
						label = "Medium Risk",
						selected = state.filter == RiskFilter.Medium,
						onClick = { viewModel.onFilterChanged(RiskFilter.Medium) }
					)
				}
				item {
					FilterChip(
						label = "Safe",
						selected = state.filter == RiskFilter.Low,
						onClick = { viewModel.onFilterChanged(RiskFilter.Low) }
					)
				}
			}
			Spacer(modifier = Modifier.height(8.dp))
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp)
			) {
				items(
					items = state.filteredApps,
					key = { it.packageName }
				) { app ->
					AppRow(
						app = app,
						onClick = { onAppSelected(app) }
					)
				}
				item {
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}
	}
}

@Composable
private fun FilterChip(
	label: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	val bgColor by animateColorAsState(
		targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
		label = "chip_bg"
	)
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(14.dp))
			.background(bgColor)
			.clickable { onClick() }
			.padding(horizontal = 16.dp, vertical = 10.dp)
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
			color = if (selected) MaterialTheme.colorScheme.onPrimary
			else MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
private fun AppRow(
	app: AppPrivacyInfo,
	onClick: () -> Unit
) {
	val riskColor = when (app.riskLevel) {
		AppRiskLevel.High -> RedVibrant
		AppRiskLevel.Medium -> OrangeVibrant
		AppRiskLevel.Low -> GreenVibrant
	}

	PrivacifyExpressiveCard(onClick = onClick) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(16.dp))
					.background(riskColor.copy(alpha = 0.15f)),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = app.appName.firstOrNull()?.uppercase() ?: "",
					color = riskColor,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black
				)
			}
			Column(modifier = Modifier.weight(1f)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = app.appName,
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.weight(1f, fill = false)
					)
					Spacer(modifier = Modifier.width(8.dp))
					RiskBadge(app)
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = app.permissionsSummary,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

@Composable
private fun RiskBadge(app: AppPrivacyInfo) {
	val (label, bg, fg) = when (app.riskLevel) {
		AppRiskLevel.High -> Triple(
			"High Risk",
			RedVibrant.copy(alpha = 0.15f),
			RedVibrant
		)

		AppRiskLevel.Medium -> Triple(
			"Medium",
			OrangeVibrant.copy(alpha = 0.15f),
			OrangeVibrant
		)

		AppRiskLevel.Low -> Triple(
			"Safe",
			GreenVibrant.copy(alpha = 0.15f),
			GreenVibrant
		)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.clip(RoundedCornerShape(999.dp))
			.background(bg)
			.padding(horizontal = 8.dp, vertical = 4.dp)
	) {
		Box(
			modifier = Modifier
				.size(6.dp)
				.clip(CircleShape)
				.background(fg)
		)
		Spacer(modifier = Modifier.width(4.dp))
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Black,
			color = fg
		)
	}
}
