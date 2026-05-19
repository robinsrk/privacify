package dev.robin.privacify.core.root

import dev.robin.privacify.core.provider.RootManagerProvider
import dev.robin.privacify.domain.root.RootManager

object RootManagerProvider {
	val instance: RootManager by lazy { dev.robin.privacify.core.provider.RootManagerProvider.provide() }
}
