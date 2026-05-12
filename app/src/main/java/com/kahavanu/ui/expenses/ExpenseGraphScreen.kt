package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun ExpenseGraphScreen(
    onBack: () -> Unit,
    viewModel: ExpenseGraphViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.extraLarge, vertical = 30.dp),
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
                        text = "EXPENSE ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        letterSpacing = 0.72.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Spending patterns & insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.lg,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.8).sp,
                        color = TextPrimary,
                    )
                }
            }

            // Total Spending Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                    Text(
                        text = "USD ${String.format("%.2f", stats.totalSpent)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.xl,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "This month",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                        color = TextSecondary.copy(alpha = 0.7f),
                    )
                }
            }

            // Category Breakdown
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    Text(
                        text = "Spending by Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = TextSize.base,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                    )

                    stats.categoryBreakdown.forEach { (category, amount) ->
                        CategorySpendRow(
                            category = category,
                            amount = amount,
                            total = stats.totalSpent,
                        )
                    }
                }
            }

            // Chart Placeholder
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(Spacing.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "📊 Chart Visualization",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "Integration ready for external chart library (e.g., Vico)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = TextSize.sm,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }
}

@Composable
private fun CategorySpendRow(
    category: String,
    amount: Double,
    total: Double,
) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    val categoryColor = when (category) {
        "Essentials" -> RawColors.Rose.Rose500
        "Transport" -> RawColors.Blue.Blue500
        "Lifestyle" -> RawColors.Amber.Amber500
        "Subscriptions" -> RawColors.Indigo.Indigo400
        else -> RawColors.Gray.Gray400
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.sm,
                color = TextPrimary,
            )
            Text(
                text = "USD ${String.format("%.2f", amount)} ($percentage%)",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.sm,
                color = categoryColor,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    color = categoryColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.extraSmall,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(4.dp)
                    .background(
                        color = categoryColor,
                        shape = MaterialTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}
