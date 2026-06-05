package com.kahavanu.ui.income

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import com.kahavanu.ui.common.HistoryDateRange
import com.kahavanu.ui.common.MatchItemState
import com.kahavanu.ui.common.contains
import com.kahavanu.ui.home.inferExpenseCategory
import com.kahavanu.ui.theme.OnSurfaceVariant
import com.kahavanu.ui.util.dailyTotals
import com.kahavanu.ui.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kahavanu.ui.income.components.isOverdue
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isPersistent
import com.kahavanu.ui.income.components.isRecurrent
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class IncomeBreakdownItem(
    val label: String,
    val amount: Double,
    val color: Color
)

data class IncomeFilters(
    val status: HistoryFilter = HistoryFilter.ALL,
    val dateRange: HistoryDateRange = HistoryDateRange.ALL,
    val minAmount: String = "",
    val maxAmount: String = "",
) {
    val isActive: Boolean
        get() = status != HistoryFilter.ALL ||
            dateRange != HistoryDateRange.ALL ||
            minAmount.isNotBlank() ||
            maxAmount.isNotBlank()
}

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

    // Daily received-income totals for the last 7 days (oldest first) for the line chart.
    val incomeTrend: StateFlow<List<Float>> = incomeRepository.observeIncomeLogs()
        .map { logs ->
            dailyTotals(
                logs.filter { !isPending(it.sourceType) }
                    .map { it.receivedAtEpochMillis to it.amount }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // Income received within the last 7 days ("this week").
    val thisWeekIncome: StateFlow<Double> = incomeRepository.observeIncomeLogs()
        .map { logs -> receivedWithinDays(logs, 7) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0,
        )

    // Average weekly income over the trailing 4 weeks.
    val weeklyAverageIncome: StateFlow<Double> = incomeRepository.observeIncomeLogs()
        .map { logs -> receivedWithinDays(logs, 28) / 4.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0,
        )

    private fun receivedWithinDays(logs: List<IncomeLogEntry>, days: Int): Double {
        val cutoff = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        return logs
            .filter { !isPending(it.sourceType) && it.receivedAtEpochMillis >= cutoff }
            .sumOf { it.amount }
    }

    val scheduledIncomes: StateFlow<List<com.kahavanu.domain.model.ScheduledIncome>> = incomeRepository.observeScheduledIncomes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(IncomeFilters())
    val filters: StateFlow<IncomeFilters> = _filters.asStateFlow()

    // Search/filter results for the income history screen, sorted newest-first.
    val historyItems: StateFlow<List<HistoryItem>> =
        combine(incomeLogs, scheduledIncomes, _filters, _searchQuery) { logs, scheduled, filters, query ->
            val logItems = logs.map { HistoryItem.Log(it) }
            val scheduledItems = scheduled
                .filter { it.type == IncomeSourceType.PENDING && it.lastGeneratedEpochMillis == null }
                .map { HistoryItem.Scheduled(it) }

            val minAmt = filters.minAmount.toDoubleOrNull()
            val maxAmt = filters.maxAmount.toDoubleOrNull()

            (logItems + scheduledItems).filter { item ->
                val matchesSearch = item.title.contains(query, ignoreCase = true) ||
                    item.amount.toString().contains(query)

                val matchesStatus = when (filters.status) {
                    HistoryFilter.ALL -> true
                    HistoryFilter.PENDING -> item is HistoryItem.Scheduled && item.scheduled.type == IncomeSourceType.PENDING
                    HistoryFilter.OVERDUE -> item is HistoryItem.Scheduled && isOverdue(item.scheduled.scheduledDateEpochMillis)
                    HistoryFilter.PAID -> item is HistoryItem.Log
                    HistoryFilter.RECURRENT -> item is HistoryItem.Scheduled && item.scheduled.type == IncomeSourceType.RECURRENT
                }

                val matchesDate = filters.dateRange.contains(item.timestamp)
                val matchesMin = minAmt == null || item.amount >= minAmt
                val matchesMax = maxAmt == null || item.amount <= maxAmt

                matchesSearch && matchesStatus && matchesDate && matchesMin && matchesMax
            }.sortedByDescending { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private var hasAppliedInitialFilter = false

    // Seeds the status filter from a navigation argument once, so later user edits aren't overwritten on recomposition.
    fun applyInitialStatusFilter(status: HistoryFilter) {
        if (hasAppliedInitialFilter) return
        hasAppliedInitialFilter = true
        _filters.value = _filters.value.copy(status = status)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(filters: IncomeFilters) {
        _filters.value = filters
    }

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
