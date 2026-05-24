package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.RootManager
import dev.robin.privacify.pro.root.RealRootManager

object RootManagerProvider {
    fun provide(): RootManager {
        return RealRootManager()
    }
}
