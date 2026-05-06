package com.kahavanu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithEmail(email.trim(), password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = if (result.isFailure) "Invalid email or password" else null,
                )
            }
        }
    }

    fun signup(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signUpWithEmail(fullName.trim(), email.trim(), password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = if (result.isFailure) "Could not create account. Please try again." else null,
                )
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
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                return@launch
            }

            val result = repository.signInWithGoogleIdToken(tokenResult.getOrThrow())
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = if (result.isFailure) "Google sign-in failed. Please try again." else null,
                )
            }
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPasswordReset(email.trim())
            result.onSuccess { onSuccess() }
                .onFailure { onFailure(it.localizedMessage ?: "Could not send reset email") }
        }
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun signOut() {
        repository.signOut()
        _uiState.update { it.copy(errorMessage = null, isLoading = false) }
    }
}
