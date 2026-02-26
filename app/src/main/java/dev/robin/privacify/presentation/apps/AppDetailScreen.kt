package dev.robin.privacify.presentation.apps

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.apps.AppRiskLevel


data class PermissionDetail(
	val name: String,
	val description: String,
	val isGranted: Boolean
)

data class AppDetailInfo(
	val packageName: String,
	val appName: String,
	val riskLevel: AppRiskLevel,
	val permissions: List<PermissionDetail>,
	val grantedCount: Int,
	val totalCount: Int
)

@Composable
fun AppDetailRoute(
	packageName: String
) {
	val context = LocalContext.current
	val viewModel: AppDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
		factory = AppDetailViewModel.factory(packageName, context)
	)
	val app by viewModel.state.collectAsState()
	val isRooted by viewModel.isRooted.collectAsState()
	val actionResult by viewModel.actionResult.collectAsState()

	LaunchedEffect(actionResult) {
		actionResult?.let { msg ->
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
			viewModel.clearActionResult()
		}
	}

	val current = app
	if (current != null) {
		AppDetailScreen(
			app = current,
			isRooted = isRooted,
			onAction = { action ->
				when (action) {
					"revoke" -> viewModel.forceRevokePermissions()
					"freeze" -> viewModel.freezeApp()
					"sensors" -> viewModel.blockSensorAccess()
				}
			}
		)
	}
}

private fun calculateRiskFromDetails(
	perms: List<PermissionDetail>,
	requested: List<String>
): AppRiskLevel {
	val hasInternet = requested.any { it == android.Manifest.permission.INTERNET }
	val grantedNames = perms.filter { it.isGranted }.map { it.name.lowercase() }

	val hasMic = grantedNames.any { it.contains("record") || it.contains("audio") }
	val hasCamera = grantedNames.any { it.contains("camera") }
	val hasLocation = grantedNames.any { it.contains("location") }
	val hasContacts = grantedNames.any { it.contains("contacts") }
	val hasSms = grantedNames.any { it.contains("sms") }

	val sensitiveGranted = listOf(hasMic, hasCamera, hasLocation, hasContacts, hasSms).count { it }

	if (sensitiveGranted == 0) return AppRiskLevel.Low
	if (sensitiveGranted >= 3 && hasInternet) return AppRiskLevel.High
	if ((hasMic || hasCamera) && hasInternet) return AppRiskLevel.High
	return AppRiskLevel.Medium
}

@Composable
private fun AppDetailScreen(
	app: AppDetailInfo,
	isRooted: Boolean = false,
	onAction: (String) -> Unit
) {
	val riskColor = when (app.riskLevel) {
		AppRiskLevel.High -> Color(0xFFEF4444)
		AppRiskLevel.Medium -> Color(0xFFF97316)
		AppRiskLevel.Low -> Color(0xFF10B981)
	}

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
			Header(app, riskColor)
			Spacer(modifier = Modifier.height(16.dp))
			ActivityInsights(app, riskColor)
			Spacer(modifier = Modifier.height(16.dp))
			PermissionsSection(app)
			Spacer(modifier = Modifier.height(16.dp))
			AdvancedControls(isRooted = isRooted, onAction = onAction)
			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

@Composable
private fun Header(
	app: AppDetailInfo,
	riskColor: Color
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.padding(top = 8.dp, bottom = 12.dp)
				.clip(RoundedCornerShape(28.dp))
				.background(riskColor.copy(alpha = 0.15f))
				.padding(24.dp)
		) {
			Text(
				text = app.appName.firstOrNull()?.uppercase() ?: "",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = riskColor
			)
		}
		Text(
			text = app.appName,
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = app.packageName,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
		)
		Spacer(modifier = Modifier.height(8.dp))
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(999.dp))
				.background(riskColor.copy(alpha = 0.12f))
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = when (app.riskLevel) {
					AppRiskLevel.High -> "High Risk App"
					AppRiskLevel.Medium -> "Medium Risk App"
					AppRiskLevel.Low -> "Low Risk App"
				},
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = riskColor
			)
		}
	}
}

@Composable
private fun ActivityInsights(
	app: AppDetailInfo,
	riskColor: Color
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = "ACTIVITY INSIGHTS",
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			InsightCard(
				title = "${app.grantedCount}/${app.totalCount}",
				subtitle = "Permissions Granted",
				modifier = Modifier.weight(1f)
			)
			InsightCard(
				title = when (app.riskLevel) {
					AppRiskLevel.High -> "Elevated"
					AppRiskLevel.Medium -> "Moderate"
					AppRiskLevel.Low -> "Low"
				},
				subtitle = "Risk Level",
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun InsightCard(
	title: String,
	subtitle: String,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = subtitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun PermissionsSection(
	app: AppDetailInfo
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = "PERMISSIONS (${app.grantedCount} granted of ${app.totalCount})",
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
			modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(20.dp))
				.background(MaterialTheme.colorScheme.surface)
		) {
			if (app.permissions.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "No sensitive permissions detected",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			app.permissions.forEachIndexed { index, perm ->
				PermissionRow(
					title = perm.name,
					description = perm.description,
					isGranted = perm.isGranted
				)
				if (index < app.permissions.lastIndex) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp)
							.height(1.dp)
							.background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
					)
				}
			}
		}
	}
}

@Composable
private fun PermissionRow(
	title: String,
	description: String,
	isGranted: Boolean
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Box(
			modifier = Modifier
				.clip(CircleShape)
				.background(
					if (isGranted) Color(0xFFEF4444).copy(alpha = 0.12f)
					else Color(0xFF10B981).copy(alpha = 0.12f)
				)
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = if (isGranted) "Granted" else "Denied",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
				color = if (isGranted) Color(0xFFEF4444) else Color(0xFF10B981)
			)
		}
	}
}

@Composable
private fun AdvancedControls(
	isRooted: Boolean = false,
	onAction: (String) -> Unit
) {
	var showConfirmDialog by remember { mutableStateOf(false) }
	var pendingAction by remember { mutableStateOf("") }
	var pendingActionId by remember { mutableStateOf("") }

	if (showConfirmDialog) {
		AlertDialog(
			onDismissRequest = { showConfirmDialog = false },
			title = { Text("Confirm Root Action") },
			text = { Text("This action requires root privileges and may affect system stability. Are you sure you want to $pendingAction?") },
			confirmButton = {
				TextButton(
					onClick = { 
						showConfirmDialog = false 
						onAction(pendingActionId)
					}
				) {
					Text("Execute", color = Color(0xFFEF4444))
				}
			},
			dismissButton = {
				TextButton(onClick = { showConfirmDialog = false }) {
					Text("Cancel")
				}
			}
		)
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "ADVANCED CONTROLS",
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = Color(0xFFEF4444)
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Box(
					modifier = Modifier
						.size(8.dp)
						.clip(CircleShape)
						.background(Color(0xFFEF4444))
				)
				Text(
					text = "ROOT ONLY",
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
					color = Color(0xFFEF4444)
				)
			}
		}
		Spacer(modifier = Modifier.height(8.dp))
		val alpha = if (isRooted) 1f else 0.45f
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.then(if (!isRooted) Modifier.padding(0.dp) else Modifier)
		) {
			AdvancedActionCard(
				icon = Icons.Outlined.Block,
				title = "Force Revoke Permissions",
				description = if (isRooted) "Bypass system restrictions" else "Requires root access",
				enabled = isRooted,
				onClick = {
					pendingAction = "force revoke permissions"
					pendingActionId = "revoke"
					showConfirmDialog = true
				}
			)
			AdvancedActionCard(
				icon = Icons.Outlined.Dangerous,
				title = "Freeze App State",
				description = if (isRooted) "Suspend execution entirely" else "Requires root access",
				enabled = isRooted,
				onClick = {
					pendingAction = "freeze this app"
					pendingActionId = "freeze"
					showConfirmDialog = true
				}
			)
			AdvancedActionCard(
				icon = Icons.Outlined.Sensors,
				title = "Block Sensor Access",
				description = if (isRooted) "Disable Gyro, GPS & Accel" else "Requires root access",
				enabled = isRooted,
				onClick = {
					pendingAction = "block sensor access"
					pendingActionId = "sensors"
					showConfirmDialog = true
				}
			)
		}
	}
}

@Composable
private fun AdvancedActionCard(
	icon: ImageVector,
	title: String,
	description: String,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	val alpha = if (enabled) 1f else 0.45f
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(Color(0xFFEF4444).copy(alpha = 0.06f * alpha))
			.clickable(enabled = enabled) { onClick() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(Color(0xFFEF4444).copy(alpha = 0.12f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = Color(0xFFEF4444),
				modifier = Modifier.size(20.dp)
			)
		}
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
			)
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(999.dp))
				.background(Color(0xFFEF4444).copy(alpha = 0.12f))
				.padding(horizontal = 12.dp, vertical = 6.dp)
		) {
			Text(
				text = "Action",
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
				color = Color(0xFFEF4444)
			)
		}
	}
}
