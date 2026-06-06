package dev.robin.privacify.pro.utils

import android.util.Log
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.DataOutputStream
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShellUtils {

    private const val TAG = "ShellUtils"
    private const val ROOT_TIMEOUT = 5L

    @Volatile
    private var binderReceived = false

    var shellTypePreference: String = "auto"
        set(value) {
            field = value
            Log.d(TAG, "Shell type preference set to: $value")
        }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky {
                binderReceived = true
                Log.d(TAG, "Shizuku binder received")
            }
            Shizuku.addBinderDeadListener {
                binderReceived = false
                Log.d(TAG, "Shizuku binder dead")
            }
            if (Shizuku.pingBinder()) {
                binderReceived = true
                Log.d(TAG, "Shizuku binder already available on init")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Shizuku listeners", e)
        }
    }

    fun setShellType(type: String) {
        shellTypePreference = type
    }

    fun runRootCommand(command: String): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        var stdout = StringBuilder()
        var stderr = StringBuilder()
        return try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val stdoutReader = Thread {
                try {
                    BufferedReader(InputStreamReader(process.inputStream)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            stdout.append(line).append('\n')
                        }
                    }
                } catch (_: Exception) { /* ignore */ }
            }
            val stderrReader = Thread {
                try {
                    BufferedReader(InputStreamReader(process.errorStream)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            stderr.append(line).append('\n')
                        }
                    }
                } catch (_: Exception) { /* ignore */ }
            }
            stdoutReader.start()
            stderrReader.start()

            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor(ROOT_TIMEOUT, TimeUnit.SECONDS)

            stdoutReader.join(1000)
            stderrReader.join(1000)

            val ok = try {
                process.exitValue() == 0
            } catch (_: IllegalThreadStateException) {
                process.destroyForcibly()
                false
            }
            if (!stdout.isEmpty()) {
                Log.d(TAG, "root stdout: $stdout")
            }
            if (!stderr.isEmpty()) {
                Log.d(TAG, "root stderr: $stderr")
            }
            ok
        } catch (e: Exception) {
            Log.e(TAG, "root command failed", e)
            false
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun isRootAvailable(): Boolean {
        return runRootCommand("id")
    }

    fun runAdbCommand(command: String): Boolean {
        var process: Process? = null
        return try {
            val shellCmd = if (command.startsWith("adb ")) {
                command.removePrefix("adb ")
            } else {
                command
            }
            process = Runtime.getRuntime().exec(arrayOf("adb", "shell", shellCmd))
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.e(TAG, "adb command failed", e)
            false
        } finally {
            process?.destroy()
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            val available = Shizuku.pingBinder()
            Log.d(TAG, "Shizuku ping result: $available")
            if (available) {
                binderReceived = true
            }
            available
        } catch (e: Exception) {
            Log.d(TAG, "Shizuku not available: ${e.message}")
            false
        }
    }

    fun isShizukuGranted(): Boolean {
        if (!binderReceived && !Shizuku.pingBinder()) {
            Log.d(TAG, "Shizuku binder not yet available")
            return false
        }
        
        return try {
            val result = Shizuku.checkSelfPermission()
            Log.d(TAG, "Shizuku checkSelfPermission: $result, GRANTED=${android.content.pm.PackageManager.PERMISSION_GRANTED}")
            result == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku permission check failed", e)
            false
        }
    }
    
    fun isShizukuReady(): Boolean {
        return isShizukuAvailable() && isShizukuGranted()
    }

    fun requestShizukuPermission(requestCode: Int, callback: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(callback)
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku permission request failed", e)
        }
    }

    fun getShizukuStatus(context: android.content.Context? = null): String {
        Log.d(TAG, "getShizukuStatus called")
        
        val pingResult = try {
            val result = Shizuku.pingBinder()
            Log.d(TAG, "Shizuku pingBinder result: $result")
            result
        } catch (e: Exception) {
            Log.d(TAG, "Shizuku ping failed: ${e.message}")
            if (context != null) {
                val packageInstalled = try {
                    context.packageManager.getPackageInfo("rikka.shizuku.privileged.api", 0)
                    true
                } catch (e: Exception) {
                    false
                }
                if (!packageInstalled) {
                    return "Not installed"
                }
                return "Not running"
            }
            return "Not installed"
        }
        
        if (!pingResult) {
            return "Not running"
        }
        
        val granted = try {
            val permission = Shizuku.checkSelfPermission()
            Log.d(TAG, "Shizuku checkSelfPermission result: $permission")
            permission == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.d(TAG, "Shizuku checkSelfPermission failed: ${e.message}")
            false
        }
        
        return if (!granted) "Not granted" else "Ready"
    }

    fun checkShizukuPackageInstalled(context: android.content.Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("rikka.shizuku.privileged.api", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isShizukuAutoStartEnabled(context: android.content.Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences("shizuku_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getBoolean("auto_start", false)
        } catch (e: Exception) {
            false
        }
    }

    fun requestShizukuAutoStart(context: android.content.Context): Boolean {
        return try {
            val intent = android.content.Intent("moe.shizuku.action.REQUEST_AUTO_START")
            intent.setPackage("rikka.shizuku.privileged.api")
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Requested Shizuku autostart")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request Shizuku autostart", e)
            false
        }
    }

    fun openShizukuAutoStartSettings(context: android.content.Context) {
        try {
            val intent = android.content.Intent("moe.shizuku.action.APP_SETTINGS")
            intent.setPackage("rikka.shizuku.privileged.api")
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Shizuku settings", e)
            // Fallback to opening Shizuku main app
            openShizukuSettings(context)
        }
    }

    fun openShizukuSettings(context: android.content.Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("rikka.shizuku.privileged.api")
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val settingsIntent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:rikka.shizuku.privileged.api")
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Shizuku", e)
        }
    }

    fun runShizukuCommand(command: String): Boolean {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val errorStream = process.errorStream
                val errorReader = BufferedReader(InputStreamReader(errorStream))
                val errorOutput = errorReader.readText()
                Log.d(TAG, "Shizuku stderr: $errorOutput")
            }
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku command failed", e)
            false
        }
    }

    fun runCommandWithFallback(command: String): Boolean {
        val preference = shellTypePreference

        when (preference) {
            "root" -> {
                if (isRootAvailable()) {
                    Log.d(TAG, "Using root (forced)")
                    return runRootCommand(command)
                }
                Log.d(TAG, "Root forced but not available")
                return false
            }
            "shizuku" -> {
                if (isShizukuAvailable() && isShizukuGranted()) {
                    Log.d(TAG, "Using Shizuku (forced)")
                    return runShizukuCommand(command)
                }
                Log.d(TAG, "Shizuku forced but not available")
                return false
            }
            else -> {
                if (isRootAvailable()) {
                    Log.d(TAG, "Using root for command")
                    return runRootCommand(command)
                }
                if (isShizukuAvailable() && isShizukuGranted()) {
                    Log.d(TAG, "Using Shizuku for command")
                    return runShizukuCommand(command)
                }
                Log.d(TAG, "No root or Shizuku available, command may fail")
                return runRootCommand(command)
            }
        }
    }

    fun runRootCommandWithOutput(command: String): String {
        var process: Process? = null
        var os: DataOutputStream? = null
        var stdout = StringBuilder()
        var stderr = StringBuilder()
        return try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            val stdoutReader = Thread {
                try {
                    BufferedReader(InputStreamReader(process.inputStream)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            stdout.append(line).append('\n')
                        }
                    }
                } catch (_: Exception) { /* ignore */ }
            }
            val stderrReader = Thread {
                try {
                    BufferedReader(InputStreamReader(process.errorStream)).use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            stderr.append(line).append('\n')
                        }
                    }
                } catch (_: Exception) { /* ignore */ }
            }
            stdoutReader.start()
            stderrReader.start()

            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor(ROOT_TIMEOUT, TimeUnit.SECONDS)

            stdoutReader.join(1000)
            stderrReader.join(1000)

            stdout.toString().trim()
        } catch (e: Exception) {
            Log.e(TAG, "root command with output failed", e)
            ""
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun runShizukuCommandWithOutput(command: String): String {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val errorStream = process.errorStream
                val errorReader = BufferedReader(InputStreamReader(errorStream))
                val errorOutput = errorReader.readText()
                Log.d(TAG, "Shizuku stderr: $errorOutput")
            }
            stdout.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku command with output failed", e)
            ""
        }
    }

    fun runCommandWithFallbackOutput(command: String): String {
        val preference = shellTypePreference

        when (preference) {
            "root" -> {
                if (isRootAvailable()) {
                    Log.d(TAG, "Using root (forced)")
                    return runRootCommandWithOutput(command)
                }
                Log.d(TAG, "Root forced but not available")
                return ""
            }
            "shizuku" -> {
                if (isShizukuAvailable() && isShizukuGranted()) {
                    Log.d(TAG, "Using Shizuku (forced)")
                    return runShizukuCommandWithOutput(command)
                }
                Log.d(TAG, "Shizuku forced but not available")
                return ""
            }
            else -> {
                if (isRootAvailable()) {
                    return runRootCommandWithOutput(command)
                }
                if (isShizukuAvailable() && isShizukuGranted()) {
                    return runShizukuCommandWithOutput(command)
                }
                return runRootCommandWithOutput(command)
            }
        }
    }
}