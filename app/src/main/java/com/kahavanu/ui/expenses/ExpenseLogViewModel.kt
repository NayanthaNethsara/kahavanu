package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseLogViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val formState = MutableStateFlow(ExpenseLogUiState())

    val uiState: StateFlow<ExpenseLogUiState> = combine(
        formState,
        settingsRepository.observeCurrencySettings(),
    ) { state, (primaryCurrency, _) ->
        state.copy(currencyCode = primaryCurrency.code)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseLogUiState(),
    )

    fun onDescriptionChange(description: String) {
        formState.update { it.copy(description = description) }
    }

    fun onAmountChange(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        formState.update { it.copy(amount = sanitized) }
    }

    fun onCategoryChange(category: String) {
        formState.update { it.copy(category = category) }
    }

    fun onPaymentMethodChange(method: String) {
        formState.update { it.copy(paymentMethod = method) }
    }

    fun onNotesChange(notes: String) {
        formState.update { it.copy(notes = notes) }
    }

    fun onDateChange(date: LocalDate) {
        formState.update { it.copy(spentDate = date) }
    }

    fun onDatePickerOpenChange(isOpen: Boolean) {
        formState.update { it.copy(isDatePickerOpen = isOpen) }
    }

    fun logExpense() {
        val state = uiState.value
        if (state.amount.isBlank()) {
            formState.update { it.copy(errorMessage = "Amount is required") }
            return
        }

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            formState.update { it.copy(errorMessage = "Please enter a valid amount") }
            return
        }

        val spentAt = state.spentDate
            ?.atStartOfDay(java.time.ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: System.currentTimeMillis()
        val normalizedDescription = state.description.trim()
        val resolvedTitle = normalizedDescription.ifBlank { state.category }

        val entry = ExpenseLogEntry(
            title = resolvedTitle,
            amount = amount,
            currency = state.currencyCode,
            spentAtEpochMillis = spentAt,
            merchant = normalizedDescription.ifBlank { null },
            category = state.category,
            notes = state.notes?.ifBlank { null },
            paymentMethod = state.paymentMethod,
        )

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = expensesRepository.logExpense(entry)
            formState.update {
                it.copy(
                    isSaving = false,
                    successMessage = result.fold(
                        { "Expense logged successfully" },
                        { "Failed to log expense" }
                    ),
                    errorMessage = result.fold(
                        { null },
                        { throwable -> throwable.message ?: "Failed to log expense" }
                    ),
                    didSubmitSuccessfully = result.isSuccess,
                )
            }
            if (result.isSuccess) {
                resetForm()
            }
        }
    }

    fun onSubmitHandled() {
        formState.update { it.copy(didSubmitSuccessfully = false) }
    }

    private fun resetForm() {
        formState.update {
            ExpenseLogUiState(
                category = it.category,
                paymentMethod = it.paymentMethod,
                spentDate = LocalDate.now(),
                successMessage = "Expense logged successfully",
                didSubmitSuccessfully = true,
                currencyCode = it.currencyCode,
            )
        }
    }
}

data class ExpenseLogUiState(
    val description: String = "",
    val amount: String = "",
    val category: String = "Food",
    val paymentMethod: String = "Card",
    val notes: String? = null,
    val currencyCode: String = "LKR",
    val spentDate: LocalDate? = LocalDate.now(),
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val didSubmitSuccessfully: Boolean = false,
)
