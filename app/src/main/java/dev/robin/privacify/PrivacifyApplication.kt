package dev.robin.privacify

import android.app.Application
import android.util.Log
import dev.robin.privacify.core.utils.AppContextProvider
import dev.robin.privacify.core.settings.UserPreferencesManager
import dev.robin.privacify.core.provider.PermissionAutomationProvider
import dev.robin.privacify.core.security.PrivacyControllersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class PrivacifyApplication : Application() {
	
	companion object {
		private const val TAG = "PrivacifyApp"
	}
	
	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "onCreate called")
		AppContextProvider.context = this
		
		setupShizukuListener()
		
		val prefs = UserPreferencesManager.getInstance(this)
		
		try {
			dev.robin.privacify.pro.utils.ShellUtils.shellTypePreference = prefs.shellType.value
		} catch (_: Exception) {}
		
		CoroutineScope(Dispatchers.IO).launch {
			prefs.shellType.collect { type ->
				try {
					dev.robin.privacify.pro.utils.ShellUtils.shellTypePreference = type
				} catch (_: Exception) {}
				try {
					dev.robin.privacify.core.root.RootManagerProvider.instance.refresh()
				} catch (_: Exception) {}
			}
		}
		
		if (prefs.automationEnabled.value) {
			try {
				val controller = PermissionAutomationProvider.provide()
				controller.automatePermissions(true)
			} catch (_: Exception) {}
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			try {
				val lockdownUseCase = PrivacyControllersProvider.lockdownUseCase
				Log.d(TAG, "Checking lockdown state: ${lockdownUseCase.isActive.value}")
			} catch (e: Exception) {
				Log.e(TAG, "Failed to restore lockdown state", e)
			}
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			try {
				checkAndStartShizuku()
			} catch (_: Exception) {}
		}
	}
	
	private fun setupShizukuListener() {
		try {
			Shizuku.addBinderReceivedListenerSticky {
				Log.d(TAG, "Shizuku binder received")
				CoroutineScope(Dispatchers.IO).launch {
					try {
						dev.robin.privacify.core.root.RootManagerProvider.instance.refresh()
					} catch (_: Exception) {}
				}
				val prefs = UserPreferencesManager.getInstance(applicationContext)
				if (prefs.shellType.value == "shizuku") {
					val permission = Shizuku.checkSelfPermission()
					if (permission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
						Log.d(TAG, "Shizuku permission not granted, requesting...")
						try {
							Shizuku.requestPermission(MainActivity.SHIZUKU_PERMISSION_REQUEST_CODE)
						} catch (e: Exception) {
							Log.e(TAG, "Failed to request Shizuku permission", e)
						}
					}
				}
			}
			
			Shizuku.addBinderDeadListener {
				Log.d(TAG, "Shizuku binder dead")
				CoroutineScope(Dispatchers.IO).launch {
					try {
						dev.robin.privacify.core.root.RootManagerProvider.instance.refresh()
					} catch (_: Exception) {}
				}
			}
		} catch (e: Exception) {
			Log.e(TAG, "Failed to setup Shizuku listener", e)
		}
	}
	
	private suspend fun checkAndStartShizuku() {
		try {
			val shellType = dev.robin.privacify.pro.utils.ShellUtils.shellTypePreference
			Log.d(TAG, "Shell type preference: $shellType")
			
			if (shellType == "shizuku" || shellType == "auto") {
				if (!dev.robin.privacify.pro.utils.ShellUtils.isShizukuAvailable()) {
					Log.d(TAG, "Shizuku not running, attempting to start...")
					if (dev.robin.privacify.pro.utils.ShellUtils.isRootAvailable()) {
						dev.robin.privacify.pro.utils.ShellUtils.runRootCommand("shizuku start")
						Log.d(TAG, "Sent shizuku start command")
					}
				}
			}
		} catch (_: Exception) {}
	}
}
