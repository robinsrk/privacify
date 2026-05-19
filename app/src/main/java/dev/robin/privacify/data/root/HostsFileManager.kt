package dev.robin.privacify.data.root

import dev.robin.privacify.core.provider.HostsFileRepositoryProvider

object HostsFileManager {
    private val delegate = HostsFileRepositoryProvider.provide()

    fun readHosts(): String = delegate.readHosts()

    fun writeHosts(content: String): Boolean = delegate.writeHosts(content)

    fun addBlockRule(domain: String): Boolean = delegate.addBlockRule(domain)
}
