package dev.robin.privacify.data.root

import dev.robin.privacify.domain.root.RootManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class DefaultRootManager : RootManager {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	private val mutableRootStatus = MutableStateFlow(false)
	override val rootStatus: StateFlow<Boolean> = mutableRootStatus

	init {
		scope.launch {
			refresh()
		}
	}

	override suspend fun refresh() {
		val rooted = checkRoot()
		mutableRootStatus.value = rooted
	}

	private fun checkRoot(): Boolean {
		return try {
			val process = Runtime.getRuntime().exec(arrayOf("su", "-v"))
			process.waitFor() == 0
		} catch (e: Exception) {
			// su not found or permission denied
			checkRootPaths()
		}
	}

	private fun checkRootPaths(): Boolean {
		val paths = listOf(
			"/system/app/Superuser.apk",
			"/sbin/su",
			"/system/bin/su",
			"/system/xbin/su",
			"/data/local/xbin/su",
			"/data/local/bin/su",
			"/system/sd/xbin/su",
			"/system/bin/failsafe/su",
			"/data/local/su"
		)

		return paths.any { path ->
			File(path).exists()
		}
	}
}

