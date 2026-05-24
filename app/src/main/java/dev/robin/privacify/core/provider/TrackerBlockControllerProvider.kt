package dev.robin.privacify.core.provider

import dev.robin.privacify.core.TrackerBlockController
import dev.robin.privacify.pro.RealTrackerBlockController

object TrackerBlockControllerProvider {

    fun provide(): TrackerBlockController {
        return RealTrackerBlockController()
    }

}
