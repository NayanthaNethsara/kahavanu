package com.kahavanu.ui.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
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

private const val QUICK_STEP_AMOUNT = 1_000.0

@Composable
fun ActiveGoalsSection(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    softLimit: Int,
    isAtLimit: Boolean,
    onAddGoal: () -> Unit,
    onAdjustSaved: (goalId: String, delta: Double) -> Unit,
) {
    Column {
        SectionHeader(
            title = "Active Goals",
            subtitle = if (goals.isEmpty()) "No active goals yet" else "${goals.size} of $softLimit recommended",
            actionText = if (goals.isEmpty()) "Add one" else null,
            onActionClick = if (goals.isEmpty()) onAddGoal else null,
        )
        if (isAtLimit && goals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.small))
            SoftLimitBanner(count = goals.size, limit = softLimit)
        }
        Spacer(modifier = Modifier.height(Spacing.small))
        if (goals.isEmpty()) {
            EmptyGoalsPlaceholder()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                goals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        currency = currency,
                        onAdjustSaved = { delta -> onAdjustSaved(goal.id, delta) },
                    )
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
        Spacer(modifier = Modifier.height(Spacing.small))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            goals.forEach { goal ->
                GoalCard(goal = goal, currency = currency, isCompleted = true)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GoalCard(
    goal: GoalEntry,
    currency: CurrencyOption,
    isCompleted: Boolean = false,
    onAdjustSaved: ((Double) -> Unit)? = null,
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else {
        0
    }
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val color = categoryColor(goal.category)

    var dialogState by remember { mutableStateOf<AdjustDialogMode?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            // Header: bare icon + title/category, amounts to the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = categoryIcon(goal.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = TextSize.base,
                            fontWeight = FontWeight.SemiBold,
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
                        fontWeight = FontWeight.SemiBold,
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

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(RawColors.Slate.Slate200),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isCompleted) RawColors.Emerald.Emerald500 else color),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Meta row: progress%, remaining, target date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$progressPercent% complete",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    fontWeight = FontWeight.Medium,
                    color = color,
                )
                if (!isCompleted && remaining > 0.0) {
                    Text(
                        text = "${formatAmount(remaining, currency.code)} to go",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                }
            }

            // Last updated + target date row
            val targetDateText = goal.targetDateEpochMillis?.let { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
                "Target: ${date.format(formatter)}"
            }
            val lastUpdatedText = "Updated ${relativeTime(goal.lastUpdatedEpochMillis)}"

            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = lastUpdatedText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                )
                if (targetDateText != null) {
                    Text(
                        text = targetDateText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                }
            }

            // Adjust buttons (only for active goals)
            if (!isCompleted && onAdjustSaved != null) {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    AdjustButton(
                        icon = Icons.Outlined.Remove,
                        label = "− ${formatAmount(QUICK_STEP_AMOUNT, currency.code)}",
                        color = RawColors.Slate.Slate500,
                        enabled = goal.currentAmount > 0.0,
                        onTap = { onAdjustSaved(-QUICK_STEP_AMOUNT) },
                        onLongPress = { dialogState = AdjustDialogMode.Subtract },
                        modifier = Modifier.weight(1f),
                    )
                    AdjustButton(
                        icon = Icons.Outlined.Add,
                        label = "+ ${formatAmount(QUICK_STEP_AMOUNT, currency.code)}",
                        color = color,
                        enabled = true,
                        onTap = { onAdjustSaved(QUICK_STEP_AMOUNT) },
                        onLongPress = { dialogState = AdjustDialogMode.Add },
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = "Long-press for custom amount",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.extraSmall),
                )
            }
        }
    }

    dialogState?.let { mode ->
        AdjustAmountDialog(
            mode = mode,
            currencyCode = currency.code,
            onDismiss = { dialogState = null },
            onConfirm = { amount ->
                val delta = if (mode == AdjustDialogMode.Add) amount else -amount
                onAdjustSaved?.invoke(delta)
                dialogState = null
            },
        )
    }
}

private enum class AdjustDialogMode { Add, Subtract }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AdjustButton(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    enabled: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgAlpha = if (enabled) 0.10f else 0.04f
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = bgAlpha))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .combinedClickable(
                enabled = enabled,
                onClick = onTap,
                onLongClick = onLongPress,
            )
            .padding(vertical = 10.dp, horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = contentAlpha),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(Spacing.extraSmall))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.xs,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = contentAlpha),
        )
    }
}

@Composable
private fun AdjustAmountDialog(
    mode: AdjustDialogMode,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val title = if (mode == AdjustDialogMode.Add) "Add to savings" else "Remove from savings"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "Enter an amount in $currencyCode",
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                OutlinedTextField(
                    value = input,
                    onValueChange = { new -> input = new.filter { it.isDigit() || it == '.' } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = input.toDoubleOrNull()?.let { it > 0 } == true,
                onClick = { input.toDoubleOrNull()?.let { onConfirm(it) } },
            ) {
                Text(if (mode == AdjustDialogMode.Add) "Add" else "Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SoftLimitBanner(count: Int, limit: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RawColors.Amber.Amber50)
            .border(1.dp, RawColors.Amber.Amber200, RoundedCornerShape(12.dp))
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            tint = RawColors.Amber.Amber600,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(Spacing.small))
        Text(
            text = "You have $count active goals. We suggest keeping it under $limit to stay focused.",
            style = MaterialTheme.typography.labelSmall,
            fontSize = TextSize.xs,
            color = RawColors.Amber.Amber600,
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

private fun relativeTime(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - epochMillis).coerceAtLeast(0)
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val date = Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
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
    GoalCategory.SAVINGS -> Icons.Outlined.Savings
    GoalCategory.TRAVEL -> Icons.Outlined.FlightTakeoff
    GoalCategory.EMERGENCY -> Icons.Outlined.Shield
    GoalCategory.EDUCATION -> Icons.Outlined.School
    GoalCategory.PURCHASE -> Icons.Outlined.ShoppingBag
    GoalCategory.INVESTMENT -> Icons.Outlined.BarChart
    GoalCategory.OTHER -> Icons.Outlined.Flag
}
