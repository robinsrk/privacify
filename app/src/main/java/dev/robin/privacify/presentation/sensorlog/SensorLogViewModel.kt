package dev.robin.privacify.presentation.sensorlog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.robin.privacify.data.sensorlog.SensorEvent
import dev.robin.privacify.data.sensorlog.SensorLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SensorLogViewModel(
    private val repository: SensorLogRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<SensorEvent>>(emptyList())
    val events: StateFlow<List<SensorEvent>> = _events

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.getEvents()
        }
    }

    fun clearEvents() {
        viewModelScope.launch {
            repository.clearEvents()
            _events.value = emptyList()
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SensorLogViewModel(
                        repository = SensorLogRepository(context.applicationContext)
                    ) as T
                }
            }
        }
    }
}
