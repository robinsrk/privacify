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

                val locState = ShellUtils.runCommandWithFallbackOutput("cmd location get-location-enabled")
                mutableLocationDisabled.value = !locState.contains("true", ignoreCase = true)
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

            val cmd = if (disabled) "cmd location set-location-enabled false"
            else "cmd location set-location-enabled true"
            android.util.Log.d("RealRootPrivacyController", "Attempting location toggle: $cmd")

            val ok = ShellUtils.runCommandWithFallback(cmd)
            android.util.Log.d("RealRootPrivacyController", "Result for location: $ok")

            if (!ok) {
                val mode = if (disabled) "0" else "3"
                val altCmd = "settings put secure location_mode $mode"
                ShellUtils.runCommandWithFallback(altCmd)
                android.util.Log.d("RealRootPrivacyController", "Tried alt location command: $altCmd")
            }
        }
    }
}