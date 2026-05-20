package com.kahavanu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthEvent {
    data class Error(val message: String) : AuthEvent
    data object ResetPasswordEmailSent : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    val isAuthenticated: StateFlow<Boolean> = repository.authState
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.currentSession != null,
        )

    val currentUser = repository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.currentSession,
        )

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Please fill in all fields")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithEmail(email.trim(), password)
            _uiState.update { it.copy(isLoading = false) }
            
            result.onFailure { error ->
                _events.send(AuthEvent.Error(error.localizedMessage ?: "Invalid email or password"))
            }
        }
    }

    fun signup(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Please fill in all fields")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signUpWithEmail(fullName.trim(), email.trim(), password)
            _uiState.update { it.copy(isLoading = false) }

            result.onFailure { error ->
                _events.send(AuthEvent.Error(error.localizedMessage ?: "Could not create account"))
            }
        }
    }

    fun startGoogleSignIn(requestIdToken: suspend () -> Result<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val tokenResult = requestIdToken()
            if (tokenResult.isFailure) {
                val message = tokenResult.exceptionOrNull()?.message
                    ?: "Google sign-in failed. Please try again."
                _uiState.update { it.copy(isLoading = false) }
                _events.send(AuthEvent.Error(message))
                return@launch
            }

            val result = repository.signInWithGoogleIdToken(tokenResult.getOrThrow())
            _uiState.update { it.copy(isLoading = false) }
            
            result.onFailure { error ->
                _events.send(AuthEvent.Error(error.localizedMessage ?: "Google sign-in failed"))
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Please enter your email")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.sendPasswordReset(email.trim())
            _uiState.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _events.send(AuthEvent.ResetPasswordEmailSent)
            }.onFailure { error ->
                _events.send(AuthEvent.Error(error.localizedMessage ?: "Could not send reset email"))
            }
        }
    }

    fun setError(message: String?) {
        message?.let {
            viewModelScope.launch { _events.send(AuthEvent.Error(it)) }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.update { it.copy(errorMessage = null, isLoading = false) }
    }
}
