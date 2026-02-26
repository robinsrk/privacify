package dev.robin.privacify.data.root

import dev.robin.privacify.core.utils.AppContextProvider
import java.io.File

object HostsFileManager {
    private const val HOSTS_PATH = "/etc/hosts"

    fun readHosts(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $HOSTS_PATH"))
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Error reading hosts file: ${e.message}"
        }
    }

    fun writeHosts(content: String): Boolean {
        return try {
            val tempFile = File(AppContextProvider.context.cacheDir, "hosts_temp")
            tempFile.writeText(content)
            
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.use { os ->
                os.write("mount -o rw,remount /\n".toByteArray())
                os.write("cp ${tempFile.absolutePath} $HOSTS_PATH\n".toByteArray())
                os.write("chmod 644 $HOSTS_PATH\n".toByteArray())
                os.write("mount -o ro,remount /\n".toByteArray())
                os.write("exit\n".toByteArray())
            }
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun addBlockRule(domain: String): Boolean {
        val current = readHosts()
        if (current.contains(domain)) return true
        val newContent = current.trim() + "\n127.0.0.1 $domain\n"
        return writeHosts(newContent)
    }
}
