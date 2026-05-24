package dev.robin.privacify

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.robin.privacify.core.theme.AppThemeMode
import dev.robin.privacify.core.theme.ThemePreferenceManager
import dev.robin.privacify.presentation.PrivacifyApp
import dev.robin.privacify.ui.theme.PrivacifyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
	
	companion object {
		const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
		var instance: MainActivity? = null
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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