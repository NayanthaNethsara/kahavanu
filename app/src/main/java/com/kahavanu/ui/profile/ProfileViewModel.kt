package com.kahavanu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.UserSession
import com.kahavanu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userSession: StateFlow<UserSession?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentSession
        )

    private val _isAutoMatchDepositsEnabled = MutableStateFlow(true)
    val isAutoMatchDepositsEnabled: StateFlow<Boolean> = _isAutoMatchDepositsEnabled.asStateFlow()

    private val _isPushAlertsEnabled = MutableStateFlow(true)
    val isPushAlertsEnabled: StateFlow<Boolean> = _isPushAlertsEnabled.asStateFlow()

    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    fun toggleAutoMatchDeposits() {
        _isAutoMatchDepositsEnabled.value = !_isAutoMatchDepositsEnabled.value
    }

    fun togglePushAlerts() {
        _isPushAlertsEnabled.value = !_isPushAlertsEnabled.value
    }

    fun toggleDarkMode() {
        _isDarkModeEnabled.value = !_isDarkModeEnabled.value
    }

    fun logout() {
        authRepository.signOut()
    }
}
