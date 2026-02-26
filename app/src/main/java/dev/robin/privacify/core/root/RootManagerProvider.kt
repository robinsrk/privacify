package dev.robin.privacify.core.root

import dev.robin.privacify.data.root.DefaultRootManager
import dev.robin.privacify.domain.root.RootManager

object RootManagerProvider {
	val instance: RootManager by lazy { DefaultRootManager() }
}

