package dev.robin.privacify.domain.apps

import kotlinx.coroutines.flow.StateFlow

interface PermissionScanner {
	val apps: StateFlow<List<AppPrivacyInfo>>
	suspend fun refresh()
}
