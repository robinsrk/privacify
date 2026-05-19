package dev.robin.privacify.free

import dev.robin.privacify.core.TrackerBlockController

class FreeTrackerBlockController : TrackerBlockController {
    override fun blockTrackers(enabled: Boolean) {
        // free implementation: limited tracker blocking
    }
}
