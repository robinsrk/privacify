package dev.robin.privacify.domain.root

import kotlinx.coroutines.flow.StateFlow

interface RootPrivacyController {
	val micDisabled: StateFlow<Boolean>
	val cameraDisabled: StateFlow<Boolean>
	suspend fun setMicDisabled(disabled: Boolean)
	suspend fun setCameraDisabled(disabled: Boolean)
}

