package dev.robin.privacify.data.root

import dev.robin.privacify.domain.root.RootPrivacyController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultRootPrivacyController : RootPrivacyController {
	private val mutableMicDisabled = MutableStateFlow(false)
	private val mutableCameraDisabled = MutableStateFlow(false)
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override val micDisabled: StateFlow<Boolean> = mutableMicDisabled
	override val cameraDisabled: StateFlow<Boolean> = mutableCameraDisabled

	init {
		scope.launch { syncState() }
	}

	private fun syncState() {
		try {
			val micState = readSensorPrivacy("microphone")
			mutableMicDisabled.value = micState
			val camState = readSensorPrivacy("camera")
			mutableCameraDisabled.value = camState
		} catch (_: Exception) {
			// Not rooted or sensor_privacy not available
		}
	}

	private fun readSensorPrivacy(sensor: String): Boolean {
		return try {
			val process = ProcessBuilder("su", "-c", "cmd sensor_privacy get-state 0 $sensor").start()
			val output = process.inputStream.bufferedReader().readText().trim()
			process.waitFor()
			// Output is typically "enabled" or "disabled", or "true"/"false"
			output.contains("enabled", ignoreCase = true) || output.contains("true", ignoreCase = true)
		} catch (_: Exception) {
			false
		}
	}

	override suspend fun setMicDisabled(disabled: Boolean) {
		withContext(Dispatchers.IO) {
			val state = if (disabled) "true" else "false"
			val appOpsMode = if (disabled) "ignore" else "allow"
			
			// 1. Android 12+ Global Toggle
			executeRootCommand("cmd sensor_privacy set-state 0 microphone $state")

			// 2. Comprehensive AppOps (User + System + Phone)
			val ops = listOf("RECORD_AUDIO", "PHONE_CALL_MICROPHONE", "RECEIVE_AMBIENT_TRIGGER_AUDIO")
			val cmdBuilder = StringBuilder()
			cmdBuilder.append("pm list packages | cut -d: -f2 | while read pkg; do ")
			for (op in ops) {
				cmdBuilder.append("appops set \"\$pkg\" $op $appOpsMode; ")
			}
			cmdBuilder.append("done")
			
			executeRootCommand(cmdBuilder.toString())
			mutableMicDisabled.value = disabled
		}
	}

	override suspend fun setCameraDisabled(disabled: Boolean) {
		withContext(Dispatchers.IO) {
			val state = if (disabled) "true" else "false"
			val appOpsMode = if (disabled) "ignore" else "allow"
			
			// 1. Android 12+ Global Toggle
			executeRootCommand("cmd sensor_privacy set-state 0 camera $state")

			// 2. Comprehensive AppOps (User + System + Phone)
			val ops = listOf("CAMERA", "PHONE_CALL_CAMERA")
			val cmdBuilder = StringBuilder()
			cmdBuilder.append("pm list packages | cut -d: -f2 | while read pkg; do ")
			for (op in ops) {
				cmdBuilder.append("appops set \"\$pkg\" $op $appOpsMode; ")
			}
			cmdBuilder.append("done")
			
			executeRootCommand(cmdBuilder.toString())
			mutableCameraDisabled.value = disabled
		}
	}

	private fun executeRootCommand(command: String): Boolean {
		return try {
			val process = ProcessBuilder("su").start()
			val os = java.io.DataOutputStream(process.outputStream)
			os.writeBytes("$command\n")
			os.writeBytes("exit\n")
			os.flush()
			os.close()
			process.waitFor() == 0
		} catch (e: Exception) {
			e.printStackTrace()
			false
		}
	}
}

