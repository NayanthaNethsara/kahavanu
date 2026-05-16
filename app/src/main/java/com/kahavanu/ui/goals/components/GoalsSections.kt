package com.kahavanu.ui.goals.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ActiveGoalsSection(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    onAddGoal: () -> Unit,
) {
    Column {
        SectionHeader(
            title = "Active Goals",
            subtitle = if (goals.isEmpty()) "No active goals yet" else "${goals.size} goal${if (goals.size == 1) "" else "s"} in progress",
            actionText = if (goals.isEmpty()) "Add one" else null,
            onActionClick = if (goals.isEmpty()) onAddGoal else null,
        )
        if (goals.isEmpty()) {
            EmptyGoalsPlaceholder()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                goals.forEach { goal ->
                    GoalCard(goal = goal, currency = currency)
                }
            }
        }
    }
}

@Composable
fun CompletedGoalsSection(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
) {
    if (goals.isEmpty()) return
    Column {
        SectionHeader(
            title = "Completed",
            subtitle = "${goals.size} goal${if (goals.size == 1) "" else "s"} achieved",
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            goals.forEach { goal ->
                GoalCard(goal = goal, currency = currency, isCompleted = true)
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: GoalEntry,
    currency: CurrencyOption,
    isCompleted: Boolean = false,
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else {
        0
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryDot(category = goal.category)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = TextSize.base,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                        )
                        Text(
                            text = goal.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = TextSize.xs,
                            color = TextSecondary,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(goal.currentAmount, currency.code),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = TextSize.sm,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) RawColors.Emerald.Emerald600 else TextPrimary,
                    )
                    Text(
                        text = "of ${formatAmount(goal.targetAmount, currency.code)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(RawColors.Slate.Slate200),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            if (isCompleted) RawColors.Emerald.Emerald500
                            else categoryColor(goal.category),
                        ),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                )
                goal.targetDateEpochMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
                    Text(
                        text = "by ${date.format(formatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDot(category: GoalCategory) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(categoryColor(category).copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = categoryColor(category),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun EmptyGoalsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = RawColors.Slate.Slate400,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = "Set your first goal",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = "Track savings targets and milestones",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.sm,
                color = TextSecondary,
            )
        }
    }
}

private fun categoryColor(category: GoalCategory) = when (category) {
    GoalCategory.SAVINGS -> RawColors.Emerald.Emerald500
    GoalCategory.TRAVEL -> RawColors.Blue.Blue500
    GoalCategory.EMERGENCY -> RawColors.Amber.Amber600
    GoalCategory.EDUCATION -> RawColors.Violet.Violet500
    GoalCategory.PURCHASE -> RawColors.Rose.Rose500
    GoalCategory.INVESTMENT -> RawColors.Emerald.Emerald600
    GoalCategory.OTHER -> RawColors.Slate.Slate500
}

private fun categoryIcon(category: GoalCategory): ImageVector = when (category) {
    GoalCategory.SAVINGS -> Icons.Outlined.AttachMoney
    GoalCategory.TRAVEL -> Icons.Outlined.FlightTakeoff
    GoalCategory.EMERGENCY -> Icons.Outlined.HealthAndSafety
    GoalCategory.EDUCATION -> Icons.Outlined.Book
    GoalCategory.PURCHASE -> Icons.Outlined.ShoppingBag
    GoalCategory.INVESTMENT -> Icons.Outlined.BarChart
    GoalCategory.OTHER -> Icons.Outlined.Flag
}
