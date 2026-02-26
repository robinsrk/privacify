package dev.robin.privacify.presentation.home

data class DashboardUiState(
	val privacyScore: Int = 100,
	val statusSubtitle: String = "System integrity verified. No unauthorized access detected.",
	val micAccessCount: Int = 0,
	val cameraAccessCount: Int = 0,
	val locationAccessCount: Int = 0,
	val firewallEnabled: Boolean = false,
	val secureNetworkSummary: String = "Firewall disabled",
	val lockdownEnabled: Boolean = false,
	val micDisabled: Boolean = false,
	val cameraDisabled: Boolean = false,
	val isRooted: Boolean = false,
	val isScanning: Boolean = false
)

enum class QuickAction {
	Lockdown,
	MicKill,
	CameraKill,
	Firewall
}
