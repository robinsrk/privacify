package dev.robin.privacify.presentation.lockdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.core.utils.AppContextProvider
import dev.robin.privacify.domain.lockdown.LockdownUseCase
import dev.robin.privacify.domain.root.RootPrivacyController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockdownUiState(
	val lockdownActive: Boolean = false,
	val micKilled: Boolean = false,
	val cameraKilled: Boolean = false
)

class LockdownViewModel(
	private val lockdownUseCase: LockdownUseCase,
	private val rootPrivacyController: RootPrivacyController
) : ViewModel() {

	private val _state = MutableStateFlow(LockdownUiState())
	val state: StateFlow<LockdownUiState> = _state

	init {
		viewModelScope.launch {
			rootPrivacyController.micDisabled.collectLatest { disabled ->
				_state.update { it.copy(micKilled = disabled) }
			}
		}
		viewModelScope.launch {
			rootPrivacyController.cameraDisabled.collectLatest { disabled ->
				_state.update { it.copy(cameraKilled = disabled) }
			}
		}
	}

	fun toggleLockdown() {
		viewModelScope.launch {
			val newActive = !_state.value.lockdownActive
			_state.update { it.copy(lockdownActive = newActive) }
			if (newActive) {
				lockdownUseCase.enableLockdown()
			} else {
				lockdownUseCase.disableLockdown()
			}
			dev.robin.privacify.core.widget.LockdownWidgetProvider.updateAllWidgets(AppContextProvider.context)
		}
	}

	fun toggleMic() {
		viewModelScope.launch {
			val newState = !_state.value.micKilled
			rootPrivacyController.setMicDisabled(newState)
		}
	}

	fun toggleCamera() {
		viewModelScope.launch {
			val newState = !_state.value.cameraKilled
			rootPrivacyController.setCameraDisabled(newState)
		}
	}

	companion object {
		val Factory: ViewModelProvider.Factory = viewModelFactory {
			initializer {
				LockdownViewModel(
					lockdownUseCase = PrivacyControllersProvider.lockdownUseCase,
					rootPrivacyController = PrivacyControllersProvider.rootPrivacyController
				)
			}
		}
	}
}
