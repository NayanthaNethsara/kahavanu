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

data class ProfileUiState(
    val user: UserSession? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(user = authRepository.currentSession))
    val state: StateFlow<ProfileUiState> = _state

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }
}
