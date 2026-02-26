package dev.robin.privacify.presentation.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.VpnLock
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.domain.onboarding.OnboardingStep

@Composable
fun OnboardingRoute(
	onFinished: () -> Unit
) {
	val viewModel: OnboardingViewModel = viewModel(
		factory = OnboardingViewModel.Companion.factory(
			context = androidx.compose.ui.platform.LocalContext.current
		)
	)
	val state by viewModel.state.collectAsState()

	if (state.isCompleted) {
		onFinished()
		return
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		when (state.step) {
			OnboardingStep.Welcome -> OnboardingWelcomeScreen(
				onContinue = { viewModel.onWelcomeContinue() }
			)

			OnboardingStep.FeaturesOverview -> OnboardingFeaturesScreen(
				onContinue = { viewModel.onFeaturesContinue() }
			)

			OnboardingStep.SystemCheck -> OnboardingSystemCheckScreen(
				state = state,
				onCheckPermissions = {
					viewModel.checkPermissions(it)
				},
				onContinue = { viewModel.onSystemCheckContinue() }
			)

			OnboardingStep.RootDetection -> OnboardingRootDetectionScreen(
				onContinue = { viewModel.onRootDetectionContinue() }
			)
		}
	}
}

@Composable
private fun OnboardingWelcomeScreen(
	onContinue: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.verticalGradient(
					colors = listOf(
						Color(0xFF0F1216),
						Color(0xFF000000)
					)
				)
			)
			.padding(horizontal = 24.dp, vertical = 32.dp),
		verticalArrangement = Arrangement.SpaceBetween,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Spacer(modifier = Modifier.height(16.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth(),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.height(280.dp)
					.fillMaxWidth(0.8f)
					.clip(RoundedCornerShape(32.dp))
					.background(Color(0xFF15191E))
			)
		}

		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = "Take Back Control of\nYour Privacy",
				style = MaterialTheme.typography.headlineMedium.copy(
					fontWeight = FontWeight.Bold
				),
				color = Color.White,
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Monitor and manage your device permissions in one place.",
				style = MaterialTheme.typography.bodyMedium,
				color = Color(0xFF94A3B8),
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.height(24.dp))
			Row(
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.height(6.dp)
						.width(32.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.primary)
				)
				Spacer(modifier = Modifier.width(8.dp))
				repeat(3) {
					Box(
						modifier = Modifier
							.size(6.dp)
							.clip(CircleShape)
							.background(Color(0xFF1F2933))
					)
					if (it != 2) {
						Spacer(modifier = Modifier.width(4.dp))
					}
				}
			}
			Spacer(modifier = Modifier.height(24.dp))
			Button(
				onClick = onContinue,
				shape = CircleShape,
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = Color(0xFF4DABF7),
					contentColor = Color(0xFF001E38)
				)
			) {
				Text(text = "Get Started")
			}
		}
	}
}

@Composable
private fun OnboardingFeaturesScreen(
	onContinue: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 24.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.SpaceBetween
	) {
		Column {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Back button placeholder if needed, or just spacer
				Spacer(modifier = Modifier.size(40.dp))
				
				// Pagination
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.3f)))
					Box(modifier = Modifier.width(24.dp).height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
					Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.3f)))
					Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.3f)))
				}
				
				Text(
					text = "Skip",
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
					modifier = Modifier.clickable { onContinue() }
				)
			}
			
			Spacer(modifier = Modifier.height(24.dp))
			
			Text(
				text = "Features\nOverview",
				style = MaterialTheme.typography.headlineMedium.copy(
					fontWeight = FontWeight.ExtraBold
				),
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Protect your digital life with our comprehensive suite of privacy tools.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
			)
			Spacer(modifier = Modifier.height(24.dp))
			
			Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
				Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
					FeatureGridCard(
						title = "Monitor Usage",
						description = "Track mic & camera access in real-time.",
						icon = androidx.compose.material.icons.Icons.Rounded.VideocamOff,
						iconColor = Color(0xFFA855F7), // Purple
						modifier = Modifier.weight(1f)
					)
					FeatureGridCard(
						title = "Block Network",
						description = "Restrict internet access per app instantly.",
						icon = androidx.compose.material.icons.Icons.Rounded.WifiOff,
						iconColor = Color(0xFF3B82F6), // Blue
						modifier = Modifier.weight(1f)
					)
				}
				Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
					FeatureGridCard(
						title = "Lockdown",
						description = "One-tap system wide sensor deactivation.",
						icon = androidx.compose.material.icons.Icons.Rounded.Lock,
						iconColor = Color(0xFFEF4444), // Red
						modifier = Modifier.weight(1f)
					)
					FeatureGridCard(
						title = "Root Tools",
						description = "Deep system control for rooted devices.",
						icon = androidx.compose.material.icons.Icons.Rounded.Code, // Terminal might not exist
						iconColor = Color(0xFFF97316), // Orange
						isPro = true,
						modifier = Modifier.weight(1f)
					)
				}
			}
		}
		Column {
			Button(
				onClick = onContinue,
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				shape = RoundedCornerShape(16.dp)
			) {
				Text(text = "Continue")
			}
		}
	}
}

@Composable
private fun FeatureGridCard(
	title: String,
	description: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	iconColor: Color,
	isPro: Boolean = false,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(20.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		if (isPro) {
			Box(
				modifier = Modifier
					.align(Alignment.TopEnd)
					.background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
					.padding(horizontal = 4.dp, vertical = 2.dp)
			) {
				Text(
					text = "PRO",
					style = MaterialTheme.typography.labelSmall,
					color = iconColor,
					fontSize = 8.sp,
					fontWeight = FontWeight.Bold
				)
			}
		}
		
		Column {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(iconColor.copy(alpha = 0.1f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = iconColor,
					modifier = Modifier.size(24.dp)
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onSurface
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
				lineHeight = 14.sp
			)
		}
	}
}

@Composable
private fun OnboardingSystemCheckScreen(
	state: OnboardingUiState,
	onCheckPermissions: (Context) -> Unit,
	onContinue: () -> Unit
) {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				onCheckPermissions(context)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	val notificationLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) {
		onCheckPermissions(context)
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 24.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.SpaceBetween
	) {
		Column {
			Text(
				text = "Required Access",
				style = MaterialTheme.typography.headlineSmall.copy(
					fontWeight = FontWeight.Bold
				),
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "To protect your privacy, we need the following permissions to monitor traffic and block trackers.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
			)
			Spacer(modifier = Modifier.height(24.dp))
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				// Usage Access
				PermissionCard(
					title = "Usage Access",
					description = "Allows app to detect which apps are currently active to apply privacy rules in real-time.",
					icon = androidx.compose.material.icons.Icons.Rounded.DataUsage,
					isGranted = state.usageAccessGranted,
					onClick = {
						if (!state.usageAccessGranted) {
							val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
							context.startActivity(intent)
						}
					}
				)
				
				// Notifications (Android 13+)
				if (android.os.Build.VERSION.SDK_INT >= 33) {
					PermissionCard(
						title = "Notifications",
						description = "Get alerts when apps access your camera or microphone.",
						icon = androidx.compose.material.icons.Icons.Rounded.Notifications,
						isGranted = state.notificationPermissionGranted,
						onClick = {
							if (!state.notificationPermissionGranted) {
								notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
							}
						}
					)
				}

				// Local Firewall (Static/Informational)
				PermissionCard(
					title = "Local Firewall",
					description = "Creates a local VPN interface to filter traffic. No data ever leaves your device.",
					icon = androidx.compose.material.icons.Icons.Rounded.VpnLock,
					isGranted = true, // Always show as granted/active conceptually
					isEnabled = false // User cannot toggle this here
				)

				// Root Access (Conditional)
				if (state.isRootAvailable) {
					PermissionCard(
						title = "Root Access",
						description = "Optional. Enables deep system cleaning and kernel-level ad blocking.",
						icon = androidx.compose.material.icons.Icons.Rounded.AdminPanelSettings,
						isGranted = false, // TODO: Check actual root grant status if possible, or leave as unchecked
						isAdvanced = true,
						onClick = {
							// Trigger root request or show info
							// For now, just a placeholder action or maybe toast "Grant in Magisk"
							android.widget.Toast.makeText(context, "Please grant Root access in Magisk", android.widget.Toast.LENGTH_SHORT).show()
						}
					)
				}
			}
		}
		Column {
			Button(
				onClick = onContinue,
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp),
				shape = RoundedCornerShape(16.dp),
				// Disable if mandatory permissions (Usage Access) are not granted
				enabled = state.usageAccessGranted
			) {
				Text(text = "Grant & Continue")
			}
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Ask me later",
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 4.dp)
					.clickable { /* Skip logic if allowed, or just do nothing for mandatory */ },
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
private fun PermissionCard(
	title: String,
	description: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	isGranted: Boolean = false,
	isEnabled: Boolean = true,
	isAdvanced: Boolean = false,
	onClick: () -> Unit = {}
) {
	val backgroundColor = if (isAdvanced) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
	val borderColor = if (isAdvanced) Color(0xFFFF9F0A).copy(alpha = 0.2f) else Color.Transparent

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(backgroundColor)
			.border(1.dp, borderColor, RoundedCornerShape(16.dp))
			.then(if (isEnabled) Modifier.clickable(onClick = onClick) else Modifier)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Box(
			modifier = Modifier
				.size(48.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(if (isGranted) Color(0xFF34C759).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = if (isGranted) Color(0xFF34C759) else if (isAdvanced) Color(0xFFFF9F0A) else MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(24.dp)
			)
		}
		
		Column(modifier = Modifier.weight(1f)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.onSurface
				)
				
				if (isAdvanced) {
					Box(
						modifier = Modifier
							.background(Color(0xFFFF9F0A).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
							.padding(horizontal = 6.dp, vertical = 2.dp)
					) {
						Text(
							text = "ADVANCED",
							style = MaterialTheme.typography.labelSmall,
							color = Color(0xFFFF9F0A),
							fontSize = 8.sp
						)
					}
				}

				// Toggle Switch UI
				Box(
					modifier = Modifier
						.width(48.dp)
						.height(28.dp)
						.clip(RoundedCornerShape(999.dp))
						.background(if (isGranted) Color(0xFF34C759) else Color.Gray.copy(alpha = 0.3f))
						.padding(2.dp),
					contentAlignment = if (isGranted) Alignment.CenterEnd else Alignment.CenterStart
				) {
					Box(
						modifier = Modifier
							.size(24.dp)
							.clip(CircleShape)
							.background(Color.White)
					)
				}
			}
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
private fun OnboardingRootDetectionScreen(
	onContinue: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 24.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.SpaceBetween,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			// Hero Section
			Box(
				modifier = Modifier
					.padding(vertical = 32.dp),
				contentAlignment = Alignment.Center
			) {
				Box(
					modifier = Modifier
						.size(192.dp)
						.clip(CircleShape)
						.background(Color(0xFFA855F7).copy(alpha = 0.2f))
						.blur(60.dp)
				)
				Box(
					modifier = Modifier
						.size(120.dp)
						.background(
							Brush.linearGradient(
								colors = listOf(Color(0xFF1F2937), Color(0xFF101922))
							),
							CircleShape
						)
						.border(1.dp, Color(0xFF374151), CircleShape),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = androidx.compose.material.icons.Icons.Rounded.AdminPanelSettings,
						contentDescription = null,
						tint = Color(0xFFA855F7),
						modifier = Modifier.size(64.dp)
					)
				}
			}

			// Badge
			Box(
				modifier = Modifier
					.padding(bottom = 24.dp)
					.background(Color(0xFFA855F7).copy(alpha = 0.1f), RoundedCornerShape(999.dp))
					.border(1.dp, Color(0xFFA855F7).copy(alpha = 0.2f), RoundedCornerShape(999.dp))
					.padding(horizontal = 12.dp, vertical = 4.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Icon(
						imageVector = androidx.compose.material.icons.Icons.Rounded.FlashOn, // Using FlashOn as Bolt might be missing
						contentDescription = null,
						tint = Color(0xFFA855F7),
						modifier = Modifier.size(14.dp)
					)
					Text(
						text = "ADVANCED MODE",
						style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
						color = Color(0xFFA855F7),
						fontSize = 10.sp
					)
				}
			}

			Text(
				text = "Advanced Mode Available",
				style = MaterialTheme.typography.headlineSmall.copy(
					fontWeight = FontWeight.Bold
				),
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "We detected root access on this device. This unlocks powerful privacy controls not available to standard users.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.height(32.dp))
			Column(
				verticalArrangement = Arrangement.spacedBy(12.dp),
				modifier = Modifier.fillMaxWidth()
			) {
				FeatureRowCard(
					title = "System-wide Ad Blocking",
					description = "Block ads in all apps, not just browsers.",
					icon = androidx.compose.material.icons.Icons.Rounded.Block,
					iconColor = Color(0xFFA855F7)
				)
				FeatureRowCard(
					title = "Deep Data Wiping",
					description = "Recover storage from hidden temp files.",
					icon = androidx.compose.material.icons.Icons.Rounded.DeleteForever,
					iconColor = Color(0xFFA855F7)
				)
				FeatureRowCard(
					title = "Kernel-level Firewall",
					description = "Total control over app network access.",
					icon = androidx.compose.material.icons.Icons.Rounded.Security,
					iconColor = Color(0xFFA855F7)
				)
			}
		}
		Button(
			onClick = onContinue,
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp),
			shape = RoundedCornerShape(16.dp)
		) {
			Text(text = "Continue")
		}
	}
}

@Composable
private fun FeatureRowCard(
	title: String,
	description: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	iconColor: Color
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
			.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(iconColor.copy(alpha = 0.1f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = iconColor,
				modifier = Modifier.size(24.dp)
			)
		}
		
		Column {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
				color = MaterialTheme.colorScheme.onSurface
			)
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
