package dev.robin.privacify.data.firewall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import dev.robin.privacify.core.security.PrivacyControllersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PrivacifyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }

        startForegroundWithNotification()
        startVpn()
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val channelId = "privacify_vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Privacify Firewall",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN firewall status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setContentTitle("Privacify Firewall Active")
            .setContentText("Protecting your network traffic")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startVpn() {
        serviceScope.launch {
            val firewall = PrivacyControllersProvider.firewallManager
            kotlinx.coroutines.flow.combine(
                firewall.enabled,
                firewall.blockedApps,
                firewall.blockData,
                firewall.systemBlocking
            ) { enabled, blocked, blockData, systemBlocking ->
                if (!enabled) null
                else VpnConfig(blocked, blockData, systemBlocking)
            }.collectLatest { config ->
                if (config == null) {
                    stopVpn()
                } else {
                    setupVpnInterface(config)
                }
            }
        }
    }

    private data class VpnConfig(
        val blockedApps: Set<String>,
        val blockData: Boolean,
        val systemBlocking: Boolean
    )

    private fun setupVpnInterface(config: VpnConfig) {
        vpnInterface?.close()
        
        val builder = Builder()
            .setSession("Privacify Firewall")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .addDnsServer("8.8.4.4")
            .setMtu(1500)

        if (config.blockData) {
            // Block everything except Privacify itself
            try {
                val pm = packageManager
                pm.getInstalledApplications(0).forEach { app ->
                    if (app.packageName != packageName) {
                        builder.addAllowedApplication(app.packageName)
                    }
                }
            } catch (e: Exception) {}
        } else {
            // Block specific apps
            config.blockedApps.forEach { pkg ->
                try { builder.addAllowedApplication(pkg) } catch (e: Exception) {}
            }
            
            if (config.systemBlocking) {
                // Block all system apps
                try {
                    val pm = packageManager
                    pm.getInstalledApplications(0).forEach { app ->
                        val isSystem = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                        if (isSystem && app.packageName != packageName) {
                            builder.addAllowedApplication(app.packageName)
                        }
                    }
                } catch (e: Exception) {}
            }
        }

        try {
            vpnInterface = builder.establish()
        } catch (e: Exception) {
            stopVpn()
        }
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        vpnInterface?.close()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "dev.robin.privacify.STOP_VPN"
        private const val NOTIFICATION_ID = 1001
    }
}
