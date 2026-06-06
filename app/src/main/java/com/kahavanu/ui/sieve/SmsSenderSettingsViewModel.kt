package com.kahavanu.ui.sieve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.SmsSender
import com.kahavanu.domain.repository.SmsSenderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsSenderSettingsViewModel @Inject constructor(
    private val smsSenderRepository: SmsSenderRepository,
) : ViewModel() {

    private val senders: StateFlow<List<SmsSender>> = smsSenderRepository
        .observeAuthorizedSenders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSenders: StateFlow<List<SmsSender>> = combine(senders, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.senderName.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addSender(name: String, subtitle: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            smsSenderRepository.addAuthorizedSender(name.trim(), subtitle.trim())
        }
    }

    fun toggleSender(senderId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            smsSenderRepository.toggleSenderEnabled(senderId, isEnabled)
        }
    }

    fun updateSender(senderId: String, newName: String, newSubtitle: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            smsSenderRepository.updateAuthorizedSender(senderId, newName.trim(), newSubtitle.trim())
        }
    }
}
