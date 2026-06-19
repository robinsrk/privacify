package dev.robin.privacify.data.sensorlog

data class SensorEvent(
    val type: String,
    val action: String,
    val timestamp: Long,
    val appPackage: String?
) {
    val isStart: Boolean get() = action == ACTION_STARTED
    val isStop: Boolean get() = action == ACTION_STOPPED

    companion object {
        const val TYPE_MIC = "MIC"
        const val TYPE_CAMERA = "CAMERA"
        const val TYPE_LOCATION = "LOCATION"
        const val ACTION_STARTED = "STARTED"
        const val ACTION_STOPPED = "STOPPED"
    }
}
