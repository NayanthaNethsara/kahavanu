package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseLogViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseLogUiState())
    val uiState: StateFlow<ExpenseLogUiState> = _uiState

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onPaymentMethodChange(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onMerchantChange(merchant: String) {
        _uiState.update { it.copy(merchant = merchant) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(spentDate = date) }
    }

    fun onDatePickerOpenChange(isOpen: Boolean) {
        _uiState.update { it.copy(isDatePickerOpen = isOpen) }
    }

    fun logExpense() {
        val state = _uiState.value
        if (state.title.isBlank() || state.amount.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title and amount are required") }
            return
        }

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount") }
            return
        }

        val spentAt = state.spentDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() 
            ?: System.currentTimeMillis()

        val entry = ExpenseLogEntry(
            title = state.title,
            amount = amount,
            currency = "USD",
            spentAtEpochMillis = spentAt,
            merchant = state.merchant?.ifBlank { null },
            category = state.category,
            notes = state.notes?.ifBlank { null },
            paymentMethod = state.paymentMethod,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = expensesRepository.logExpense(entry)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = result.fold(
                        { "Expense logged successfully!" },
                        { "Failed to log expense" }
                    ),
                    errorMessage = result.fold(
                        { null },
                        { it.message }
                    ),
                )
            }
            if (result.isSuccess) {
                resetForm()
            }
        }
    }

    private fun resetForm() {
        _uiState.update {
            ExpenseLogUiState()
        }
    }
}

data class ExpenseLogUiState(
    val title: String = "",
    val amount: String = "",
    val category: String = "Essentials",
    val paymentMethod: String = "Cash",
    val merchant: String? = null,
    val notes: String? = null,
    val spentDate: LocalDate? = LocalDate.now(),
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
