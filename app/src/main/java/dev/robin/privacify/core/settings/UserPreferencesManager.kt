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

    private val _automationEnabled = MutableStateFlow(prefs.getBoolean("automation_enabled", false))
    val automationEnabled: StateFlow<Boolean> = _automationEnabled

    private val _autostartEnabled = MutableStateFlow(prefs.getBoolean("autostart_enabled", false))
    val autostartEnabled: StateFlow<Boolean> = _autostartEnabled

    private val _scanFrequency = MutableStateFlow(prefs.getString("scan_frequency", "Daily") ?: "Daily")
    val scanFrequency: StateFlow<String> = _scanFrequency

    private val _shellType = MutableStateFlow(prefs.getString("shell_type", "auto") ?: "auto")
    val shellType: StateFlow<String> = _shellType

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

    fun setAutomationEnabled(enabled: Boolean) {
        _automationEnabled.value = enabled
        prefs.edit().putBoolean("automation_enabled", enabled).apply()
    }

    fun setAutostartEnabled(enabled: Boolean) {
        _autostartEnabled.value = enabled
        prefs.edit().putBoolean("autostart_enabled", enabled).apply()
    }

    fun setScanFrequency(frequency: String) {
        _scanFrequency.value = frequency
        prefs.edit().putString("scan_frequency", frequency).apply()
    }

    fun setShellType(type: String) {
        _shellType.value = type
        prefs.edit().putString("shell_type", type).apply()
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
