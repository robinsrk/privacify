package dev.robin.privacify.core.provider

import dev.robin.privacify.domain.root.RootPrivacyController
import dev.robin.privacify.pro.root.RealRootPrivacyController

object RootPrivacyControllerProvider {
    fun provide(): RootPrivacyController {
        return RealRootPrivacyController()
    }
}
