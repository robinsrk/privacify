package dev.robin.privacify.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import dev.robin.privacify.domain.apps.AppPrivacyInfo
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.apps.PermissionScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SystemPermissionScanner(
	private val context: Context
) : PermissionScanner {

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	private val mutableApps = MutableStateFlow<List<AppPrivacyInfo>>(emptyList())
	override val apps: StateFlow<List<AppPrivacyInfo>> = mutableApps.asStateFlow()

	init {
		scope.launch {
			loadInstalledApps()
		}
	}

	private fun loadInstalledApps() {
		val pm = context.packageManager
		val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

		val appInfos = packages.mapNotNull { pkgInfo ->
			val appInfo: ApplicationInfo = pkgInfo.applicationInfo ?: return@mapNotNull null

			if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
				return@mapNotNull null
			}

			val packageName = pkgInfo.packageName
			val appName = appInfo.loadLabel(pm).toString()

			val requested = pkgInfo.requestedPermissions?.toList().orEmpty()
			val grantedPerms = getGrantedPermissions(pkgInfo)

			val summary = buildPermissionsSummary(grantedPerms)
			val icons = buildPermissionIcons(grantedPerms)
			val risk = calculateRisk(requested, grantedPerms)

			AppPrivacyInfo(
				packageName = packageName,
				appName = appName,
				riskLevel = risk,
				permissionsSummary = summary.ifBlank { "No sensitive permissions granted" },
				permissionIcons = icons
			)
		}.sortedBy { it.appName.lowercase() }

		mutableApps.value = appInfos
	}

	override suspend fun refresh() {
		loadInstalledApps()
	}

	private fun getGrantedPermissions(pkgInfo: PackageInfo): List<String> {
		val requested = pkgInfo.requestedPermissions ?: return emptyList()
		val flags = pkgInfo.requestedPermissionsFlags ?: return emptyList()

		return requested.zip(flags.toList())
			.filter { (_, flag) ->
				(flag and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
			}
			.map { (perm, _) -> perm }
	}

	private fun buildPermissionsSummary(granted: List<String>): String {
		val labels = mutableListOf<String>()

		if (granted.any { it == android.Manifest.permission.RECORD_AUDIO }) {
			labels.add("Mic")
		}
		if (granted.any { it == android.Manifest.permission.CAMERA }) {
			labels.add("Camera")
		}
		if (granted.any {
				it == android.Manifest.permission.ACCESS_FINE_LOCATION ||
						it == android.Manifest.permission.ACCESS_COARSE_LOCATION
			}
		) {
			labels.add("Location")
		}
		if (granted.any {
				it == android.Manifest.permission.READ_CONTACTS ||
						it == android.Manifest.permission.WRITE_CONTACTS
			}
		) {
			labels.add("Contacts")
		}
		if (granted.any {
				it == android.Manifest.permission.READ_EXTERNAL_STORAGE ||
						it == android.Manifest.permission.WRITE_EXTERNAL_STORAGE
			}
		) {
			labels.add("Storage")
		}
		if (granted.any {
				it == android.Manifest.permission.READ_SMS ||
						it == android.Manifest.permission.SEND_SMS
			}
		) {
			labels.add("SMS")
		}

		return labels.joinToString(", ")
	}

	private fun buildPermissionIcons(granted: List<String>): List<String> {
		val icons = mutableListOf<String>()

		if (granted.any { it == android.Manifest.permission.RECORD_AUDIO }) {
			icons.add("mic")
		}
		if (granted.any { it == android.Manifest.permission.CAMERA }) {
			icons.add("photo_camera")
		}
		if (granted.any {
				it == android.Manifest.permission.ACCESS_FINE_LOCATION ||
						it == android.Manifest.permission.ACCESS_COARSE_LOCATION
			}
		) {
			icons.add("location_on")
		}
		if (granted.any {
				it == android.Manifest.permission.READ_CONTACTS ||
						it == android.Manifest.permission.WRITE_CONTACTS
			}
		) {
			icons.add("contacts")
		}
		if (granted.any {
				it == android.Manifest.permission.READ_EXTERNAL_STORAGE ||
						it == android.Manifest.permission.WRITE_EXTERNAL_STORAGE
			}
		) {
			icons.add("folder")
		}

		return icons
	}

	private fun calculateRisk(requested: List<String>, granted: List<String>): AppRiskLevel {
		val hasInternet = requested.any { it == android.Manifest.permission.INTERNET }
		val hasMic = granted.any { it == android.Manifest.permission.RECORD_AUDIO }
		val hasCamera = granted.any { it == android.Manifest.permission.CAMERA }
		val hasLocation = granted.any {
			it == android.Manifest.permission.ACCESS_FINE_LOCATION ||
					it == android.Manifest.permission.ACCESS_COARSE_LOCATION
		}
		val hasContacts = granted.any {
			it == android.Manifest.permission.READ_CONTACTS ||
					it == android.Manifest.permission.WRITE_CONTACTS
		}
		val hasSms = granted.any {
			it == android.Manifest.permission.READ_SMS ||
					it == android.Manifest.permission.SEND_SMS
		}

		val sensitiveCount = listOf(
			hasMic,
			hasCamera,
			hasLocation,
			hasContacts,
			hasSms
		).count { it }

		if (sensitiveCount == 0) {
			return AppRiskLevel.Low
		}

		if (sensitiveCount >= 3 && hasInternet) {
			return AppRiskLevel.High
		}

		if ((hasMic || hasCamera) && hasInternet) {
			return AppRiskLevel.High
		}

		return AppRiskLevel.Medium
	}
}
