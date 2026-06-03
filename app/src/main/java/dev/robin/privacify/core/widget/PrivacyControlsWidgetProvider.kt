package dev.robin.privacify.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import dev.robin.privacify.R
import dev.robin.privacify.core.security.PrivacyControllersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrivacyControlsWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_MIC = "dev.robin.privacify.action.TOGGLE_MIC"
        private const val ACTION_TOGGLE_CAMERA = "dev.robin.privacify.action.TOGGLE_CAMERA"
        private const val ACTION_TOGGLE_LOCATION = "dev.robin.privacify.action.TOGGLE_LOCATION"

        private const val COLOR_ACTIVE = "#94A3B8"
        private const val COLOR_BLOCKED = "#10B981"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, PrivacyControlsWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                updateWidget(context, manager, ids)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            val controller = PrivacyControllersProvider.rootPrivacyController

            val micBlocked = try { controller.micDisabled.value } catch (_: Exception) { false }
            val cameraBlocked = try { controller.cameraDisabled.value } catch (_: Exception) { false }
            val locationBlocked = try { controller.locationDisabled.value } catch (_: Exception) { false }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_privacy_controls)

                setSensorStatus(views, R.id.widget_mic_status, micBlocked)
                setSensorStatus(views, R.id.widget_camera_status, cameraBlocked)
                setSensorStatus(views, R.id.widget_location_status, locationBlocked)

                val micIntent = Intent(context, PrivacyControlsWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_MIC
                }
                val cameraIntent = Intent(context, PrivacyControlsWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_CAMERA
                }
                val locationIntent = Intent(context, PrivacyControlsWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_LOCATION
                }

                views.setOnClickPendingIntent(
                    R.id.widget_mic_section,
                    PendingIntent.getBroadcast(
                        context, 0, micIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                views.setOnClickPendingIntent(
                    R.id.widget_camera_section,
                    PendingIntent.getBroadcast(
                        context, 1, cameraIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                views.setOnClickPendingIntent(
                    R.id.widget_location_section,
                    PendingIntent.getBroadcast(
                        context, 2, locationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )

                manager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun setSensorStatus(views: RemoteViews, statusViewId: Int, blocked: Boolean) {
            views.setTextViewText(statusViewId, if (blocked) "Blocked" else "Active")
            views.setTextColor(
                statusViewId,
                if (blocked) Color.parseColor(COLOR_BLOCKED) else Color.parseColor(COLOR_ACTIVE)
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val controller = PrivacyControllersProvider.rootPrivacyController
        when (intent.action) {
            ACTION_TOGGLE_MIC -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        controller.setMicDisabled(!controller.micDisabled.value)
                        updateAllWidgets(context)
                    } catch (_: Exception) {}
                }
            }
            ACTION_TOGGLE_CAMERA -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        controller.setCameraDisabled(!controller.cameraDisabled.value)
                        updateAllWidgets(context)
                    } catch (_: Exception) {}
                }
            }
            ACTION_TOGGLE_LOCATION -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        controller.setLocationDisabled(!controller.locationDisabled.value)
                        updateAllWidgets(context)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
