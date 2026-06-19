package dev.robin.privacify.data.sensorlog

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SensorLogRepository(private val context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val lock = Any()

    fun addEvent(event: SensorEvent) {
        synchronized(lock) {
            val events = readEventsInternal().toMutableList()
            events.add(0, event)
            while (events.size > MAX_EVENTS) {
                events.removeAt(events.lastIndex)
            }
            writeEventsInternal(events)
        }
    }

    fun getEvents(): List<SensorEvent> {
        synchronized(lock) {
            return readEventsInternal()
        }
    }

    fun clearEvents() {
        synchronized(lock) {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun readEventsInternal(): List<SensorEvent> {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            if (text.isBlank()) return emptyList()
            val array = JSONArray(text)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                SensorEvent(
                    type = obj.getString("type"),
                    action = obj.getString("action"),
                    timestamp = obj.getLong("timestamp"),
                    appPackage = obj.optString("app").takeIf { it.isNotBlank() }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeEventsInternal(events: List<SensorEvent>) {
        try {
            val array = JSONArray()
            events.forEach { event ->
                val obj = JSONObject().apply {
                    put("type", event.type)
                    put("action", event.action)
                    put("timestamp", event.timestamp)
                    put("app", event.appPackage ?: "")
                }
                array.put(obj)
            }
            file.writeText(array.toString())
        } catch (e: Exception) {
            // silently fail
        }
    }

    companion object {
        private const val FILE_NAME = "sensor_log.json"
        private const val MAX_EVENTS = 200
    }
}
