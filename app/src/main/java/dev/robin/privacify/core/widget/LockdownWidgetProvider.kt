package dev.robin.privacify.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.robin.privacify.R
import dev.robin.privacify.core.security.PrivacyControllersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockdownWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_LOCKDOWN = "dev.robin.privacify.action.TOGGLE_LOCKDOWN"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, LockdownWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                updateWidget(context, manager, ids)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            val active = try {
                PrivacyControllersProvider.lockdownUseCase.isActive.value
            } catch (_: Exception) {
                false
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_lockdown)

                views.setTextViewText(
                    R.id.widget_status,
                    if (active) "Active" else "Inactive"
                )
                views.setTextColor(
                    R.id.widget_status,
                    if (active) android.graphics.Color.parseColor("#10B981")
                    else android.graphics.Color.parseColor("#94A3B8")
                )

                val toggleIntent = Intent(context, LockdownWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_LOCKDOWN
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_lockdown_container, pendingIntent)

                manager.updateAppWidget(appWidgetId, views)
            }
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
        if (ACTION_TOGGLE_LOCKDOWN == intent.action) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val useCase = PrivacyControllersProvider.lockdownUseCase
                    if (useCase.isActive.value) {
                        useCase.disableLockdown()
                    } else {
                        useCase.enableLockdown()
                    }
                    updateAllWidgets(context)
                } catch (_: Exception) {
                }
            }
        }
    }
}
