package dev.robin.privacify.core.provider

import dev.robin.privacify.core.PrivacyController
import dev.robin.privacify.free.FreePrivacyController

object PrivacyControllerProvider {

    fun provide(): PrivacyController {
        return try {

            val clazz = Class.forName(
                "dev.robin.privacify.pro.RealPrivacyController"
            )

            clazz.getDeclaredConstructor().newInstance() as PrivacyController

        } catch (e: Exception) {

            FreePrivacyController()

        }
    }

}
