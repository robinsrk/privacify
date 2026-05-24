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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExpressiveDarkColorScheme = darkColorScheme(
	primary = Color(0xFF5EEAD4),
	onPrimary = Color(0xFF00382C),
	primaryContainer = Color(0xFF005140),
	onPrimaryContainer = Color(0xFF83F5DF),
	secondary = Color(0xFFD8B4FE),
	onSecondary = Color(0xFF3B0764),
	secondaryContainer = Color(0xFF581C87),
	onSecondaryContainer = Color(0xFFE9D5FF),
	tertiary = Color(0xFF7DD3FC),
	onTertiary = Color(0xFF013343),
	tertiaryContainer = Color(0xFF014B64),
	onTertiaryContainer = Color(0xFFB6ECFF),
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceVariant,
	onSurfaceVariant = DarkOnSurfaceVariant,
	outline = DarkOutline,
	outlineVariant = DarkOutlineVariant,
	error = Color(0xFFFCA5A5),
	onError = Color(0xFF450A0A),
	errorContainer = Color(0xFF7F1D1D),
	onErrorContainer = Color(0xFFFECACA),
	inverseSurface = Color(0xFFE2E8F0),
	inverseOnSurface = Color(0xFF0F172A),
	surfaceTint = Color(0xFF5EEAD4)
)

private val ExpressiveLightColorScheme = lightColorScheme(
	primary = Color(0xFF0D9488),
	onPrimary = Color.White,
	primaryContainer = Color(0xFFCCFBF1),
	onPrimaryContainer = Color(0xFF022C22),
	secondary = Color(0xFF7E22CE),
	onSecondary = Color.White,
	secondaryContainer = Color(0xFFF3E8FF),
	onSecondaryContainer = Color(0xFF2E1065),
	tertiary = Color(0xFF0284C7),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFE0F2FE),
	onTertiaryContainer = Color(0xFF0C4A6E),
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceVariant,
	onSurfaceVariant = LightOnSurfaceVariant,
	outline = LightOutline,
	outlineVariant = LightOutlineVariant,
	error = Color(0xFFDC2626),
	onError = Color.White,
	errorContainer = Color(0xFFFEE2E2),
	onErrorContainer = Color(0xFF450A0A),
	inverseSurface = Color(0xFF1E293B),
	inverseOnSurface = Color(0xFFF8FAFC),
	surfaceTint = Color(0xFF0D9488)
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
