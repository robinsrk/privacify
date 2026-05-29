package dev.robin.privacify

import android.app.Application
import android.util.Log
import dev.robin.privacify.core.utils.AppContextProvider
import dev.robin.privacify.core.settings.UserPreferencesManager
import dev.robin.privacify.core.provider.PermissionAutomationProvider
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.pro.utils.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class PrivacifyApplication : Application() {

	private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	companion object {
		private const val TAG = "PrivacifyApp"
	}

	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "onCreate called")
		AppContextProvider.context = this

		setupShizukuListener()

		val prefs = UserPreferencesManager.getInstance(this)
		ShellUtils.setShellType(prefs.shellType.value)

		applicationScope.launch {
			prefs.shellType.collect { type ->
				ShellUtils.setShellType(type)
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

		applicationScope.launch {
			try {
				val lockdownUseCase = PrivacyControllersProvider.lockdownUseCase
				Log.d(TAG, "Checking lockdown state: ${lockdownUseCase.isActive.value}")
			} catch (e: Exception) {
				Log.e(TAG, "Failed to restore lockdown state", e)
			}
		}

		applicationScope.launch {
			try {
				checkAndStartShizuku()
			} catch (_: Exception) {}
		}
	}

	override fun onTerminate() {
		applicationScope.cancel()
		super.onTerminate()
	}

	private fun setupShizukuListener() {
		try {
			Shizuku.addBinderReceivedListenerSticky {
				Log.d(TAG, "Shizuku binder received")
				applicationScope.launch {
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
				applicationScope.launch {
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
			val shellType = ShellUtils.shellTypePreference
			Log.d(TAG, "Shell type preference: $shellType")

			if (shellType == "shizuku" || shellType == "auto") {
				if (!ShellUtils.isShizukuAvailable()) {
					Log.d(TAG, "Shizuku not running, attempting to start...")
					if (ShellUtils.isRootAvailable()) {
						ShellUtils.runRootCommand("shizuku start")
						Log.d(TAG, "Sent shizuku start command")
					}
				}
			}
		} catch (_: Exception) {}
	}
}
