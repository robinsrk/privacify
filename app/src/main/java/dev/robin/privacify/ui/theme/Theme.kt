package dev.robin.privacify.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExpressiveDarkColorScheme = darkColorScheme(
	primary = DarkPrimary,
	onPrimary = DarkOnPrimary,
	primaryContainer = DarkPrimaryContainer,
	onPrimaryContainer = DarkOnPrimaryContainer,
	secondary = DarkSecondary,
	onSecondary = DarkOnSecondary,
	secondaryContainer = DarkSecondaryContainer,
	onSecondaryContainer = DarkOnSecondaryContainer,
	tertiary = DarkTertiary,
	onTertiary = DarkOnTertiary,
	tertiaryContainer = DarkTertiaryContainer,
	onTertiaryContainer = DarkOnTertiaryContainer,
	error = DarkError,
	onError = DarkOnError,
	errorContainer = DarkErrorContainer,
	onErrorContainer = DarkOnErrorContainer,
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceContainer,
	onSurfaceVariant = DarkOnSurfaceVariant,
	surfaceContainerLowest = DarkSurfaceContainerLowest,
	surfaceContainerLow = DarkSurfaceContainerLow,
	surfaceContainer = DarkSurfaceContainer,
	surfaceContainerHigh = DarkSurfaceContainerHigh,
	surfaceContainerHighest = DarkSurfaceContainerHighest,
	surfaceDim = DarkSurfaceDim,
	surfaceBright = DarkSurfaceBright,
	outline = DarkOutline,
	outlineVariant = DarkOutlineVariant,
	inverseSurface = DarkInverseSurface,
	inverseOnSurface = DarkInverseOnSurface,
	inversePrimary = DarkInversePrimary,
	surfaceTint = DarkPrimary
)

private val ExpressiveLightColorScheme = lightColorScheme(
	primary = LightPrimary,
	onPrimary = LightOnPrimary,
	primaryContainer = LightPrimaryContainer,
	onPrimaryContainer = LightOnPrimaryContainer,
	secondary = LightSecondary,
	onSecondary = LightOnSecondary,
	secondaryContainer = LightSecondaryContainer,
	onSecondaryContainer = LightOnSecondaryContainer,
	tertiary = LightTertiary,
	onTertiary = LightOnTertiary,
	tertiaryContainer = LightTertiaryContainer,
	onTertiaryContainer = LightOnTertiaryContainer,
	error = LightError,
	onError = LightOnError,
	errorContainer = LightErrorContainer,
	onErrorContainer = LightOnErrorContainer,
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceContainer,
	onSurfaceVariant = LightOnSurfaceVariant,
	surfaceContainerLowest = LightSurfaceContainerLowest,
	surfaceContainerLow = LightSurfaceContainerLow,
	surfaceContainer = LightSurfaceContainer,
	surfaceContainerHigh = LightSurfaceContainerHigh,
	surfaceContainerHighest = LightSurfaceContainerHighest,
	surfaceDim = LightSurfaceDim,
	surfaceBright = LightSurfaceBright,
	outline = LightOutline,
	outlineVariant = LightOutlineVariant,
	inverseSurface = LightInverseSurface,
	inverseOnSurface = LightInverseOnSurface,
	inversePrimary = LightInversePrimary,
	surfaceTint = LightPrimary
)

@Composable
fun PrivacifyTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}
		darkTheme -> ExpressiveDarkColorScheme
		else -> ExpressiveLightColorScheme
	}

	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
			WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
		}
	}

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		shapes = PrivacifyShapes,
		content = content
	)
}
