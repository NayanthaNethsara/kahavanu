package com.kahavanu.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.UserSession
import com.kahavanu.domain.repository.AuthRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val userSession: StateFlow<UserSession?> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentSession
        )

    val isAutoMatchDepositsEnabled: StateFlow<Boolean> = settingsRepository.observeAutoMatchDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPushAlertsEnabled: StateFlow<Boolean> = settingsRepository.observePushAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    fun toggleAutoMatchDeposits() {
        viewModelScope.launch {
            settingsRepository.updateAutoMatchDeposits(!isAutoMatchDepositsEnabled.value)
        }
    }

    fun togglePushAlerts() {
        viewModelScope.launch {
            settingsRepository.updatePushAlerts(!isPushAlertsEnabled.value)
        }
    }

    fun toggleDarkMode() {
        _isDarkModeEnabled.value = !_isDarkModeEnabled.value
    }

    fun logout() {
        authRepository.signOut()
    }
}
