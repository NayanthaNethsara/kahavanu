package com.kahavanu.ui.income

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.theme.RawColors
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isPersistent
import com.kahavanu.ui.income.components.isRecurrent
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class IncomeBreakdownItem(
    val label: String,
    val amount: Double,
    val color: Color
)

@HiltViewModel
class IncomeOverviewViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val incomeLogs: StateFlow<List<IncomeLogEntry>> = incomeRepository.observeIncomeLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val scheduledIncomes: StateFlow<List<com.kahavanu.domain.model.ScheduledIncome>> = incomeRepository.observeScheduledIncomes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val currencySettings: StateFlow<Pair<String, String>> = settingsRepository.observeCurrencySettings()
        .map { it.first.code to it.second.code }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "" to "",
        )

    val primaryCurrency: StateFlow<String> = currencySettings
        .map { it.first }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )

    fun markAsReceived(id: Long) {
        viewModelScope.launch {
            incomeRepository.markScheduledAsReceived(id)
        }
    }

    fun disableScheduled(id: Long) {
        viewModelScope.launch {
            incomeRepository.deleteScheduledIncome(id)
        }
    }

    val totalIncomeByCurrency: StateFlow<Map<String, Double>> = combine(incomeLogs, currencySettings) { logs, settings ->
        val month = YearMonth.now()
        val (primary, secondary) = settings
        val initial = mutableMapOf<String, Double>()
        if (primary.isNotBlank()) initial[primary] = 0.0
        if (secondary.isNotBlank() && secondary != primary) initial[secondary] = 0.0
        
        initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
            .groupBy { it.currency }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    val totalReceivedByCurrency: StateFlow<Map<String, Double>> = combine(incomeLogs, currencySettings) { logs, settings ->
        val month = YearMonth.now()
        val (primary, secondary) = settings
        val initial = mutableMapOf<String, Double>()
        if (primary.isNotBlank()) initial[primary] = 0.0
        if (secondary.isNotBlank() && secondary != primary) initial[secondary] = 0.0
        
        initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) && !isPending(it.sourceType) }
            .groupBy { it.currency }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    val pendingLogs: StateFlow<List<IncomeLogEntry>> = incomeLogs
        .map { logs ->
            logs.filter { isPersistent(it.sourceType) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val breakdownsByCurrency: StateFlow<Map<String, List<IncomeBreakdownItem>>> = combine(incomeLogs, currencySettings) { logs, settings ->
        val month = YearMonth.now()
        val monthLogs = logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
        val (primary, secondary) = settings
        
        val currencyGroups = monthLogs.groupBy { it.currency }.toMutableMap()
        if (primary.isNotBlank() && !currencyGroups.containsKey(primary)) currencyGroups[primary] = emptyList()
        if (secondary.isNotBlank() && primary != secondary && !currencyGroups.containsKey(secondary)) {
            currencyGroups[secondary] = emptyList()
        }

        currencyGroups.mapValues { (currency, items) ->
            val recurrentTotal = items.filter { 
                isRecurrent(it.sourceType) ||
                it.title.contains("Salary", true) || 
                it.title.contains("Retainer", true) || 
                it.title.contains("Subscription", true)
            }.sumOf { it.amount }
            
            val otherTotal = items.sumOf { it.amount } - recurrentTotal

            listOf(
                IncomeBreakdownItem("Main Recurrent", recurrentTotal, RawColors.Emerald.Emerald600),
                IncomeBreakdownItem("Freelance / Other", otherTotal, RawColors.Slate.Slate600)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )


    private fun isInMonth(epochMillis: Long, month: YearMonth): Boolean {
        val date = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.year == month.year && date.month == month.month
    }
}
