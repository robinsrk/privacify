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

private val DarkColorScheme = darkColorScheme(
	primary = PrimaryDark,
	onPrimary = OnPrimaryDark,
	primaryContainer = PrimaryContainerDark,
	onPrimaryContainer = OnPrimaryContainerDark,
	secondary = SecondaryDark,
	onSecondary = OnSecondaryDark,
	secondaryContainer = SecondaryContainerDark,
	onSecondaryContainer = OnSecondaryContainerDark,
	tertiary = TertiaryDark,
	onTertiary = OnTertiaryDark,
	tertiaryContainer = TertiaryContainerDark,
	onTertiaryContainer = OnTertiaryContainerDark,
	error = ErrorDark,
	onError = OnErrorDark,
	errorContainer = ErrorContainerDark,
	onErrorContainer = OnErrorContainerDark,
	surface = SurfaceDark,
	onSurface = OnSurfaceDark,
	onSurfaceVariant = OnSurfaceVariantDark,
	surfaceContainerLowest = SurfaceContainerLowestDark,
	surfaceContainerLow = SurfaceContainerLowDark,
	surfaceContainer = SurfaceContainerDark,
	surfaceContainerHigh = SurfaceContainerHighDark,
	surfaceContainerHighest = SurfaceContainerHighestDark,
	surfaceDim = SurfaceDimDark,
	surfaceBright = SurfaceBrightDark,
	outline = OutlineDark,
	outlineVariant = OutlineVariantDark,
	inverseSurface = InverseSurfaceDark,
	inverseOnSurface = InverseOnSurfaceDark,
	inversePrimary = InversePrimaryDark,
)

private val LightColorScheme = lightColorScheme(
	primary = PrimaryLight,
	onPrimary = OnPrimaryLight,
	primaryContainer = PrimaryContainerLight,
	onPrimaryContainer = OnPrimaryContainerLight,
	secondary = SecondaryLight,
	onSecondary = OnSecondaryLight,
	secondaryContainer = SecondaryContainerLight,
	onSecondaryContainer = OnSecondaryContainerLight,
	tertiary = TertiaryLight,
	onTertiary = OnTertiaryLight,
	tertiaryContainer = TertiaryContainerLight,
	onTertiaryContainer = OnTertiaryContainerLight,
	error = ErrorLight,
	onError = OnErrorLight,
	errorContainer = ErrorContainerLight,
	onErrorContainer = OnErrorContainerLight,
	surface = SurfaceLight,
	onSurface = OnSurfaceLight,
	onSurfaceVariant = OnSurfaceVariantLight,
	surfaceContainerLowest = SurfaceContainerLowestLight,
	surfaceContainerLow = SurfaceContainerLowLight,
	surfaceContainer = SurfaceContainerLight,
	surfaceContainerHigh = SurfaceContainerHighLight,
	surfaceContainerHighest = SurfaceContainerHighestLight,
	surfaceDim = SurfaceDimLight,
	surfaceBright = SurfaceBrightLight,
	outline = OutlineLight,
	outlineVariant = OutlineVariantLight,
	inverseSurface = InverseSurfaceLight,
	inverseOnSurface = InverseOnSurfaceLight,
	inversePrimary = InversePrimaryLight,
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
		darkTheme -> DarkColorScheme
		else -> LightColorScheme
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
