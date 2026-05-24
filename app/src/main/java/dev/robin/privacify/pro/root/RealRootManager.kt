package dev.robin.privacify.pro.root

import dev.robin.privacify.domain.root.RootManager
import dev.robin.privacify.pro.utils.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealRootManager : RootManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mutableRootStatus = MutableStateFlow(false)
    override val rootStatus: StateFlow<Boolean> = mutableRootStatus

    init {
        try {
            rikka.shizuku.Shizuku.addBinderReceivedListenerSticky {
                scope.launch { refresh() }
            }
            rikka.shizuku.Shizuku.addBinderDeadListener {
                scope.launch { refresh() }
            }
        } catch (_: Exception) {}
        scope.launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        val rooted = checkRoot()
        mutableRootStatus.value = rooted
        android.util.Log.d("RealRootManager","rooted=${rooted}")
    }

    private fun checkRoot(): Boolean {
        val preference = ShellUtils.shellTypePreference
        val result = when (preference) {
            "root" -> ShellUtils.isRootAvailable()
            "shizuku" -> ShellUtils.isShizukuReady()
            else -> ShellUtils.isRootAvailable() || ShellUtils.isShizukuReady()
        }
        android.util.Log.d("RealRootManager","checkRoot result: $result (preference=$preference)")
        return result
    }
}
