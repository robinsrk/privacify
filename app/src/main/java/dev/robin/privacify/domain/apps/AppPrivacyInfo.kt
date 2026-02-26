package dev.robin.privacify.domain.apps

enum class AppRiskLevel {
	High,
	Medium,
	Low
}

data class AppPrivacyInfo(
	val packageName: String,
	val appName: String,
	val riskLevel: AppRiskLevel,
	val permissionsSummary: String,
	val permissionIcons: List<String>
)

