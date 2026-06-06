package com.kahavanu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SubscriptionsRepository
import com.kahavanu.ui.util.CurrencyConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

private const val ACTIVE_GOAL_SOFT_LIMIT = 5

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val settingsRepository: SettingsRepository,
    private val incomeRepository: IncomeRepository,
    private val expensesRepository: ExpensesRepository,
    private val subscriptionsRepository: SubscriptionsRepository,
) : ViewModel() {

    private val sortMode = MutableStateFlow(GoalSortMode.BY_PRIORITY)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalsRepository.observeGoals(),
        settingsRepository.observeCurrencySettings(),
        sortMode,
        incomeRepository.observeIncomeLogs(),
        combine(
            expensesRepository.observeExpenseLogs(),
            subscriptionsRepository.observeSubscriptions(),
        ) { expenses, subs -> expenses to subs },
    ) { goals, (primaryCurrency, _), sort, incomes, (expenses, subs) ->
        val active = goals.firstOrNull { !it.isCompleted && it.isActive }
        val backlog = goals.filter { !it.isCompleted && it !== active && it.id != active?.id }
            .sortedWith(sort.comparator())
        val completed = goals.filter { it.isCompleted }

        val capacity = computeCapacity(
            incomes = incomes,
            expenses = expenses,
            subscriptions = subs,
            primaryCurrency = primaryCurrency.code,
        )

        GoalsUiState(
            activeGoal = active,
            backlogGoals = backlog,
            completedGoals = completed,
            totalTargetAmount = (listOfNotNull(active) + backlog).sumOf { it.targetAmount },
            totalSavedAmount = (listOfNotNull(active) + backlog).sumOf { it.currentAmount },
            currency = primaryCurrency,
            activeGoalSoftLimit = ACTIVE_GOAL_SOFT_LIMIT,
            isAtActiveGoalLimit = (listOfNotNull(active).size + backlog.size) >= ACTIVE_GOAL_SOFT_LIMIT,
            sortMode = sort,
            capacity = capacity,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(),
    )

    fun adjustSavedAmount(goalId: String, delta: Double) {
        if (delta == 0.0) return
        viewModelScope.launch {
            goalsRepository.adjustSavedAmount(goalId, delta)
        }
    }

    fun setActiveGoal(goalId: String) {
        viewModelScope.launch {
            goalsRepository.setActiveGoal(goalId)
        }
    }

    fun reorderBacklog(orderedIds: List<String>) {
        viewModelScope.launch {
            goalsRepository.reorderBacklog(orderedIds)
        }
    }

    fun toggleSortMode() {
        val modes = GoalSortMode.entries
        val current = sortMode.value
        sortMode.value = modes[(modes.indexOf(current) + 1) % modes.size]
    }
}

private fun GoalSortMode.comparator(): Comparator<GoalEntry> = when (this) {
    GoalSortMode.BY_PRIORITY -> compareBy { it.priority }
    GoalSortMode.BY_PROGRESS -> compareByDescending { g ->
        if (g.targetAmount > 0) g.currentAmount / g.targetAmount else 0.0
    }
    GoalSortMode.BY_REMAINING -> compareBy { g -> g.targetAmount - g.currentAmount }
    GoalSortMode.BY_DATE -> compareBy { g -> g.targetDateEpochMillis ?: Long.MAX_VALUE }
}

private fun computeCapacity(
    incomes: List<com.kahavanu.domain.model.IncomeLogEntry>,
    expenses: List<com.kahavanu.domain.model.ExpenseLogEntry>,
    subscriptions: List<com.kahavanu.domain.model.Subscription>,
    primaryCurrency: String,
): CapacityBreakdown {
    val zone = ZoneId.systemDefault()
    val now = java.time.LocalDate.now(zone)
    val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val endOfMonthExclusive = now.plusMonths(1).withDayOfMonth(1)
        .atStartOfDay(zone).toInstant().toEpochMilli()

    fun inThisMonth(epochMillis: Long): Boolean =
        epochMillis in startOfMonth until endOfMonthExclusive

    // Fold every currency into the primary one (static rates) so secondary logs aren't dropped.
    val monthlyIncome = incomes.filter { inThisMonth(it.receivedAtEpochMillis) }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, primaryCurrency) }

    val monthlySubscriptions = subscriptions.filter { !it.isPaused }.sumOf { sub ->
        val cost = CurrencyConverter.convert(sub.cost, sub.currency, primaryCurrency)
        when (sub.frequency.lowercase()) {
            "yearly" -> cost / 12.0
            else -> cost
        }
    }

    val monthlyExpenses = expenses.filter { inThisMonth(it.spentAtEpochMillis) }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, primaryCurrency) }

    // Committed = recurring subscriptions; Discretionary = the rest of this month's
    // spend (already excludes future subscription bills, includes any subscription
    // payments that have already posted as expense entries).
    val committed = monthlySubscriptions
    val discretionary = (monthlyExpenses - committed).coerceAtLeast(0.0)
    val rcs = (monthlyIncome - committed - discretionary).coerceAtLeast(0.0)

    return CapacityBreakdown(
        monthlyIncome = monthlyIncome,
        committed = committed,
        discretionary = discretionary,
        realCapacityToSave = rcs,
    )
}
