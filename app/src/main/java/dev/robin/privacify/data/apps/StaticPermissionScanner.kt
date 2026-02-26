package dev.robin.privacify.data.apps

import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

class StaticPermissionScanner : PermissionScanner {
	companion object {
		val sampleApps: List<AppPrivacyInfo> = listOf(
			AppPrivacyInfo(
				packageName = "com.example.chatworld",
				appName = "ChatWorld",
				riskLevel = AppRiskLevel.High,
				permissionsSummary = "Location, Mic, Camera, Contacts +2 more",
				permissionIcons = listOf("location_on", "mic", "photo_camera", "contacts")
			),
			AppPrivacyInfo(
				packageName = "com.example.buyquick",
				appName = "BuyQuick",
				riskLevel = AppRiskLevel.Medium,
				permissionsSummary = "Location, Network",
				permissionIcons = listOf("location_on", "public")
			),
			AppPrivacyInfo(
				packageName = "com.example.zoomin",
				appName = "ZoomIn",
				riskLevel = AppRiskLevel.High,
				permissionsSummary = "Camera, Mic, Screen Share",
				permissionIcons = listOf("photo_camera", "mic", "screen_share")
			),
			AppPrivacyInfo(
				packageName = "com.example.calcmaster",
				appName = "CalcMaster",
				riskLevel = AppRiskLevel.Low,
				permissionsSummary = "No sensitive permissions",
				permissionIcons = emptyList()
			),
			AppPrivacyInfo(
				packageName = "com.example.melodystream",
				appName = "MelodyStream",
				riskLevel = AppRiskLevel.Low,
				permissionsSummary = "Storage",
				permissionIcons = listOf("folder")
			)
		)
	}

	private val mutableApps = MutableStateFlow(sampleApps)

	override val apps: StateFlow<List<AppPrivacyInfo>> = mutableApps.asStateFlow()
	
	override suspend fun refresh() {
		delay(500)
	}
}
