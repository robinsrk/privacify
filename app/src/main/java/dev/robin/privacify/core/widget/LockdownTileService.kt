package dev.robin.privacify.core.widget

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import dev.robin.privacify.core.security.PrivacyControllersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LockdownTileService : TileService() {

    companion object {
        private const val TAG = "LockdownTile"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            try {
                val useCase = PrivacyControllersProvider.lockdownUseCase
                val active = useCase.isActive.value
                if (active) {
                    useCase.disableLockdown()
                    Log.d(TAG, "Lockdown disabled via tile")
                } else {
                    useCase.enableLockdown()
                    Log.d(TAG, "Lockdown enabled via tile")
                }
                updateTile()
                LockdownWidgetProvider.updateAllWidgets(this@LockdownTileService)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle lockdown from tile", e)
            }
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        try {
            val active = PrivacyControllersProvider.lockdownUseCase.isActive.value
            tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = getString(dev.robin.privacify.R.string.tile_lockdown_label)
            tile.contentDescription = if (active) "Lockdown active" else "Lockdown inactive"
            tile.updateTile()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update tile", e)
        }
    }
}
