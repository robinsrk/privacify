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

private val DarkColorScheme = darkColorScheme(
	primary = TealPrimary,
	onPrimary = Color.White,
	primaryContainer = TealDark,
	onPrimaryContainer = TealLight,
	secondary = VioletPrimary,
	onSecondary = Color.White,
	secondaryContainer = VioletDark,
	onSecondaryContainer = VioletLight,
	tertiary = BluePrimary,
	onTertiary = Color.White,
	tertiaryContainer = BlueDark,
	onTertiaryContainer = BlueLight,
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceVariant,
	onSurfaceVariant = DarkOnSurfaceVariant,
	outline = DarkOutline,
	outlineVariant = DarkOutline,
	error = Red500,
	onError = Color.White,
	errorContainer = Color(0xFF93000A),
	onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
	primary = TealPrimary,
	onPrimary = Color.White,
	primaryContainer = TealLight,
	onPrimaryContainer = Color(0xFF002019),
	secondary = VioletPrimary,
	onSecondary = Color.White,
	secondaryContainer = VioletLight,
	onSecondaryContainer = Color(0xFF21005D),
	tertiary = BluePrimary,
	onTertiary = Color.White,
	tertiaryContainer = BlueLight,
	onTertiaryContainer = Color(0xFF001E38),
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceVariant,
	onSurfaceVariant = LightOnSurfaceVariant,
	outline = LightOutline,
	outlineVariant = LightOutline,
	error = Red500,
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
		content = content
	)
}