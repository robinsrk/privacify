package dev.robin.privacify.core.settings

import android.content.Context
import android.content.SharedPreferences
import dev.robin.privacify.core.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("privacify_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _scanFrequency = MutableStateFlow(prefs.getString("scan_frequency", "Daily") ?: "Daily")
    val scanFrequency: StateFlow<String> = _scanFrequency

    fun setTheme(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun cycleTheme() {
        val next = when (_themeMode.value) {
            AppThemeMode.Dark -> AppThemeMode.System
            AppThemeMode.System -> AppThemeMode.Light
            AppThemeMode.Light -> AppThemeMode.Dark
        }
        setTheme(next)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun setScanFrequency(frequency: String) {
        _scanFrequency.value = frequency
        prefs.edit().putString("scan_frequency", frequency).apply()
    }

    private fun loadTheme(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.Dark.name)
        return try {
            AppThemeMode.valueOf(name!!)
        } catch (e: Exception) {
            AppThemeMode.Dark
        }
    }

    companion object {
        private var instance: UserPreferencesManager? = null
        fun getInstance(context: Context): UserPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
