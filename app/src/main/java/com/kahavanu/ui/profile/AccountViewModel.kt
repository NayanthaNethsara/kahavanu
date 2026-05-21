package com.kahavanu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.UserSession
import com.kahavanu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val user: UserSession? = null,
    val displayNameInput: String = "",
    val isDisplayNameDirty: Boolean = false,
    val isSavingName: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountUiState(
            user = authRepository.currentSession,
            displayNameInput = authRepository.currentSession?.displayName.orEmpty(),
        )
    )
    val state: StateFlow<AccountUiState> = _state

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _state.update { current ->
                    val syncedName = user?.displayName.orEmpty()
                    current.copy(
                        user = user,
                        displayNameInput = if (current.isDisplayNameDirty) {
                            current.displayNameInput
                        } else {
                            syncedName
                        },
                        isDisplayNameDirty = if (current.isDisplayNameDirty) {
                            current.displayNameInput.trim() != syncedName.trim()
                        } else {
                            false
                        },
                    )
                }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _state.update { current ->
            val savedName = current.user?.displayName.orEmpty()
            current.copy(
                displayNameInput = value,
                isDisplayNameDirty = value.trim() != savedName.trim(),
                error = null,
            )
        }
    }

    fun saveDisplayName() {
        val trimmedName = _state.value.displayNameInput.trim()
        if (trimmedName.isBlank() || !_state.value.isDisplayNameDirty) return

        viewModelScope.launch {
            _state.update { it.copy(isSavingName = true, error = null) }
            val result = authRepository.updateDisplayName(trimmedName)
            _state.update { current ->
                current.copy(
                    isSavingName = false,
                    isDisplayNameDirty = result.isFailure,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun showDeleteConfirmation(show: Boolean) {
        _state.update { it.copy(showDeleteConfirmation = show) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, showDeleteConfirmation = false)
            }
            val result = authRepository.deleteAccount()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }
}
