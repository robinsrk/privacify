package dev.robin.privacify.free.root

import dev.robin.privacify.domain.root.HostsFileRepository

class FreeHostsFileRepository : HostsFileRepository {
    override fun readHosts(): String = "Hosts file editing requires Pro version."
    override fun writeHosts(content: String): Boolean = false
    override fun addBlockRule(domain: String): Boolean = false
}
