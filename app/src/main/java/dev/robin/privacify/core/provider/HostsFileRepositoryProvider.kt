package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.HostsFileRepository
import dev.robin.privacify.free.root.FreeHostsFileRepository

object HostsFileRepositoryProvider {
    fun provide(): HostsFileRepository {
        return try {
            val clazz = Class.forName("dev.robin.privacify.pro.root.RealHostsFileRepository")
            clazz.getDeclaredConstructor().newInstance() as HostsFileRepository
        } catch (e: Exception) {
            FreeHostsFileRepository()
        }
    }
}
