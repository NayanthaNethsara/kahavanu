package com.kahavanu.ui.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import com.kahavanu.ui.theme.extendedColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GoalsSummaryCard(
    featuredGoal: GoalEntry?,
    currency: CurrencyOption,
    onAddGoal: () -> Unit = {},
    onAddSavings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (featuredGoal == null) {
        ActiveGoalEmptyCard(
            currency = currency,
            onAddGoal = onAddGoal,
            modifier = modifier,
        )
    } else {
        ActiveGoalSummary(
            featuredGoal = featuredGoal,
            currency = currency,
            onAddSavings = onAddSavings,
            modifier = modifier,
        )
    }
}

@Composable
private fun ActiveGoalSummary(
    featuredGoal: GoalEntry,
    currency: CurrencyOption,
    onAddSavings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val savedAmount = featuredGoal.currentAmount
    val targetAmount = featuredGoal.targetAmount
    val progressPercent = if (targetAmount > 0.0) {
        ((savedAmount / targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else 0

    val progressBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF34D399),
            Color(0xFF10B981),
            Color(0xFF059669),
        ),
    )

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ACTIVE GOAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                        .padding(horizontal = Spacing.small, vertical = 4.dp),
                ) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = featuredGoal.title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextSize.xxl,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Saved so far",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = formatGoalAmount(savedAmount, currency.code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = TextSize.lg,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = formatGoalAmount(targetAmount, currency.code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = TextSize.base,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                val ratio = (progressPercent / 100f).coerceIn(0f, 1f)
                if (ratio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(progressBrush),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.08f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "On track. Every verified payout raises the level.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF059669),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Button(
                onClick = onAddSavings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Add savings")
            }
        }
    }
}

@Composable
private fun ActiveGoalEmptyCard(
    currency: CurrencyOption,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddGoal() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ACTIVE GOAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.brandAccent,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = "Pick your first target",
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextSize.xl,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            Text(
                text = "Set a goal and we'll feature it here, project arrival time, and show how much of your ${currency.code} capacity to allocate each month.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Surface(
                shape = RoundedCornerShape(CornerRadius.large),
                color = MaterialTheme.extendedColors.brandAccent,
                modifier = Modifier.clickable { onAddGoal() },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Set Your First Goal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

private fun formatGoalAmount(amount: Double, currencyCode: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "$currencyCode ${formatter.format(amount)}"
}
