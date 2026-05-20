package com.kahavanu.ui.sieve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.SmsSender
import com.kahavanu.domain.repository.SmsSenderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsSenderSettingsViewModel @Inject constructor(
    private val smsSenderRepository: SmsSenderRepository,
) : ViewModel() {

    val senders: StateFlow<List<SmsSender>> = smsSenderRepository
        .observeAuthorizedSenders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addSender(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            smsSenderRepository.addAuthorizedSender(name.trim())
        }
    }

    fun toggleSender(senderId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            smsSenderRepository.toggleSenderEnabled(senderId, isEnabled)
        }
    }

    fun deleteSender(senderId: String) {
        viewModelScope.launch {
            smsSenderRepository.deleteAuthorizedSender(senderId)
        }
    }
}
