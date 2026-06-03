package com.kahavanu.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.AppNotification
import com.kahavanu.domain.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = repository.observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = repository.observeUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        // Drop read notifications past their retention window on each app session.
        viewModelScope.launch { repository.purgeExpired() }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead() }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
