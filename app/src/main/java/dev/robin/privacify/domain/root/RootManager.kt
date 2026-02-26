package dev.robin.privacify.domain.root

import kotlinx.coroutines.flow.StateFlow

interface RootManager {
	val rootStatus: StateFlow<Boolean>
	suspend fun refresh()
}

