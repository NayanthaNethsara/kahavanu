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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HeaderRow(
                    transactionCount = uiState.transactionCount,
                    onBack = onBack,
                )
            }

            item {
                SearchBar(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChange,
                )
            }

            item {
                TotalExpensesCard(
                    totalAmount = uiState.totalExpenses,
                    currencyCode = uiState.currencyCode,
                )
            }

            if (grouped.isEmpty()) {
                item {
                    EmptyHistoryState()
                }
            } else {
                grouped.forEach { (date, entries) ->
                    item {
                        DateHeader(date = date)
                    }
                    item {
                        DayTransactionsCard(
                            entries = entries,
                            currencyCode = uiState.currencyCode,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun HeaderRow(
    transactionCount: Int,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .border(0.7.dp, RawColors.Slate.Slate200.copy(alpha = 0.8f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = RawColors.Emerald.Emerald600,
                )
            }
            Column {
                Text(
                    text = "ALL EXPENSES",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 0.72.sp,
                )
                Text(
                    text = "$transactionCount transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.xl,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.8).sp,
                )
            }
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                .border(0.7.dp, RawColors.Slate.Slate200.copy(alpha = 0.8f), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                tint = TextSecondary,
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search expenses...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextSecondary,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = textFieldColors(),
    )
}

@Composable
private fun TotalExpensesCard(
    totalAmount: Double,
    currencyCode: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A00BC7D), RoundedCornerShape(16.dp))
            .border(0.7.dp, Color(0x3300BC7D), RoundedCornerShape(16.dp))
            .padding(horizontal = Spacing.large, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = RawColors.Emerald.Emerald600,
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
            color = RawColors.Slate.Slate200.copy(alpha = 0.6f),
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
                        color = RawColors.Slate.Slate200.copy(alpha = 0.5f),
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

private fun categoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Color(0xFFF97316)
        "transport" -> RawColors.Blue.Blue500
        "utilities" -> RawColors.Violet.Violet500
        "shopping", "lifestyle" -> RawColors.Rose.Rose500
        "health" -> RawColors.Red.Red500
        "fun", "subscriptions" -> RawColors.Emerald.Emerald500
        else -> RawColors.Slate.Slate400
    }
}

private fun categoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Icons.Outlined.LocalPizza
        "transport" -> Icons.Outlined.LocalTaxi
        "utilities" -> Icons.Outlined.Bolt
        "shopping", "lifestyle" -> Icons.Outlined.ShoppingBag
        "health" -> Icons.Outlined.HealthAndSafety
        "fun", "subscriptions" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.TipsAndUpdates
    }
}
