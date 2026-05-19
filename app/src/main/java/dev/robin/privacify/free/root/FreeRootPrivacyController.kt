package dev.robin.privacify.free.root

import dev.robin.privacify.domain.root.RootPrivacyController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FreeRootPrivacyController : RootPrivacyController {
    override val micDisabled: StateFlow<Boolean> = MutableStateFlow(false)
    override val cameraDisabled: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun setMicDisabled(disabled: Boolean) {
        // Free version does not support root features
    }

    override suspend fun setCameraDisabled(disabled: Boolean) {
        // Free version does not support root features
    }
}
