package dev.robin.privacify.core.provider

import dev.robin.privacify.core.TrackerBlockController
import dev.robin.privacify.free.FreeTrackerBlockController

object TrackerBlockControllerProvider {

    fun provide(): TrackerBlockController {
        return try {

            val clazz = Class.forName(
                "dev.robin.privacify.pro.RealTrackerBlockController"
            )

            clazz.getDeclaredConstructor().newInstance() as TrackerBlockController

        } catch (e: Exception) {

            FreeTrackerBlockController()

        }
    }

}
