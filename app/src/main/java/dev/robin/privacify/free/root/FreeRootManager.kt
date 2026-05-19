package dev.robin.privacify.free.root

import dev.robin.privacify.domain.root.RootManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FreeRootManager : RootManager {
    override val rootStatus: StateFlow<Boolean> = MutableStateFlow(false)
    override suspend fun refresh() {
        // Free version does not support root features
    }
}
