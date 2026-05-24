package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.HostsFileRepository
import dev.robin.privacify.pro.root.RealHostsFileRepository

object HostsFileRepositoryProvider {
    fun provide(): HostsFileRepository {
        return RealHostsFileRepository()
    }
}
