package fr.labomg.biophonie.feature.firstlaunch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.labomg.biophonie.core.network.ConflictThrowable
import fr.labomg.biophonie.core.network.InternalErrorThrowable
import fr.labomg.biophonie.core.network.NoConnectionThrowable
import fr.labomg.biophonie.data.user.source.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TutorialViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {

    var name by mutableStateOf("")
        private set

    private var _uiState = MutableStateFlow(NameUiState())
    val uiState: StateFlow<NameUiState> = _uiState.asStateFlow()

    private var _shouldStartMapExploration = MutableStateFlow(false)
    val shouldStartMapExploration: StateFlow<Boolean> = _shouldStartMapExploration.asStateFlow()

    private val pattern = Regex("[a-zA-z\\s-]*")

    fun updateName(enteredName: String) {
        name = enteredName
        if (enteredName.matches(pattern)) {
            _uiState.updateError()
        } else {
            _uiState.updateError(NameError.SPECIAL_CHAR)
        }
    }

    private fun MutableStateFlow<NameUiState>.updateError(error: NameError? = null) {
        if (error == null)
            this.update { it.copy(isNameInvalid = false, supportingText = NameError.EMPTY) }
        else this.update { it.copy(isNameInvalid = true, supportingText = error) }
    }

    fun submit() {
        when {
            name.isEmpty() -> _uiState.updateError(NameError.NOT_NULL)
            name.length <= MINIMUM_NAME_LENGTH -> _uiState.updateError(NameError.TOO_SHORT)
            else ->
                viewModelScope.launch {
                    userRepository
                        .addUser(name)
                        .onSuccess { _shouldStartMapExploration.value = true }
                        .onFailure { throwable -> _uiState.updateError(throwable.toNameError()) }
                }
        }
    }

    private fun Throwable.toNameError(): NameError {
        return when (this) {
            is NoConnectionThrowable -> NameError.NETWORK_UNREACHABLE
            is ConflictThrowable -> NameError.NOT_UNIQUE
            is InternalErrorThrowable -> NameError.SERVER_ERROR
            else -> NameError.UNKNOWN
        }
    }

    companion object {
        private const val MINIMUM_NAME_LENGTH = 2
    }
}
