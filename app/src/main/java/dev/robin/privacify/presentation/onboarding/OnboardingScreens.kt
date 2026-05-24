package dev.robin.privacify.presentation.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.WifiOff
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
import dev.robin.privacify.ui.components.PrivacifyBadge
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
import dev.robin.privacify.ui.theme.DarkBackground
import dev.robin.privacify.ui.theme.GradientEnd
import dev.robin.privacify.ui.theme.GradientMid
import dev.robin.privacify.ui.theme.GradientStart
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.PurpleVibrant
import dev.robin.privacify.ui.theme.RedVibrant

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

	val totalSteps = if (state.isRootAvailable) 4 else 3
	val currentStep = state.step.ordinal

	val buttonText: String = when (state.step) {
		OnboardingStep.Welcome -> "Get Started"
		OnboardingStep.FeaturesOverview -> "Continue"
		OnboardingStep.SystemCheck -> "Grant & Continue"
		OnboardingStep.RootDetection -> "Continue"
	}

	val canProceed: Boolean = when (state.step) {
		OnboardingStep.SystemCheck -> state.usageAccessGranted
		else -> true
	}

	val onContinue: () -> Unit = when (state.step) {
		OnboardingStep.Welcome -> viewModel::onWelcomeContinue
		OnboardingStep.FeaturesOverview -> viewModel::onFeaturesContinue
		OnboardingStep.SystemCheck -> viewModel::onSystemCheckContinue
		OnboardingStep.RootDetection -> viewModel::onRootDetectionContinue
	}

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = DarkBackground
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.statusBarsPadding()
				.background(
					Brush.verticalGradient(
						colors = listOf(
							Color(0xFF030712),
							Color(0xFF0F172A),
							Color(0xFF020617)
						)
					)
				)
		) {
			Box(modifier = Modifier.weight(1f)) {
				AnimatedContent(
					targetState = state.step,
					transitionSpec = {
						val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
						slideInVertically { it * direction } + fadeIn(tween(300)) togetherWith
						slideOutVertically { -it * direction / 3 } + fadeOut(tween(200))
					},
					label = "onboarding_content"
				) { step ->
					when (step) {
						OnboardingStep.Welcome -> WelcomeContent()
						OnboardingStep.FeaturesOverview -> FeaturesContent(
							onSkip = viewModel::onFeaturesContinue
						)
						OnboardingStep.SystemCheck -> SystemCheckContent(
							state = state,
							onCheckPermissions = viewModel::checkPermissions
						)
						OnboardingStep.RootDetection -> RootDetectionContent()
					}
				}
			}

			OnboardingBottomBar(
				currentStep = currentStep,
				totalSteps = totalSteps,
				buttonText = buttonText,
				buttonEnabled = canProceed,
				onContinue = onContinue
			)
		}
	}
}

@Composable
private fun OnboardingBottomBar(
	currentStep: Int,
	totalSteps: Int,
	buttonText: String,
	buttonEnabled: Boolean,
	onContinue: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color(0xFF020617).copy(alpha = 0.95f))
			.navigationBarsPadding()
			.padding(horizontal = 28.dp)
			.padding(top = 16.dp, bottom = 8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		OnboardingPageIndicator(
			currentStep = currentStep,
			totalSteps = totalSteps
		)

		Spacer(modifier = Modifier.height(16.dp))

		Button(
			onClick = onContinue,
			enabled = buttonEnabled,
			shape = RoundedCornerShape(16.dp),
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
				disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
				disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
			)
		) {
			Text(
				text = buttonText,
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Black
			)
		}

		Spacer(modifier = Modifier.height(4.dp))
	}
}

@Composable
private fun OnboardingPageIndicator(
	currentStep: Int,
	totalSteps: Int
) {
	Row(
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(totalSteps) { index ->
			val isActive = index == currentStep

			val width by animateDpAsState(
				targetValue = if (isActive) 32.dp else 8.dp,
				animationSpec = tween(400),
				label = "dot_width"
			)

			Box(
				modifier = Modifier
					.width(width)
					.height(8.dp)
					.clip(CircleShape)
					.background(
						if (isActive) MaterialTheme.colorScheme.primary
						else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
					)
			)

			if (index < totalSteps - 1) {
				Spacer(modifier = Modifier.width(6.dp))
			}
		}
	}
}

@Composable
private fun WelcomeContent() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 28.dp, vertical = 32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier.fillMaxWidth(),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.size(280.dp)
					.clip(RoundedCornerShape(40.dp))
					.background(
						Brush.linearGradient(
							colors = listOf(GradientStart, GradientMid, GradientEnd)
						)
					),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Rounded.Shield,
					contentDescription = null,
					tint = Color.White.copy(alpha = 0.9f),
					modifier = Modifier.size(120.dp)
				)
			}
		}

		Spacer(modifier = Modifier.height(40.dp))

		Text(
			text = "Welcome to\nPrivacify",
			style = MaterialTheme.typography.displaySmall,
			fontWeight = FontWeight.Black,
			color = Color.White,
			textAlign = TextAlign.Center
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Your ultimate privacy control center.\nMonitor, manage, and protect your data.",
			style = MaterialTheme.typography.bodyLarge,
			color = Color(0xFF94A3B8),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun FeaturesContent(
	onSkip: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp)
			.verticalScroll(rememberScrollState())
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 24.dp),
			horizontalArrangement = Arrangement.End,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Skip",
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
				modifier = Modifier.clickable { onSkip() }
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "Features\nOverview",
			style = MaterialTheme.typography.headlineLarge,
			fontWeight = FontWeight.Black,
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
					icon = Icons.Rounded.VideocamOff,
					iconColor = PurpleVibrant,
					modifier = Modifier.weight(1f)
				)
				FeatureGridCard(
					title = "Block Network",
					description = "Restrict internet access per app instantly.",
					icon = Icons.Rounded.WifiOff,
					iconColor = BlueVibrant,
					modifier = Modifier.weight(1f)
				)
			}
			Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
				FeatureGridCard(
					title = "Lockdown",
					description = "One-tap system wide sensor deactivation.",
					icon = Icons.Rounded.Lock,
					iconColor = RedVibrant,
					modifier = Modifier.weight(1f)
				)
				Spacer(modifier = Modifier.weight(1f))
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Composable
private fun SystemCheckContent(
	state: OnboardingUiState,
	onCheckPermissions: (Context) -> Unit
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
			.padding(horizontal = 24.dp)
			.verticalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "Required Access",
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Black,
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
			PermissionCard(
				title = "Usage Access",
				description = "Allows app to detect which apps are currently active to apply privacy rules in real-time.",
				icon = Icons.Rounded.DataUsage,
				isGranted = state.usageAccessGranted,
				onClick = {
					if (!state.usageAccessGranted) {
						val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
						context.startActivity(intent)
					}
				}
			)

			if (Build.VERSION.SDK_INT >= 33) {
				PermissionCard(
					title = "Notifications",
					description = "Get alerts when apps access your camera or microphone.",
					icon = Icons.Rounded.Notifications,
					isGranted = state.notificationPermissionGranted,
					onClick = {
						if (!state.notificationPermissionGranted) {
							notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
						}
					}
				)
			}

			if (state.isRootAvailable) {
				PermissionCard(
					title = "Root/Shizuku Access",
					description = "Optional. Enables deep system control and tracker blocking.",
					icon = Icons.Rounded.AdminPanelSettings,
					isGranted = state.isRootGranted,
					isAdvanced = true,
					onClick = {
						if (!state.isRootGranted) {
							Toast.makeText(
								context,
								"Open Shizuku app to grant permission",
								Toast.LENGTH_SHORT
							).show()
						}
					}
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		Text(
			text = "Ask me later",
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 12.dp)
				.clickable { },
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
			textAlign = TextAlign.Center
		)

		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Composable
private fun RootDetectionContent() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp, vertical = 32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier.padding(vertical = 16.dp),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.size(192.dp)
					.clip(CircleShape)
					.background(PurpleVibrant.copy(alpha = 0.2f))
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
					imageVector = Icons.Rounded.AdminPanelSettings,
					contentDescription = null,
					tint = PurpleVibrant,
					modifier = Modifier.size(64.dp)
				)
			}
		}

		Box(
			modifier = Modifier
				.padding(bottom = 24.dp)
				.background(PurpleVibrant.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
				.border(1.dp, PurpleVibrant.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
				.padding(horizontal = 12.dp, vertical = 4.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Icon(
					imageVector = Icons.Rounded.FlashOn,
					contentDescription = null,
					tint = PurpleVibrant,
					modifier = Modifier.size(14.dp)
				)
				Text(
					text = "ADVANCED MODE",
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Black,
					color = PurpleVibrant,
					fontSize = 10.sp
				)
			}
		}

		Text(
			text = "Advanced Mode\nAvailable",
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Black,
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
				title = "Auto-Guard",
				description = "Auto-pause kill switches when you use camera or mic.",
				icon = Icons.Rounded.AutoAwesome,
				iconColor = AutoGuardPrimary
			)
		}
	}
}

@Composable
private fun FeatureGridCard(
	title: String,
	description: String,
	icon: ImageVector,
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
			PrivacifyBadge(
				text = "PRO",
				color = iconColor,
				modifier = Modifier.align(Alignment.TopEnd)
			)
		}

		Column {
			PrivacifyIconBox(
				icon = icon,
				tint = iconColor,
				background = iconColor.copy(alpha = 0.1f),
				size = 48,
				iconSize = 24
			)
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Black,
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
private fun PermissionCard(
	title: String,
	description: String,
	icon: ImageVector,
	isGranted: Boolean = false,
	isEnabled: Boolean = true,
	isAdvanced: Boolean = false,
	onClick: () -> Unit = {}
) {
	val backgroundColor = if (isAdvanced) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
	val borderColor = if (isAdvanced) OrangeVibrant.copy(alpha = 0.2f) else Color.Transparent

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.background(backgroundColor)
			.border(1.dp, borderColor, RoundedCornerShape(20.dp))
			.then(if (isEnabled) Modifier.clickable(onClick = onClick) else Modifier)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = if (isGranted) GreenVibrant else if (isAdvanced) OrangeVibrant else MaterialTheme.colorScheme.primary,
			background = if (isGranted) GreenVibrant.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
			size = 48,
			iconSize = 24
		)

		Column(modifier = Modifier.weight(1f)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Black,
					color = MaterialTheme.colorScheme.onSurface
				)

				if (isAdvanced) {
					PrivacifyBadge(text = "ADVANCED", color = OrangeVibrant)
				}

				Box(
					modifier = Modifier
						.width(48.dp)
						.height(28.dp)
						.clip(RoundedCornerShape(999.dp))
						.background(if (isGranted) GreenVibrant else Color.Gray.copy(alpha = 0.3f))
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
private fun FeatureRowCard(
	title: String,
	description: String,
	icon: ImageVector,
	iconColor: Color
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
			.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		PrivacifyIconBox(
			icon = icon,
			tint = iconColor,
			background = iconColor.copy(alpha = 0.1f),
			size = 40,
			iconSize = 24
		)

		Column {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
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
