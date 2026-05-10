package com.kahavanu.ui.income

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.ui.theme.RawColors
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kahavanu.ui.income.components.isPending
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

    val monthlyTotal: StateFlow<Double> = incomeLogs
        .map { logs ->
            val month = YearMonth.now()
            logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0,
        )

    val pendingLogs: StateFlow<List<IncomeLogEntry>> = incomeLogs
        .map { logs ->
            logs.filter { isPending(it.note) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val breakdowns: StateFlow<List<IncomeBreakdownItem>> = incomeLogs
        .map { logs ->
            val month = YearMonth.now()
            val monthLogs = logs.filter { isInMonth(it.receivedAtEpochMillis, month) }
            
            // Heuristic-based breakdown for now since logs don't have explicit types
            val recurrentTotal = monthLogs.filter { 
                it.title.contains("Salary", true) || 
                it.title.contains("Retainer", true) || 
                it.title.contains("Subscription", true)
            }.sumOf { it.amount }
            
            val otherTotal = monthLogs.sumOf { it.amount } - recurrentTotal

            listOf(
                IncomeBreakdownItem("Main Recurrent", recurrentTotal, RawColors.Emerald.Emerald600),
                IncomeBreakdownItem("Freelance / Other", otherTotal, RawColors.Slate.Slate600)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val currency: StateFlow<String> = incomeLogs
        .map { logs -> logs.firstOrNull()?.currency ?: "LKR" }
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
