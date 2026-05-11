package com.kahavanu.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.ui.income.components.formatAmount
import com.kahavanu.ui.income.components.formatDate
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun RecurringManagerScreen(
    onBack: () -> Unit,
    viewModel: IncomeOverviewViewModel = hiltViewModel(),
) {
    val scheduledIncomes by viewModel.scheduledIncomes.collectAsStateWithLifecycle()
    val recurrentItems = scheduledIncomes.filter { it.type == IncomeSourceType.RECURRENT }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Emerald.Emerald50.copy(alpha = 0.4f),
                        RawColors.Slate.Slate100
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 120.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Column {
                    Text(
                        text = "Recurring Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.xl,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Track active schedules and recent runs",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (recurrentItems.isEmpty()) {
            item {
                Surface(
                    shape = KahavanuShapes.large,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.extraLarge),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventRepeat,
                            contentDescription = null,
                            tint = RawColors.Emerald.Emerald400,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No recurring incomes yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(recurrentItems, key = { it.id }) { item ->
                RecurringCard(
                    item = item,
                    onDisable = { viewModel.disableScheduled(item.id) }
                )
            }
        }
    }
}

@Composable
private fun RecurringCard(
    item: ScheduledIncome,
    onDisable: () -> Unit,
) {
    Surface(
        shape = KahavanuShapes.large,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RawColors.Emerald.Emerald500.copy(alpha = 0.12f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Autorenew,
                            contentDescription = null,
                            tint = RawColors.Emerald.Emerald700,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = formatAmount(item.amount, item.currency),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = RawColors.Slate.Slate100
                ) {
                    Text(
                        text = item.frequency ?: "Monthly",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(label = "Next run", value = formatDate(item.scheduledDateEpochMillis))
                InfoRow(
                    label = "Last run",
                    value = item.lastGeneratedEpochMillis?.let { formatDate(it) } ?: "Never"
                )
                InfoRow(label = "Occurrences", value = item.occurrenceCount.toString())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onDisable,
                    shape = CircleShape,
                    color = RawColors.Red.Red500.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = null,
                            tint = RawColors.Red.Red600,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Disable",
                            style = MaterialTheme.typography.labelSmall,
                            color = RawColors.Red.Red600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
