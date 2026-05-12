package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.SelectableChip
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpenseLogScreen(
    onBack: () -> Unit,
    onLogged: () -> Unit,
    viewModel: ExpenseLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    }
    val dateLabel = uiState.spentDate?.format(dateFormatter).orEmpty()

    if (uiState.isDatePickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.spentDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli() ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerOpenChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onDateChange(selectedDate)
                        }
                        viewModel.onDatePickerOpenChange(false)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDatePickerOpenChange(false) }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(
                    start = Spacing.extraLarge,
                    end = Spacing.extraLarge,
                    top = 30.dp,
                    bottom = Spacing.large,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.medium))
                Column {
                    Text(
                        text = "LOG EXPENSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        letterSpacing = 0.72.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Record your spending",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.lg,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.8).sp,
                        color = TextPrimary,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Title")
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Groceries, Gas, Dinner") },
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Amount")
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Category")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf("Essentials", "Transport", "Lifestyle", "Other").forEach { category ->
                        SelectableChip(
                            text = category,
                            selected = uiState.category == category,
                            onClick = { viewModel.onCategoryChange(category) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Payment Method")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf("Cash", "Card", "Transfer").forEach { method ->
                        SelectableChip(
                            text = method,
                            selected = uiState.paymentMethod == method,
                            onClick = { viewModel.onPaymentMethodChange(method) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Merchant (Optional)")
                OutlinedTextField(
                    value = uiState.merchant ?: "",
                    onValueChange = { viewModel.onMerchantChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Keells, Shell, Cafe Noir") },
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Notes (Optional)")
                OutlinedTextField(
                    value = uiState.notes ?: "",
                    onValueChange = { viewModel.onNotesChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add any additional details") },
                    maxLines = 3,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel("Date")
                    Text(
                        text = dateLabel.ifEmpty { "Select date" },
                        color = TextSecondary,
                        fontSize = TextSize.sm,
                    )
                }
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pick date",
                        )
                    },
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                )
            }

            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.sm,
                )
            }

            val successMessage = uiState.successMessage
            if (successMessage != null) {
                Text(
                    text = successMessage,
                    color = TextPrimaryEmerald,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.sm,
                )
            }

            PrimaryActionButton(
                text = if (uiState.isSaving) "Logging..." else "Log Expense",
                enabled = !uiState.isSaving,
                onClick = viewModel::logExpense,
            )

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }
}
