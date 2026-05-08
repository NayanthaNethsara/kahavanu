package com.kahavanu.ui.income.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.income.IncomeBreakdownItem
import com.kahavanu.ui.income.IncomeFilter
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondaryEmerald
import com.kahavanu.ui.theme.TextTertiaryEmerald
import kotlin.math.roundToInt

@Composable
fun TotalExpectedCard(
    totalForMonth: Double,
    currency: String,
    monthLabel: String,
    breakdowns: List<IncomeBreakdownItem>,
    selectedFilter: IncomeFilter,
    onFilterSelected: (IncomeFilter) -> Unit,
) {
    // If Pending is selected, we show 0 for now as it's not yet implemented in the data layer
    val displayTotal = if (selectedFilter == IncomeFilter.ALL) totalForMonth else 0.0
    val totalText = formatAmount(displayTotal, currency)
    val progress = if (displayTotal > 0.0) 1f else 0f
    val progressLabel = "${(progress * 100).roundToInt()}%"
    val receivedLabel = when {
        selectedFilter == IncomeFilter.PENDING -> "Pending payments this month"
        displayTotal > 0.0 -> "$totalText received"
        else -> "No income received"
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total received · $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = totalText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .background(RawColors.Slate.Slate100.copy(alpha = 0.5f), shape = KahavanuShapes.small)
                        .padding(2.dp)
                ) {
                    IncomeFilterTab(
                        text = "All",
                        isSelected = selectedFilter == IncomeFilter.ALL,
                        onClick = { onFilterSelected(IncomeFilter.ALL) }
                    )
                    IncomeFilterTab(
                        text = "Pending",
                        isSelected = selectedFilter == IncomeFilter.PENDING,
                        onClick = { onFilterSelected(IncomeFilter.PENDING) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receivedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = RawColors.Emerald.Emerald500,
                trackColor = RawColors.Slate.Slate100.copy(alpha = 0.5f),
            )

            if (selectedFilter == IncomeFilter.ALL && breakdowns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))
                
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    breakdowns.forEach { item ->
                        SummaryItem(
                            label = item.label,
                            value = formatAmount(item.amount, currency),
                            color = item.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun IncomeFilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
        label = "tabBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        label = "tabText"
    )

    Box(
        modifier = Modifier
            .clip(KahavanuShapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
