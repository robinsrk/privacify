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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import dev.robin.privacify.ui.components.PrivacifyExpressiveCard
import dev.robin.privacify.ui.components.PrivacifyIconBox
import dev.robin.privacify.ui.theme.AutoGuardPrimary
import dev.robin.privacify.ui.theme.BlueVibrant
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
		OnboardingStep.SystemCheck -> "Continue"
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
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier.fillMaxSize()
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
	var pressed by remember { mutableStateOf(false) }
	val buttonScale by animateFloatAsState(
		targetValue = if (pressed) 0.94f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessMedium
		),
		label = "btn_scale"
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
			.navigationBarsPadding()
			.padding(horizontal = 24.dp)
			.padding(top = 12.dp, bottom = 8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		OnboardingPageIndicator(
			currentStep = currentStep,
			totalSteps = totalSteps
		)

		Spacer(modifier = Modifier.height(16.dp))

		Button(
			onClick = {
				pressed = true
				onContinue()
			},
			enabled = buttonEnabled,
			shape = RoundedCornerShape(999.dp),
			modifier = Modifier
				.fillMaxWidth()
				.height(52.dp)
				.scale(buttonScale),
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
						else MaterialTheme.colorScheme.outlineVariant
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
			.padding(horizontal = 32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier
				.size(120.dp)
				.clip(RoundedCornerShape(28.dp))
				.background(
					MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
				),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Rounded.Shield,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(60.dp)
			)
		}

		Spacer(modifier = Modifier.height(32.dp))

		Text(
			text = "Welcome to\nPrivacify",
			style = MaterialTheme.typography.displaySmall,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Your ultimate privacy control center.\nMonitor, manage, and protect your data.",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun FeaturesContent(
	onSkip: () -> Unit
) {
	val features = listOf(
		FeatureItem(
			title = "Monitor Sensors",
			description = "Track mic, camera and location access in real-time.",
			icon = Icons.Rounded.VideocamOff,
			color = PurpleVibrant
		),
		FeatureItem(
			title = "Block Trackers",
			description = "Restrict internet access and stop data tracking.",
			icon = Icons.Rounded.WifiOff,
			color = BlueVibrant
		),
		FeatureItem(
			title = "Lockdown Mode",
			description = "One-tap system-wide sensor deactivation.",
			icon = Icons.Rounded.Lock,
			color = RedVibrant
		),
		FeatureItem(
			title = "Auto-Guard",
			description = "Smart protection that adapts to your usage.",
			icon = Icons.Rounded.AutoAwesome,
			color = AutoGuardPrimary
		)
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp)
			.verticalScroll(rememberScrollState())
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 20.dp),
			horizontalArrangement = Arrangement.End
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
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
		)

		Spacer(modifier = Modifier.height(20.dp))

		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			features.forEach { feature ->
				PrivacifyExpressiveCard {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(16.dp)
					) {
						PrivacifyIconBox(
							icon = feature.icon,
							tint = feature.color,
							background = feature.color.copy(alpha = 0.12f)
						)
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = feature.title,
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface
							)
							Spacer(modifier = Modifier.height(2.dp))
							Text(
								text = feature.description,
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
	}
}

private data class FeatureItem(
	val title: String,
	val description: String,
	val icon: ImageVector,
	val color: Color
)

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
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
		)

		Spacer(modifier = Modifier.height(24.dp))

		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			PermissionCard(
				title = "Usage Access",
				description = "Allows app to detect which apps are currently active to apply privacy rules in real-time.",
				icon = Icons.Rounded.AdminPanelSettings,
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
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
			.padding(horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier
				.size(128.dp)
				.clip(RoundedCornerShape(28.dp))
				.background(PurpleVibrant.copy(alpha = 0.1f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Rounded.AdminPanelSettings,
				contentDescription = null,
				tint = PurpleVibrant,
				modifier = Modifier.size(64.dp)
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

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
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
			textAlign = TextAlign.Center
		)

		Spacer(modifier = Modifier.height(32.dp))

		PrivacifyExpressiveCard {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				PrivacifyIconBox(
					icon = Icons.Rounded.AutoAwesome,
					tint = AutoGuardPrimary,
					background = AutoGuardPrimary.copy(alpha = 0.12f)
				)
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = "Auto-Guard",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = "Auto-pause kill switches when you use camera or mic.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

@Composable
private fun PermissionCard(
	title: String,
	description: String,
	icon: ImageVector,
	isGranted: Boolean = false,
	isAdvanced: Boolean = false,
	onClick: () -> Unit = {}
) {
	PrivacifyExpressiveCard(
		onClick = onClick
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			PrivacifyIconBox(
				icon = icon,
				tint = if (isGranted) GreenVibrant else if (isAdvanced) OrangeVibrant else MaterialTheme.colorScheme.primary,
				background = if (isGranted) GreenVibrant.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
			)
			Column(modifier = Modifier.weight(1f)) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface
					)
					if (isAdvanced) {
						PrivacifyBadge(text = "ADVANCED", color = OrangeVibrant)
					}
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = description,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
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
	}
}
