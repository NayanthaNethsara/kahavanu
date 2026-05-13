package com.kahavanu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.UserSession
import com.kahavanu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = authRepository.authState
        .map { user -> ProfileUiState(user = user) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(user = authRepository.currentSession)
        )

    private val _internalState = MutableStateFlow(ProfileUiState())
    // Note: To combine internal state (loading, error, dialogs) with the auth state, 
    // we could use combine(), but for simplicity let's manage internal state separately 
    // or just use a single MutableStateFlow that we update.
    
    // Actually, let's use a single MutableStateFlow for all UI state to follow the project requirements more strictly.
    private val _state = MutableStateFlow(ProfileUiState(user = authRepository.currentSession))
    val state: StateFlow<ProfileUiState> = _state

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _state.update { it.copy(user = user) }
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
            _state.update { it.copy(isLoading = true, error = null, showDeleteConfirmation = false) }
            val result = authRepository.deleteAccount()
            _state.update { 
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                ) 
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
