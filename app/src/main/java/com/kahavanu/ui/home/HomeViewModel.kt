package com.kahavanu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SmsScanRepository
import com.kahavanu.domain.repository.SmsSenderRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SieveType { EXPENSE, INCOME }

data class SieveItem(
    val id: String,
    val type: SieveType,
    val title: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val detectedFrom: String,
    val merchantOrSource: String,
)

data class IncomeStreamItem(
    val title: String,
    val subtitle: String,
    val receivedAmount: Double,
    val receivedFormatted: String,
    val pendingText: String,
    val iconIndex: Int,
)

data class HomeUiState(
    val featuredGoal: GoalEntry? = null,
    val sieveItems: List<SieveItem> = emptyList(),
    val incomeStreams: List<IncomeStreamItem> = emptyList(),
    val currentUserName: String = "User",
    val isScanning: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val incomeRepository: IncomeRepository,
    private val expensesRepository: ExpensesRepository,
    private val settingsRepository: SettingsRepository,
    private val smsSenderRepository: SmsSenderRepository,
    private val smsSuggestionRepository: SmsSuggestionRepository,
    private val smsScanRepository: SmsScanRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        goalsRepository.observeGoals(),
        incomeRepository.observeIncomeLogs(),
        smsSuggestionRepository.observePendingSuggestions(),
        smsScanRepository.isScanningFlow,
        smsSenderRepository.observeAuthorizedSenders(),
    ) { goals, incomeLogs, suggestions, isScanning, senders ->
        val featured = goals.firstOrNull { !it.isCompleted }
        val hasEnabledSenders = senders.any { it.isEnabled }
        val sieveItems = if (hasEnabledSenders) suggestions.map { it.toSieveItem() } else emptyList()

        val lkrReceived = incomeLogs.filter { it.currency == "LKR" && it.sourceType != "pending" }.sumOf { it.amount }
        val lkrPending = incomeLogs.filter { it.currency == "LKR" && it.sourceType == "pending" }.sumOf { it.amount }
        val usdReceived = incomeLogs.filter { it.currency == "USD" && it.sourceType != "pending" }.sumOf { it.amount }
        val usdInvoiced = incomeLogs.filter { it.currency == "USD" && it.sourceType == "pending" }.sumOf { it.amount }
        val cryptoReceived = incomeLogs.filter { it.currency in cryptoCurrencies }.sumOf { it.amount }

        HomeUiState(
            featuredGoal = featured,
            sieveItems = sieveItems,
            incomeStreams = listOf(
                IncomeStreamItem(
                    title = "Local · LKR",
                    subtitle = "LKR ${String.format("%,.0f", lkrPending)} pending",
                    receivedAmount = lkrReceived,
                    receivedFormatted = "LKR ${String.format("%,.0f", lkrReceived)}",
                    pendingText = "LKR ${String.format("%,.0f", lkrPending)} pending",
                    iconIndex = 12,
                ),
                IncomeStreamItem(
                    title = "Global · USD",
                    subtitle = "$ ${String.format("%,.0f", usdInvoiced)} invoiced",
                    receivedAmount = usdReceived,
                    receivedFormatted = "$ ${String.format("%,.0f", usdReceived)}",
                    pendingText = "$ ${String.format("%,.0f", usdInvoiced)} invoiced",
                    iconIndex = 13,
                ),
                IncomeStreamItem(
                    title = "Crypto",
                    subtitle = "—",
                    receivedAmount = cryptoReceived,
                    receivedFormatted = "$ ${String.format("%,.0f", cryptoReceived)}",
                    pendingText = "—",
                    iconIndex = 14,
                ),
            ),
            isScanning = isScanning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun scanNow() {
        smsScanRepository.scanNow()
    }

    fun confirmSieveItem(itemId: String) {
        viewModelScope.launch {
            val id = itemId.toLongOrNull() ?: return@launch
            val suggestion = smsSuggestionRepository.getById(id) ?: return@launch
            commitSuggestion(suggestion)
            smsSuggestionRepository.confirm(id)
        }
    }

    fun dismissSieveItem(itemId: String) {
        viewModelScope.launch {
            val id = itemId.toLongOrNull() ?: return@launch
            smsSuggestionRepository.dismiss(id)
        }
    }

    private suspend fun commitSuggestion(s: SmsSuggestion) {
        when (s.kind) {
            SuggestionKind.INCOME -> {
                incomeRepository.logIncome(
                    IncomeLogEntry(
                        title = s.title,
                        amount = s.amount,
                        currency = s.currency,
                        receivedAtEpochMillis = s.txnAtEpochMillis,
                        sourceName = s.merchant ?: s.smsSenderName,
                        sourceType = "sms",
                    )
                )
            }
            SuggestionKind.EXPENSE -> {
                expensesRepository.logExpense(
                    ExpenseLogEntry(
                        title = s.title,
                        amount = s.amount,
                        currency = s.currency,
                        spentAtEpochMillis = s.txnAtEpochMillis,
                        merchant = s.merchant,
                        category = inferExpenseCategory(s.smsSenderName, s.merchant),
                    )
                )
            }
            SuggestionKind.SETTLE_PENDING -> {
                val scheduledId = s.matchedScheduledIncomeId
                if (scheduledId != null) {
                    incomeRepository.markScheduledAsReceived(scheduledId)
                } else {
                    incomeRepository.logIncome(
                        IncomeLogEntry(
                            title = s.title,
                            amount = s.amount,
                            currency = s.currency,
                            receivedAtEpochMillis = s.txnAtEpochMillis,
                            sourceName = s.smsSenderName,
                            sourceType = "sms",
                        )
                    )
                }
            }
        }
    }
}

private fun SmsSuggestion.toSieveItem() = SieveItem(
    id = localId.toString(),
    type = if (kind == SuggestionKind.EXPENSE) SieveType.EXPENSE else SieveType.INCOME,
    title = title,
    amount = amount,
    currency = currency,
    category = inferExpenseCategory(smsSenderName, merchant),
    detectedFrom = "From $smsSenderName",
    merchantOrSource = merchant ?: smsSenderName,
)

private val cryptoCurrencies = setOf("USDT", "BTC", "ETH")

internal fun inferExpenseCategory(senderName: String, merchant: String?): String {
    val text = listOf(senderName, merchant ?: "").joinToString(" ").lowercase()
    return when {
        "pickme" in text || "uber" in text || "taxi" in text -> "Transport"
        "keells" in text || "cargills" in text || "arpico" in text || "laugfs" in text -> "Food"
        "pharmacy" in text || "hospital" in text || "clinic" in text -> "Health"
        else -> "Shopping"
    }
}
