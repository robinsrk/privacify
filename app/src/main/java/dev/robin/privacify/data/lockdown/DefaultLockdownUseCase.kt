package dev.robin.privacify.data.lockdown

import android.content.Context
import android.util.Log
import dev.robin.privacify.core.utils.AppContextProvider
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootPrivacyController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultLockdownUseCase(
	private val rootPrivacyController: RootPrivacyController
) : LockdownUseCase {
	
	companion object {
		private const val TAG = "LockdownUseCase"
		private const val PREFS_NAME = "lockdown_prefs"
		private const val KEY_LOCKDOWN_ACTIVE = "lockdown_active"
	}
	
	private val context: Context = AppContextProvider.context ?: throw IllegalStateException("AppContextProvider not initialized")
	private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	
	private val _lockdownActiveFlow = MutableStateFlow(isLockdownActive())
	override val isActive: StateFlow<Boolean> = _lockdownActiveFlow.asStateFlow()
	
	init {
		CoroutineScope(Dispatchers.IO).launch {
			restoreLockdownIfNeeded()
		}
	}
	
	private fun isLockdownActive(): Boolean {
		return prefs.getBoolean(KEY_LOCKDOWN_ACTIVE, false)
	}
	
	private suspend fun restoreLockdownIfNeeded() {
		if (isLockdownActive()) {
			Log.d(TAG, "Restoring lockdown state...")
			applyLockdownState(true)
		}
	}
	
	private fun applyLockdownState(enabled: Boolean) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				if (enabled) {
					rootPrivacyController.setMicDisabled(true)
					rootPrivacyController.setCameraDisabled(true)
					enableDnd(true)
				} else {
					rootPrivacyController.setMicDisabled(false)
					rootPrivacyController.setCameraDisabled(false)
					enableDnd(false)
				}
			} catch (e: Exception) {
				Log.e(TAG, "Failed to apply lockdown state: $enabled", e)
			}
		}
	}
	
	override suspend fun enableLockdown() {
		Log.d(TAG, "enableLockdown called")
		prefs.edit().putBoolean(KEY_LOCKDOWN_ACTIVE, true).apply()
		_lockdownActiveFlow.value = true
		applyLockdownState(true)
	}
	
	override suspend fun disableLockdown() {
		Log.d(TAG, "disableLockdown called")
		prefs.edit().putBoolean(KEY_LOCKDOWN_ACTIVE, false).apply()
		_lockdownActiveFlow.value = false
		applyLockdownState(false)
	}
	
	fun isEnabled(): Boolean = _lockdownActiveFlow.value
	
	private fun enableDnd(enable: Boolean) {
		try {
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
			if (notificationManager.isNotificationPolicyAccessGranted) {
				val filter = if (enable) {
					android.app.NotificationManager.INTERRUPTION_FILTER_NONE
				} else {
					android.app.NotificationManager.INTERRUPTION_FILTER_ALL
				}
				notificationManager.setInterruptionFilter(filter)
				Log.d(TAG, "DND set to: $filter")
			} else {
				Log.d(TAG, "Notification policy access not granted")
			}
		} catch (e: Exception) {
			Log.e(TAG, "Failed to set DND", e)
		}
	}
}
