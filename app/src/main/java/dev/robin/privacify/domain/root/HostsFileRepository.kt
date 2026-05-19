package dev.robin.privacify.domain.root

interface HostsFileRepository {
    fun readHosts(): String
    fun writeHosts(content: String): Boolean
    fun addBlockRule(domain: String): Boolean
}
