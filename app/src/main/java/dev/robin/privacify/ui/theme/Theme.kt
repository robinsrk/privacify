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
	primary = BluePrimary,
	onPrimary = Color.White,
	secondary = BlueLight,
	onSecondary = Color(0xFF001E38),
	tertiary = Purple500,
	background = DarkBackground,
	onBackground = DarkOnBackground,
	surface = DarkSurface,
	onSurface = DarkOnSurface,
	surfaceVariant = DarkSurfaceVariant,
	onSurfaceVariant = DarkOnSurfaceVariant,
	outline = DarkOutline,
	outlineVariant = DarkOutline,
	error = Red500,
	onError = Color.White
)

private val LightColorScheme = lightColorScheme(
	primary = BluePrimary,
	onPrimary = Color.White,
	secondary = BlueDark,
	onSecondary = Color.White,
	tertiary = Purple500,
	background = LightBackground,
	onBackground = LightOnBackground,
	surface = LightSurface,
	onSurface = LightOnSurface,
	surfaceVariant = LightSurfaceVariant,
	onSurfaceVariant = LightOnSurfaceVariant,
	outline = LightOutline,
	outlineVariant = LightOutline,
	error = Red500,
	onError = Color.White
)

@Composable
fun PrivacifyTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = false,
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