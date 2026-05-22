package com.kahavanu.ui.income

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import com.kahavanu.ui.common.MatchItemState
import com.kahavanu.ui.home.inferExpenseCategory
import com.kahavanu.ui.theme.OnSurfaceVariant
import com.kahavanu.ui.theme.Primary
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
    private val smsSuggestionRepository: SmsSuggestionRepository,
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
            initialValue = "LKR" to "USD",
        )

    val primaryCurrency: StateFlow<String> = currencySettings
        .map { it.first }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "LKR",
        )

    val incomeSuggestions: StateFlow<List<MatchItemState>> =
        smsSuggestionRepository.observePendingByKinds(
            listOf(SuggestionKind.INCOME, SuggestionKind.SETTLE_PENDING)
        ).map { list -> list.map { it.toMatchItemState() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun confirmIncomeSuggestion(id: String) {
        viewModelScope.launch {
            val localId = id.toLongOrNull() ?: return@launch
            val suggestion = smsSuggestionRepository.getById(localId) ?: return@launch
            when (suggestion.kind) {
                SuggestionKind.SETTLE_PENDING -> {
                    suggestion.matchedScheduledIncomeId?.let { incomeRepository.markScheduledAsReceived(it) }
                        ?: incomeRepository.logIncome(suggestion.toLogEntry())
                }
                else -> incomeRepository.logIncome(suggestion.toLogEntry())
            }
            smsSuggestionRepository.confirm(localId)
        }
    }

    fun dismissIncomeSuggestion(id: String) {
        viewModelScope.launch {
            smsSuggestionRepository.dismiss(id.toLongOrNull() ?: return@launch)
        }
    }

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
        
        initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) && it.currency.isNotBlank() }
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
        
        initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) && !isPending(it.sourceType) && it.currency.isNotBlank() }
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
        
        val currencyGroups = monthLogs.filter { it.currency.isNotBlank() }.groupBy { it.currency }.toMutableMap()
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
                IncomeBreakdownItem("Main Recurrent", recurrentTotal, Primary),
                IncomeBreakdownItem("Freelance / Other", otherTotal, OnSurfaceVariant)
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

private fun SmsSuggestion.toMatchItemState(): MatchItemState {
    val isSettle = kind == SuggestionKind.SETTLE_PENDING
    return MatchItemState(
        id = localId.toString(),
        title = smsSenderName,
        subtitle = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            .format(java.util.Date(smsReceivedAtEpochMillis)),
        amount = "$currency ${String.format("%,.0f", amount)}",
        matchPercent = (confidence * 100).toInt(),
        likelyFor = if (isSettle) "Settles a pending income" else title,
        primaryActionLabel = if (isSettle) "Settle pending" else "Confirm income",
        secondaryActionLabel = "Log as new",
    )
}

private fun SmsSuggestion.toLogEntry() = IncomeLogEntry(
    title = title,
    amount = amount,
    currency = currency,
    receivedAtEpochMillis = txnAtEpochMillis,
    sourceName = merchant ?: smsSenderName,
    sourceType = "sms",
)
