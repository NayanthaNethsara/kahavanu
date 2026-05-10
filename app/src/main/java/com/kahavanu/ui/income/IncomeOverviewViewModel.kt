package com.kahavanu.ui.income

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.ui.theme.RawColors
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isPersistent
import com.kahavanu.ui.income.components.isRecurrent
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class IncomeBreakdownItem(
    val label: String,
    val amount: Double,
    val color: Color
)

@HiltViewModel
class IncomeOverviewViewModel @Inject constructor(
    repository: IncomeRepository,
) : ViewModel() {
    val incomeLogs: StateFlow<List<IncomeLogEntry>> = repository.observeIncomeLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val totalIncomeByCurrency: StateFlow<Map<String, Double>> = incomeLogs
        .map { logs ->
            val month = YearMonth.now()
            val primary = primaryCurrency.value
            val initial = mapOf(primary to 0.0)
            initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
                .groupBy { it.currency }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val totalReceivedByCurrency: StateFlow<Map<String, Double>> = incomeLogs
        .map { logs ->
            val month = YearMonth.now()
            val primary = primaryCurrency.value
            val initial = mapOf(primary to 0.0)
            initial + logs.filter { isInMonth(it.receivedAtEpochMillis, month) && !isPending(it.sourceType) }
                .groupBy { it.currency }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
        }
        .stateIn(
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

    val breakdownsByCurrency: StateFlow<Map<String, List<IncomeBreakdownItem>>> = incomeLogs
        .map { logs ->
            val month = YearMonth.now()
            val monthLogs = logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
            val primary = primaryCurrency.value
            
            val currencyGroups = monthLogs.groupBy { it.currency }.toMutableMap()
            if (!currencyGroups.containsKey(primary)) {
                currencyGroups[primary] = emptyList()
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
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val primaryCurrency: StateFlow<String> = repository.observeCurrencySettings()
        .map { it.first.code }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "LKR",
        )

    private fun isInMonth(epochMillis: Long, month: YearMonth): Boolean {
        val date = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.year == month.year && date.month == month.month
    }
}
