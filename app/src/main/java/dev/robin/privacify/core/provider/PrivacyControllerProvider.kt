package dev.robin.privacify.core.provider

import dev.robin.privacify.core.PrivacyController
import dev.robin.privacify.pro.RealPrivacyController

object PrivacyControllerProvider {

    fun provide(): PrivacyController {
        return RealPrivacyController()
    }

}
