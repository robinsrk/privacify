package dev.robin.privacify.pro.root

import dev.robin.privacify.core.utils.AppContextProvider
import dev.robin.privacify.domain.root.HostsFileRepository
import dev.robin.privacify.pro.utils.ShellUtils
import java.io.File

class RealHostsFileRepository : HostsFileRepository {
    private val HOSTS_PATH = "/etc/hosts"

    override fun readHosts(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $HOSTS_PATH"))
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Error reading hosts file: ${e.message}"
        }
    }

    override fun writeHosts(content: String): Boolean {
        return try {
            val tempFile = File(AppContextProvider.context.cacheDir, "hosts_temp")
            tempFile.writeText(content)
            
            // Using ShellUtils if possible, or manual execution like original
            ShellUtils.runRootCommand("mount -o rw,remount /")
            ShellUtils.runRootCommand("cp ${tempFile.absolutePath} $HOSTS_PATH")
            ShellUtils.runRootCommand("chmod 644 $HOSTS_PATH")
            ShellUtils.runRootCommand("mount -o ro,remount /")
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun addBlockRule(domain: String): Boolean {
        val current = readHosts()
        if (current.contains(domain)) return true
        val newContent = current.trim() + "\n127.0.0.1 $domain\n"
        return writeHosts(newContent)
    }
}
