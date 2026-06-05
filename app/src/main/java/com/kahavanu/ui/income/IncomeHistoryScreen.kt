package com.kahavanu.ui.income

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.clickable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.ui.common.AmountRangeSection
import com.kahavanu.ui.common.ActiveFilterChip
import com.kahavanu.ui.common.AmountRangeSection
import com.kahavanu.ui.common.FilterBottomSheet
import com.kahavanu.ui.common.FilterChips
import com.kahavanu.ui.common.FilterSection
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.HistoryDateRange
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.SearchWithFiltersBar
import com.kahavanu.ui.income.components.HistoryListItem
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeHistoryScreen(
    onBack: () -> Unit,
    initialFilter: HistoryFilter = HistoryFilter.ALL,
    viewModel: IncomeOverviewViewModel = hiltViewModel()
) {
    val allLogs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    val allScheduled by viewModel.scheduledIncomes.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.applyInitialStatusFilter(initialFilter) }

    val activeChips = buildList {
        if (filters.status != HistoryFilter.ALL)
            add(ActiveFilterChip(filters.status.label) { viewModel.updateFilters(filters.copy(status = HistoryFilter.ALL)) })
        if (filters.dateRange != HistoryDateRange.ALL)
            add(ActiveFilterChip(filters.dateRange.label) { viewModel.updateFilters(filters.copy(dateRange = HistoryDateRange.ALL)) })
        if (filters.minAmount.isNotBlank())
            add(ActiveFilterChip("Min ${filters.minAmount}") { viewModel.updateFilters(filters.copy(minAmount = "")) })
        if (filters.maxAmount.isNotBlank())
            add(ActiveFilterChip("Max ${filters.maxAmount}") { viewModel.updateFilters(filters.copy(maxAmount = "")) })
    }

    KahavanuSubScreen(
        label = "Income History",
        title = "${historyItems.size} logs",
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.extraLarge),
        ) {
            SearchWithFiltersBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onFilterClick = { isFilterSheetOpen = true },
                filterActive = filters.isActive,
                activeChips = activeChips,
                placeholder = "Search logs...",
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            GlassCard(modifier = Modifier.weight(1f)) {
                if (historyItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No income history found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(historyItems) { index, item ->
                            val canReceive = item is HistoryItem.Scheduled &&
                                item.scheduled.type == IncomeSourceType.PENDING &&
                                item.scheduled.lastGeneratedEpochMillis == null
                            HistoryListItem(
                                item = item,
                                onMarkAsReceived = if (canReceive && item is HistoryItem.Scheduled) {
                                    { viewModel.markAsReceived(item.scheduled.id) }
                                } else null,
                            )
                            if (index < historyItems.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Show 20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Prev",
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = "1 / ${maxOf(1, (historyItems.size + 19) / 20)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    if (isFilterSheetOpen) {
        FilterBottomSheet(
            title = "Filter income",
            subtitle = "Refine the list by status, date, or amount",
            sheetState = sheetState,
            onDismiss = { isFilterSheetOpen = false },
            onClear = {
                viewModel.updateFilters(IncomeFilters())
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
            FilterSection(title = "Status") {
                FilterChips(
                    options = HistoryFilter.entries.toList(),
                    selected = filters.status,
                    onSelect = { viewModel.updateFilters(filters.copy(status = it)) },
                    labelFor = { it.label },
                )
            }
            FilterSection(title = "Date range") {
                FilterChips(
                    options = HistoryDateRange.entries.toList(),
                    selected = filters.dateRange,
                    onSelect = { viewModel.updateFilters(filters.copy(dateRange = it)) },
                    labelFor = { it.label },
                )
            }
            FilterSection(title = "Amount") {
                val firstCurrency = allLogs.firstOrNull()?.currency
                    ?: allScheduled.firstOrNull()?.currency
                    ?: "LKR"
                AmountRangeSection(
                    minValue = filters.minAmount,
                    maxValue = filters.maxAmount,
                    onMinChange = { viewModel.updateFilters(filters.copy(minAmount = it.filterAmount())) },
                    onMaxChange = { viewModel.updateFilters(filters.copy(maxAmount = it.filterAmount())) },
                    currencyCode = firstCurrency,
                )
            }
        }
    }
}

private fun String.filterAmount(): String = filter { it.isDigit() || it == '.' }

sealed class HistoryItem {
    data class Log(val log: IncomeLogEntry) : HistoryItem()
    data class Scheduled(val scheduled: ScheduledIncome) : HistoryItem()

    val title: String get() = when (this) {
        is Log -> log.title
        is Scheduled -> scheduled.title
    }
    val amount: Double get() = when (this) {
        is Log -> log.amount
        is Scheduled -> scheduled.amount
    }
    val currency: String get() = when (this) {
        is Log -> log.currency
        is Scheduled -> scheduled.currency
    }
    val timestamp: Long get() = when (this) {
        is Log -> log.receivedAtEpochMillis
        is Scheduled -> scheduled.lastGeneratedEpochMillis ?: scheduled.scheduledDateEpochMillis
    }
    val isInvoiceSent: Boolean get() = when (this) {
        is Log -> log.isInvoiceSent
        is Scheduled -> scheduled.isInvoiceSent
    }
}
