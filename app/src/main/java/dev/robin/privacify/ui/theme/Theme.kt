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
	primary = Color(0xFF80F0E0),
	onPrimary = Color(0xFF003830),
	primaryContainer = Color(0xFF005048),
	onPrimaryContainer = Color(0xFF9CF5E5),
	secondary = Color(0xFFC8B0FF),
	onSecondary = Color(0xFF32105A),
	secondaryContainer = Color(0xFF4A2872),
	onSecondaryContainer = Color(0xFFE0D0FF),
	tertiary = Color(0xFFA0C8FF),
	onTertiary = Color(0xFF003258),
	tertiaryContainer = Color(0xFF00497D),
	onTertiaryContainer = Color(0xFFD0E3FF),
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceVariant,
	onSurfaceVariant = DarkOnSurfaceVariant,
	outline = DarkOutline,
	outlineVariant = DarkOutline,
	error = Color(0xFFFFB4AB),
	onError = Color(0xFF690005),
	errorContainer = Color(0xFF93000A),
	onErrorContainer = Color(0xFFFFDAD6)
)

private val ExpressiveLightColorScheme = lightColorScheme(
	primary = Color(0xFF006B60),
	onPrimary = Color.White,
	primaryContainer = Color(0xFF9CF5E5),
	onPrimaryContainer = Color(0xFF002019),
	secondary = Color(0xFF634286),
	onSecondary = Color.White,
	secondaryContainer = Color(0xFFE8DCFA),
	onSecondaryContainer = Color(0xFF21005D),
	tertiary = Color(0xFF00668B),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFC8E6FF),
	onTertiaryContainer = Color(0xFF001E38),
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceVariant,
	onSurfaceVariant = LightOnSurfaceVariant,
	outline = LightOutline,
	outlineVariant = LightOutline,
	error = Color(0xFFBA1A1A),
	onError = Color.White,
	errorContainer = Color(0xFFFFDAD6),
	onErrorContainer = Color(0xFF410002)
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
