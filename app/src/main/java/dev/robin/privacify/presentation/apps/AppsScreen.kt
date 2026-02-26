package dev.robin.privacify.presentation.apps

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
				Text(
					text = "Permission Risk Scanner",
					style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = "Scanning ${state.apps.size} installed applications",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
				)
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
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(12.dp))
			.background(
				if (selected) MaterialTheme.colorScheme.primary
				else MaterialTheme.colorScheme.surface
			)
			.clickable { onClick() }
			.padding(horizontal = 14.dp, vertical = 8.dp)
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
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
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.surface)
			.clickable { onClick() }
			.padding(12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Box(
			modifier = Modifier
				.size(48.dp)
				.clip(RoundedCornerShape(14.dp))
				.background(
					when (app.riskLevel) {
						AppRiskLevel.High -> Color(0xFFEF4444).copy(alpha = 0.15f)
						AppRiskLevel.Medium -> Color(0xFFF97316).copy(alpha = 0.15f)
						AppRiskLevel.Low -> Color(0xFF10B981).copy(alpha = 0.15f)
					}
				),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = app.appName.firstOrNull()?.uppercase() ?: "",
				color = when (app.riskLevel) {
					AppRiskLevel.High -> Color(0xFFEF4444)
					AppRiskLevel.Medium -> Color(0xFFF97316)
					AppRiskLevel.Low -> Color(0xFF10B981)
				},
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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

@Composable
private fun RiskBadge(app: AppPrivacyInfo) {
	val (label, bg, fg) = when (app.riskLevel) {
		AppRiskLevel.High -> Triple(
			"High Risk",
			Color(0xFFEF4444).copy(alpha = 0.15f),
			Color(0xFFEF4444)
		)

		AppRiskLevel.Medium -> Triple(
			"Medium",
			Color(0xFFF97316).copy(alpha = 0.15f),
			Color(0xFFF97316)
		)

		AppRiskLevel.Low -> Triple(
			"Safe",
			Color(0xFF10B981).copy(alpha = 0.15f),
			Color(0xFF10B981)
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
			style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
			color = fg
		)
	}
}
