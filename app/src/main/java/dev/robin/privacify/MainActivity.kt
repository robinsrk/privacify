package dev.robin.privacify

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
import dev.robin.privacify.presentation.PrivacifyApp
import dev.robin.privacify.ui.theme.PrivacifyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

	companion object {
		const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
		var instance: MainActivity? = null
	}

	private var isAppReady = false

	override fun onCreate(savedInstanceState: Bundle?) {
		val splashScreen = installSplashScreen()
		super.onCreate(savedInstanceState)

		splashScreen.setKeepOnScreenCondition { !isAppReady }

		splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
			try {
				val splashScreenView = splashScreenViewProvider.view

				val splashIcon = try {
					splashScreenViewProvider.iconView
				} catch (e: Exception) {
					null
				}

				try {
					if (splashIcon != null) {
						val blink = ObjectAnimator.ofFloat(
							splashIcon, "scaleY", 1f, 0.12f, 1f
						).apply {
							duration = 300
						}

						val fadeOut = ObjectAnimator.ofFloat(splashScreenView, "alpha", 1f, 0f).apply {
							duration = 350
						}
						fadeOut.doOnEnd {
							splashScreenViewProvider.remove()
							enableEdgeToEdge()
						}

						blink.doOnEnd { fadeOut.start() }
						blink.start()
					}
				} catch (_: Exception) {}
			} catch (e: Exception) {
				splashScreenViewProvider.remove()
			}
		}

		instance = this

		enableEdgeToEdge()

		setContent {
			val themeMode by ThemePreferenceManager.themeMode.collectAsState()
			val darkTheme = when (themeMode) {
				AppThemeMode.Dark -> true
				AppThemeMode.Light -> false
				AppThemeMode.System -> isSystemInDarkTheme()
			}
			PrivacifyTheme(darkTheme = darkTheme) {
				PrivacifyApp()
			}

			LaunchedEffect(Unit) {
				delay(100)
				isAppReady = true
			}
		}

		setupShizukuPermissionListener()
	}

	private fun setupShizukuPermissionListener() {
		try {
			Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
				if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
					Log.d("MainActivity", "Shizuku permission result: $grantResult")
					if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
						Log.d("MainActivity", "Shizuku permission granted!")
						CoroutineScope(Dispatchers.IO).launch {
							try {
								dev.robin.privacify.core.root.RootManagerProvider.instance.refresh()
							} catch (_: Exception) {}
						}
					} else {
						Log.d("MainActivity", "Shizuku permission denied")
					}
				}
			}
		} catch (e: Exception) {
			Log.e("MainActivity", "Failed to setup Shizuku permission listener", e)
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
			Log.d("MainActivity", "Shizuku permission result: grantResults=${grantResults.contentToString()}")
		}
	}

	fun requestShizukuPermission() {
		Log.d("MainActivity", "requestShizukuPermission called")
		try {
			if (!Shizuku.pingBinder()) {
				Log.w("MainActivity", "Shizuku not running")
				return
			}

			if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				Log.d("MainActivity", "Already has permission")
				return
			}

			Log.d("MainActivity", "Requesting Shizuku permission with code $SHIZUKU_PERMISSION_REQUEST_CODE")
			Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
			Log.d("MainActivity", "Permission request sent")
		} catch (e: Exception) {
			Log.e("MainActivity", "Failed to request Shizuku permission", e)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		instance = null
	}
}
