package dev.robin.privacify.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.robin.privacify.data.root.HostsFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HostsEditorUiState(
	val content: String = "",
	val isLoading: Boolean = true,
	val newDomain: String = "",
	val statusMessage: String? = null
)

class HostsEditorViewModel : ViewModel() {

	private val _state = MutableStateFlow(HostsEditorUiState())
	val state: StateFlow<HostsEditorUiState> = _state

	init {
		loadHosts()
	}

	private fun loadHosts() {
		viewModelScope.launch(Dispatchers.IO) {
			val content = HostsFileManager.readHosts()
			_state.update { it.copy(content = content, isLoading = false) }
		}
	}

	fun onContentChanged(content: String) {
		_state.update { it.copy(content = content) }
	}

	fun onNewDomainChanged(domain: String) {
		_state.update { it.copy(newDomain = domain) }
	}

	fun addBlockRule() {
		val domain = _state.value.newDomain.trim()
		if (domain.isBlank()) return
		viewModelScope.launch(Dispatchers.IO) {
			val success = HostsFileManager.addBlockRule(domain)
			withContext(Dispatchers.Main) {
				_state.update {
					it.copy(
						newDomain = "",
						statusMessage = if (success) "Blocked $domain" else "Failed to add rule"
					)
				}
			}
			// Reload
			val content = HostsFileManager.readHosts()
			_state.update { it.copy(content = content) }
		}
	}

	fun saveHosts() {
		viewModelScope.launch(Dispatchers.IO) {
			val success = HostsFileManager.writeHosts(_state.value.content)
			withContext(Dispatchers.Main) {
				_state.update {
					it.copy(
						statusMessage = if (success) "Hosts file saved" else "Failed to save hosts file"
					)
				}
			}
		}
	}

	fun clearStatusMessage() {
		_state.update { it.copy(statusMessage = null) }
	}

	companion object {
		val Factory: ViewModelProvider.Factory = viewModelFactory {
			initializer {
				HostsEditorViewModel()
			}
		}
	}
}
