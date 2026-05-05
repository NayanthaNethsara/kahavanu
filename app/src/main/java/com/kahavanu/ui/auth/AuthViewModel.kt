package com.kahavanu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kahavanu.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    val isAuthenticated: StateFlow<Boolean> = repository.authState
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repository.currentSession != null,
        )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithEmail(email.trim(), password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
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
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithGoogleIdToken(idToken)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
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
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModelFactory(
    private val repository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
