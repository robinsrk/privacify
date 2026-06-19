package dev.robin.privacify.presentation.sensorlog

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.robin.privacify.core.provider.ProFeature
import dev.robin.privacify.data.sensorlog.SensorEvent
import dev.robin.privacify.ui.theme.AmberVibrant
import dev.robin.privacify.ui.theme.GreenVibrant
import dev.robin.privacify.ui.theme.OrangeVibrant
import dev.robin.privacify.ui.theme.RedVibrant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SensorLogScreen(
    onBack: () -> Unit
) {
    val isPro = ProFeature.isAutoGuardAvailable()

    if (!isPro) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TopBar(onBack = onBack, showClear = false, onClear = {})
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sensor Usage History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upgrade to Pro to view sensor usage history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val viewModel: SensorLogViewModel = viewModel(factory = SensorLogViewModel.factory(context))
    val events by viewModel.events.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                onBack = onBack,
                showClear = events.isNotEmpty(),
                onClear = { viewModel.clearEvents() }
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No sensor usage recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sensor usage logs will appear here\nafter Auto-Guard detects activity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                val sessions = pairIntoSessions(events)
                val grouped = groupSessionsByDate(sessions)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (dateLabel, dateSessions) ->
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(dateSessions, key = { "${it.type}_${it.startTime}" }) { session ->
                            TimelineSessionCard(session = session)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    showClear: Boolean,
    onClear: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Sensor Usage History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        if (showClear) {
            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear history",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showConfirm) {
        ClearConfirmDialog(
            onConfirm = {
                onClear()
                showConfirm = false
            },
            onDismiss = { showConfirm = false }
        )
    }
}

@Composable
private fun ClearConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clear History?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("This will permanently delete all sensor usage logs.")
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(
                    text = "Clear",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class AppDisplayInfo(val name: String, val icon: Drawable?)

data class SensorSession(
    val type: String,
    val appPackage: String?,
    val startTime: Long,
    val stopTime: Long?,
    val startEvent: SensorEvent,
    val stopEvent: SensorEvent?
)

@Composable
private fun TimelineSessionCard(session: SensorSession) {
    val context = LocalContext.current
    val sensorIcon = sensorIcon(session.type)
    val sensorColor = sensorColor(session.type)
    val sensorLabel = sensorLabel(session.type)
    val stopTime = session.stopTime
    val durationText = formatDuration(session.startTime, stopTime)
    val hasStopped = stopTime != null

    val pkg = session.appPackage
    val appInfo = remember(pkg) {
        if (pkg.isNullOrBlank()) null
        else try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            AppDisplayInfo(
                name = pm.getApplicationLabel(ai).toString(),
                icon = pm.getApplicationIcon(ai)
            )
        } catch (_: Exception) {
            AppDisplayInfo(name = pkg, icon = null)
        }
    }
    val iconPainter = remember(appInfo) {
        appInfo?.icon?.let { drawable ->
            val w = drawable.intrinsicWidth.coerceAtLeast(1)
            val h = drawable.intrinsicHeight.coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            BitmapPainter(bitmap.asImageBitmap())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        // Header: app icon + name, sensor label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(sensorColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sensorIcon,
                    contentDescription = null,
                    tint = sensorColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (iconPainter != null) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = appInfo?.name ?: sensorLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$sensorLabel access",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!pkg.isNullOrBlank() && appInfo?.name != pkg) {
                Text(
                    text = pkg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Timeline body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // Timeline column
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Start dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GreenVibrant)
                    )
                    // Vertical line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(if (hasStopped) 48.dp else 24.dp)
                            .background(sensorColor.copy(alpha = 0.3f))
                    )
                    // Stop dot (or running indicator)
                    if (hasStopped) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(RedVibrant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(sensorColor.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content column
            Column(modifier = Modifier.weight(1f)) {
                // Started row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Started",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenVibrant
                    )
                    Text(
                        text = timeFormat.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasStopped) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Duration badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stopped row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stopped",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RedVibrant
                        )
                        Text(
                            text = timeFormat.format(Date(stopTime!!)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenVibrant,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private fun formatDuration(start: Long, stop: Long?): String {
    if (stop == null) return "Running"
    val diffMs = stop - start
    val seconds = diffMs / 1000
    if (seconds < 60) return "${seconds}s"
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    if (minutes < 60) return "${minutes}m ${remainingSeconds}s"
    val hours = minutes / 60
    val remMin = minutes % 60
    return "${hours}h ${remMin}m"
}

fun pairIntoSessions(events: List<SensorEvent>): List<SensorSession> {
    val chrono = events.reversed()
    val sessions = mutableListOf<SensorSession>()
    val pending = mutableMapOf<String, SensorEvent>()

    for (event in chrono) {
        val key = "${event.type}|${event.appPackage ?: "unknown"}"
        if (event.isStart) {
            pending[key] = event
        } else {
            val started = pending.remove(key)
            if (started != null) {
                sessions.add(
                    SensorSession(
                        type = event.type,
                        appPackage = event.appPackage,
                        startTime = started.timestamp,
                        stopTime = event.timestamp,
                        startEvent = started,
                        stopEvent = event
                    )
                )
            }
        }
    }

    for ((_, started) in pending) {
        sessions.add(
            SensorSession(
                type = started.type,
                appPackage = started.appPackage,
                startTime = started.timestamp,
                stopTime = null,
                startEvent = started,
                stopEvent = null
            )
        )
    }

    return sessions.sortedByDescending { it.startTime }
}

private fun sensorIcon(type: String): ImageVector = when (type) {
    SensorEvent.TYPE_MIC -> Icons.Outlined.MicOff
    SensorEvent.TYPE_CAMERA -> Icons.Outlined.CameraAlt
    SensorEvent.TYPE_LOCATION -> Icons.Outlined.LocationOn
    else -> Icons.Outlined.History
}

@Composable
private fun sensorColor(type: String): Color = when (type) {
    SensorEvent.TYPE_MIC -> RedVibrant
    SensorEvent.TYPE_CAMERA -> OrangeVibrant
    SensorEvent.TYPE_LOCATION -> AmberVibrant
    else -> MaterialTheme.colorScheme.primary
}

private fun sensorLabel(type: String): String = when (type) {
    SensorEvent.TYPE_MIC -> "Mic"
    SensorEvent.TYPE_CAMERA -> "Camera"
    SensorEvent.TYPE_LOCATION -> "Location"
    else -> type
}

private data class DateGroup(
    val label: String,
    val sessions: List<SensorSession>
)

private fun groupSessionsByDate(sessions: List<SensorSession>): List<DateGroup> {
    if (sessions.isEmpty()) return emptyList()

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - 86_400_000L

    val groups = mutableListOf<DateGroup>()
    var currentLabel: String? = null
    var currentSessions = mutableListOf<SensorSession>()

    for (session in sessions) {
        val label = when {
            session.startTime >= todayStart -> "Today"
            session.startTime >= yesterdayStart -> "Yesterday"
            else -> dateGroupFormat.format(Date(session.startTime))
        }
        if (currentLabel != label) {
            if (currentLabel != null) {
                groups.add(DateGroup(currentLabel, currentSessions.toList()))
            }
            currentLabel = label
            currentSessions = mutableListOf()
        }
        currentSessions.add(session)
    }
    if (currentLabel != null) {
        groups.add(DateGroup(currentLabel, currentSessions.toList()))
    }

    return groups
}

private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dateGroupFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
