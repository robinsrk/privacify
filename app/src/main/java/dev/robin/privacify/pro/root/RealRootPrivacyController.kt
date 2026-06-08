package dev.robin.privacify.pro.root

import dev.robin.privacify.domain.root.RootPrivacyController
import dev.robin.privacify.pro.utils.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealRootPrivacyController : RootPrivacyController {
    private val mutableMicDisabled = MutableStateFlow(false)
    private val mutableCameraDisabled = MutableStateFlow(false)
    private val mutableLocationDisabled = MutableStateFlow(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val micDisabled: StateFlow<Boolean> = mutableMicDisabled
    override val cameraDisabled: StateFlow<Boolean> = mutableCameraDisabled
    override val locationDisabled: StateFlow<Boolean> = mutableLocationDisabled

    init {
        scope.launch { syncState() }
    }

    private fun syncState() {
        try {
            val hasRoot = ShellUtils.isRootAvailable()
            val hasShizuku = ShellUtils.isShizukuAvailable() && ShellUtils.isShizukuGranted()

            if (hasRoot || hasShizuku) {
                val micState = ShellUtils.runCommandWithFallbackOutput("cmd sensor_privacy get-state 0 microphone")
                mutableMicDisabled.value = micState.contains("enabled", ignoreCase = true) || micState.contains("true", ignoreCase = true)

                val camState = ShellUtils.runCommandWithFallbackOutput("cmd sensor_privacy get-state 0 camera")
                mutableCameraDisabled.value = camState.contains("enabled", ignoreCase = true) || camState.contains("true", ignoreCase = true)

                val locMode = ShellUtils.runCommandWithFallbackOutput("settings get secure location_mode").trim()
                val isLocationOn = locMode == "3" || locMode == "2" || locMode == "1"
                val locCmd = ShellUtils.runCommandWithFallbackOutput("cmd location get-location-enabled")
                val cmdSaysOn = locCmd.contains("true", ignoreCase = true)
                mutableLocationDisabled.value = !isLocationOn && !cmdSaysOn
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun setMicDisabled(disabled: Boolean) {
        withContext(Dispatchers.IO) {
            val state = if (disabled) "1" else "0"

            mutableMicDisabled.value = disabled

            val serviceCmd = "service call sensor_privacy 10 i32 0 i32 0 i32 1 i32 $state"
            android.util.Log.d("RealRootPrivacyController", "Attempting mic disable: $serviceCmd")

            val ok = ShellUtils.runCommandWithFallback(serviceCmd)
            android.util.Log.d("RealRootPrivacyController", "Result for mic: $ok")

            if (!ok) {
                val altCmd = "settings put global mic_lock ${if (disabled) "1" else "0"}"
                ShellUtils.runCommandWithFallback(altCmd)
                android.util.Log.d("RealRootPrivacyController", "Tried alt mic command")
            }
        }
    }

    override suspend fun setCameraDisabled(disabled: Boolean) {
        withContext(Dispatchers.IO) {
            val state = if (disabled) "1" else "0"

            mutableCameraDisabled.value = disabled

            val serviceCmd = "service call sensor_privacy 10 i32 0 i32 0 i32 2 i32 $state"
            android.util.Log.d("RealRootPrivacyController", "Attempting camera disable: $serviceCmd")

            val ok = ShellUtils.runCommandWithFallback(serviceCmd)
            android.util.Log.d("RealRootPrivacyController", "Result for camera: $ok")

            if (!ok) {
                val altCmd = "settings put global cam_lock ${if (disabled) "1" else "0"}"
                ShellUtils.runCommandWithFallback(altCmd)
                android.util.Log.d("RealRootPrivacyController", "Tried alt camera command")
            }
        }
    }

    override suspend fun setLocationDisabled(disabled: Boolean) {
        withContext(Dispatchers.IO) {
            mutableLocationDisabled.value = disabled

            val mode = if (disabled) "0" else "3"
            val modeLabel = if (disabled) "0 (off)" else "3 (high accuracy)"
            android.util.Log.d("RealRootPrivacyController", "setLocationDisabled($disabled) — target location_mode=$modeLabel")

            // Always run ALL approaches — some may fail silently, some may work

            // Approach 1: settings put secure (most reliable on rooted devices)
            android.util.Log.d("RealRootPrivacyController", "Approach 1: settings put secure location_mode $mode")
            ShellUtils.runCommandWithFallback("settings put secure location_mode $mode")
            Thread.sleep(100)

            // Approach 2: settings --user current (explicit user context)
            android.util.Log.d("RealRootPrivacyController", "Approach 2: settings --user current put secure location_mode $mode")
            ShellUtils.runCommandWithFallback("settings --user current put secure location_mode $mode")
            Thread.sleep(100)

            // Approach 3: content update via ContentProvider
            android.util.Log.d("RealRootPrivacyController", "Approach 3: content update --uri content://settings/secure")
            ShellUtils.runCommandWithFallback("content update --uri content://settings/secure --bind value:s:$mode --where \"name='location_mode'\"")
            Thread.sleep(100)

            // Approach 4: cmd location (works on some custom ROMs)
            val cmd4 = if (disabled) "cmd location set-location-enabled false"
            else "cmd location set-location-enabled true"
            android.util.Log.d("RealRootPrivacyController", "Approach 4: $cmd4")
            ShellUtils.runCommandWithFallback(cmd4)
            Thread.sleep(100)

            // Approach 5: settings put global (alternative storage)
            android.util.Log.d("RealRootPrivacyController", "Approach 5: settings put global location_mode $mode")
            ShellUtils.runCommandWithFallback("settings put global location_mode $mode")
            Thread.sleep(100)

            // Approach 6: device_config (Android 12+ privacy namespace)
            val cmd6 = if (disabled) "cmd device_config put privacy location_control_disabled true"
            else "cmd device_config put privacy location_control_disabled false"
            android.util.Log.d("RealRootPrivacyController", "Approach 6: $cmd6")
            ShellUtils.runCommandWithFallback(cmd6)

            // Approach 7: Disable WiFi/Bluetooth scanning (eliminates network-based location)
            if (disabled) {
                android.util.Log.d("RealRootPrivacyController", "Approach 7: disable wifi/bt scanning")
                ShellUtils.runCommandWithFallback("settings put global wifi_scan_always_enabled 0")
                ShellUtils.runCommandWithFallback("settings put global ble_scan_always_enabled 0")
            } else {
                ShellUtils.runCommandWithFallback("settings put global wifi_scan_always_enabled 1")
                ShellUtils.runCommandWithFallback("settings put global ble_scan_always_enabled 1")
            }
            Thread.sleep(100)

            // Approach 8: Direct SQLite (last resort)
            if (disabled) {
                android.util.Log.d("RealRootPrivacyController", "Approach 8: sqlite3 direct DB write")
                ShellUtils.runCommandWithFallback("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE secure SET value='0' WHERE name='location_mode'\" 2>/dev/null || true")
                ShellUtils.runCommandWithFallback("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value='0' WHERE name='location_mode'\" 2>/dev/null || true")
            }

            // Verify and retry
            for (attempt in 1..5) {
                val current = ShellUtils.runCommandWithFallbackOutput("settings get secure location_mode 2>/dev/null || settings get secure location_mode 2>&1 || echo unknown").trim()
                val currentGlobal = ShellUtils.runCommandWithFallbackOutput("settings get global location_mode 2>/dev/null || echo unknown").trim()
                val verifyOk = if (disabled) current == "0" else current == "3" || current == "2" || current == "1"
                android.util.Log.d("RealRootPrivacyController", "Verify attempt $attempt: secure=$current global=$currentGlobal target=$modeLabel ok=$verifyOk")
                if (verifyOk) break
                // Retry the most reliable approach
                ShellUtils.runCommandWithFallback("settings put secure location_mode $mode")
                ShellUtils.runCommandWithFallback("settings --user current put secure location_mode $mode")
                Thread.sleep(500)
            }
        }
    }
}