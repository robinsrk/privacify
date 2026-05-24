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
			val clazz = Class.forName("dev.robin.privacify.pro.utils.ShellUtils")
			val setter = clazz.getMethod("setShellTypePreference", String::class.java)
			setter.invoke(null, prefs.shellType.value)
		} catch (_: Exception) {}
		
		CoroutineScope(Dispatchers.IO).launch {
			prefs.shellType.collect { type ->
				try {
					val clazz = Class.forName("dev.robin.privacify.pro.utils.ShellUtils")
					val setter = clazz.getMethod("setShellTypePreference", String::class.java)
					setter.invoke(null, type)
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
			val shellCls = Class.forName("dev.robin.privacify.pro.utils.ShellUtils")
			val getter = shellCls.getMethod("getShellTypePreference")
			val shellType = getter.invoke(null) as? String ?: "auto"
			Log.d(TAG, "Shell type preference: $shellType")
			
			if (shellType == "shizuku" || shellType == "auto") {
				val isAvailable = shellCls.getMethod("isShizukuAvailable").invoke(null) as? Boolean ?: false
				if (!isAvailable) {
					Log.d(TAG, "Shizuku not running, attempting to start...")
					val isRoot = shellCls.getMethod("isRootAvailable").invoke(null) as? Boolean ?: false
					if (isRoot) {
						val runRoot = shellCls.getMethod("runRootCommand", String::class.java)
						runRoot.invoke(null, "shizuku start")
						Log.d(TAG, "Sent shizuku start command")
					}
				}
			}
		} catch (_: Exception) {}
	}
}
