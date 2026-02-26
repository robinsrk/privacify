package dev.robin.privacify.presentation.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.robin.privacify.domain.apps.AppRiskLevel
import dev.robin.privacify.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDetailViewModel(
    private val packageName: String,
    private val packageManager: PackageManager,
    private val rootManager: RootManager
) : ViewModel() {

    private val mutableState = MutableStateFlow<AppDetailInfo?>(null)
    val state: StateFlow<AppDetailInfo?> = mutableState

    private val _isRooted = MutableStateFlow(false)
    val isRooted: StateFlow<Boolean> = _isRooted

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    init {
        loadAppDetails()
        viewModelScope.launch {
            rootManager.rootStatus.collectLatest { rooted ->
                _isRooted.value = rooted
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }

    private fun loadAppDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val appInfo = pkgInfo.applicationInfo ?: return@launch
                val appName = appInfo.loadLabel(packageManager).toString()

                val requested = pkgInfo.requestedPermissions?.toList().orEmpty()
                val flags = pkgInfo.requestedPermissionsFlags?.toList().orEmpty()

                val sensitivePermissions = mapOf(
                    android.Manifest.permission.RECORD_AUDIO to "Record audio through the microphone",
                    android.Manifest.permission.CAMERA to "Take photos and record video",
                    android.Manifest.permission.ACCESS_FINE_LOCATION to "Access precise GPS location",
                    android.Manifest.permission.ACCESS_COARSE_LOCATION to "Access approximate location",
                    android.Manifest.permission.READ_CONTACTS to "Read your contacts list",
                    android.Manifest.permission.WRITE_CONTACTS to "Modify your contacts list",
                    android.Manifest.permission.READ_EXTERNAL_STORAGE to "Read files from storage",
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "Write files to storage",
                    android.Manifest.permission.READ_SMS to "Read your SMS messages",
                    android.Manifest.permission.SEND_SMS to "Send SMS messages",
                    android.Manifest.permission.READ_CALL_LOG to "Read your call history",
                    android.Manifest.permission.READ_PHONE_STATE to "Read phone state and identity",
                    android.Manifest.permission.INTERNET to "Full network access"
                )

                val permDetails = mutableListOf<PermissionDetail>()

                requested.forEachIndexed { index, perm ->
                    val desc = sensitivePermissions[perm]
                    if (desc != null) {
                        val granted = if (index < flags.size) {
                            (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                        } else false

                        val shortName = perm.substringAfterLast(".")
                            .replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }

                        permDetails.add(
                            PermissionDetail(
                                name = shortName,
                                description = desc,
                                isGranted = granted
                            )
                        )
                    }
                }

                val grantedCount = permDetails.count { it.isGranted }
                val risk = calculateRiskFromDetails(permDetails, requested)

                mutableState.value = AppDetailInfo(
                    packageName = packageName,
                    appName = appName,
                    riskLevel = risk,
                    permissions = permDetails,
                    grantedCount = grantedCount,
                    totalCount = permDetails.size
                )
            } catch (e: Exception) {
                mutableState.value = null
            }
        }
    }

    private fun calculateRiskFromDetails(perms: List<PermissionDetail>, requested: List<String>): AppRiskLevel {
        val hasInternet = requested.any { it == android.Manifest.permission.INTERNET }
        val grantedNames = perms.filter { it.isGranted }.map { it.name.lowercase() }

        val hasMic = grantedNames.any { it.contains("record") || it.contains("audio") }
        val hasCamera = grantedNames.any { it.contains("camera") }
        val hasLocation = grantedNames.any { it.contains("location") }
        val hasContacts = grantedNames.any { it.contains("contacts") }
        val hasSms = grantedNames.any { it.contains("sms") }

        val sensitiveGranted = listOf(hasMic, hasCamera, hasLocation, hasContacts, hasSms).count { it }

        if (sensitiveGranted == 0) return AppRiskLevel.Low
        if (sensitiveGranted >= 3 && hasInternet) return AppRiskLevel.High
        if ((hasMic || hasCamera) && hasInternet) return AppRiskLevel.High
        return AppRiskLevel.Medium
    }

    fun forceRevokePermissions() {
        if (!_isRooted.value) {
            _actionResult.value = "Root access required"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Revoke each granted runtime permission individually
            val current = mutableState.value ?: return@launch
            val runtimePerms = current.permissions.filter { it.isGranted }.map { perm ->
                "android.permission." + perm.name.uppercase().replace(" ", "_")
            }
            val cmd = StringBuilder()
            for (perm in runtimePerms) {
                cmd.append("pm revoke $packageName $perm 2>/dev/null; ")
            }
            val success = executeRootCommand(cmd.toString())
            withContext(Dispatchers.Main) {
                _actionResult.value = if (success) "Permissions revoked" else "Failed to revoke permissions"
            }
            loadAppDetails() // Refresh
        }
    }

    fun freezeApp() {
        if (!_isRooted.value) {
            _actionResult.value = "Root access required"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = executeRootCommand("pm disable-user --user 0 $packageName")
            withContext(Dispatchers.Main) {
                _actionResult.value = if (success) "App frozen" else "Failed to freeze app"
            }
        }
    }

    fun blockSensorAccess() {
        if (!_isRooted.value) {
            _actionResult.value = "Root access required"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val ops = listOf("CAMERA", "RECORD_AUDIO", "COARSE_LOCATION", "FINE_LOCATION", "BODY_SENSORS")
            val cmd = ops.joinToString("; ") { op ->
                "appops set $packageName $op ignore"
            }
            val success = executeRootCommand(cmd)
            withContext(Dispatchers.Main) {
                _actionResult.value = if (success) "Sensor access blocked" else "Failed to block sensors"
            }
        }
    }

    private fun executeRootCommand(command: String): Boolean {
        return try {
            val process = ProcessBuilder("su").start()
            val os = java.io.DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        fun factory(packageName: String, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppDetailViewModel(
                        packageName = packageName,
                        packageManager = context.packageManager,
                        rootManager = dev.robin.privacify.core.root.RootManagerProvider.instance
                    ) as T
                }
            }
        }
    }
}
