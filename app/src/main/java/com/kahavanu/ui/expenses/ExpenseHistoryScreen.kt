package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.ui.common.AmountRangeSection
import com.kahavanu.ui.common.FilterBottomSheet
import com.kahavanu.ui.common.FilterChips
import com.kahavanu.ui.common.FilterSection
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.HistoryDateRange
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.MorphingIconButton
import com.kahavanu.ui.common.NestedSearchField
import com.kahavanu.ui.common.categoryColor
import com.kahavanu.ui.common.categoryIcon
import com.kahavanu.ui.theme.AccentIncomeBorder
import com.kahavanu.ui.theme.AccentIncomeSoft
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    onBack: () -> Unit,
    viewModel: ExpenseHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val grouped = uiState.expenses.groupBy { expense ->
        Instant.ofEpochMilli(expense.spentAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }.toSortedMap(compareByDescending { it })

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    KahavanuSubScreen(
        label = "All Expenses",
        title = "${uiState.transactionCount} transactions",
        onBack = onBack,
        trailing = {
            MorphingIconButton(
                icon = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                onClick = { isFilterSheetOpen = true },
                nested = true,
                badge = uiState.hasActiveFilter,
                tint = if (uiState.hasActiveFilter) MaterialTheme.colorScheme.primary else TextSecondary,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            item {
                NestedSearchField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search expenses...",
                )
            }

            item {
                TotalExpensesCard(
                    totalAmount = uiState.totalExpenses,
                    currencyCode = uiState.currencyCode,
                )
            }

            if (grouped.isEmpty()) {
                item { EmptyHistoryState() }
            } else {
                grouped.forEach { (date, entries) ->
                    item { DateHeader(date = date) }
                    item {
                        DayTransactionsCard(
                            entries = entries,
                            currencyCode = uiState.currencyCode,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }

    if (isFilterSheetOpen) {
        FilterBottomSheet(
            title = "Filter expenses",
            subtitle = "Refine by category, date, payment, or amount",
            sheetState = sheetState,
            onDismiss = { isFilterSheetOpen = false },
            onClear = {
                viewModel.clearFilters()
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) isFilterSheetOpen = false
                }
            },
            onApply = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) isFilterSheetOpen = false
                }
            },
        ) {
            FilterSection(title = "Category") {
                FilterChips(
                    options = uiState.availableCategories,
                    selected = uiState.filters.category,
                    onSelect = { newCategory ->
                        viewModel.onFiltersChange { it.copy(category = newCategory) }
                    },
                    labelFor = { it },
                )
            }
            FilterSection(title = "Date range") {
                FilterChips(
                    options = HistoryDateRange.entries.toList(),
                    selected = uiState.filters.dateRange,
                    onSelect = { newRange ->
                        viewModel.onFiltersChange { it.copy(dateRange = newRange) }
                    },
                    labelFor = { it.label },
                )
            }
            if (uiState.availablePaymentMethods.size > 1) {
                FilterSection(title = "Payment method") {
                    FilterChips(
                        options = uiState.availablePaymentMethods,
                        selected = uiState.filters.paymentMethod,
                        onSelect = { newMethod ->
                            viewModel.onFiltersChange { it.copy(paymentMethod = newMethod) }
                        },
                        labelFor = { it },
                    )
                }
            }
            FilterSection(title = "Amount") {
                AmountRangeSection(
                    minValue = uiState.filters.minAmount,
                    maxValue = uiState.filters.maxAmount,
                    onMinChange = { value ->
                        viewModel.onFiltersChange { it.copy(minAmount = value.filterAmount()) }
                    },
                    onMaxChange = { value ->
                        viewModel.onFiltersChange { it.copy(maxAmount = value.filterAmount()) }
                    },
                    currencyCode = uiState.currencyCode,
                )
            }
        }
    }
}

private fun String.filterAmount(): String = filter { it.isDigit() || it == '.' }

@Composable
private fun TotalExpensesCard(
    totalAmount: Double,
    currencyCode: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentIncomeSoft, RoundedCornerShape(16.dp))
            .border(0.7.dp, AccentIncomeBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = Spacing.large, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$currencyCode ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 37.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                letterSpacing = (-0.41).sp,
            )
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val label = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.xs,
            color = TextSecondary,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun DayTransactionsCard(
    entries: List<ExpenseLogEntry>,
    currencyCode: String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            entries.forEachIndexed { index, entry ->
                TransactionRow(
                    entry = entry,
                    currencyCode = currencyCode,
                )
                if (index != entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    entry: ExpenseLogEntry,
    currencyCode: String,
) {
    val tint = categoryColor(entry.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(entry.category),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = entry.category,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }

        Text(
            text = "-$currencyCode ${String.format(Locale.getDefault(), "%,.0f", entry.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 21.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            letterSpacing = (-0.38).sp,
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No expenses found",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
    }
}
