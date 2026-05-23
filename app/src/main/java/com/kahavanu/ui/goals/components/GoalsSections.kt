package com.kahavanu.ui.goals.components

import com.kahavanu.ui.util.goalCategoryIcon
import com.kahavanu.ui.util.goalCategoryColor
import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.theme.BrandAccent
import com.kahavanu.ui.theme.IconMuted
import com.kahavanu.ui.theme.Romance
import com.kahavanu.ui.theme.UtilityAccent
import com.kahavanu.ui.theme.Warning
import com.kahavanu.ui.theme.Info
import com.kahavanu.ui.theme.Primary
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.EmptyState
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
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
fun BacklogSection(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    onAdjustSaved: (goalId: String, delta: Double) -> Unit,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Backlog Targets",
            subtitle = if (goals.isEmpty()) "No other targets" else "${goals.size} target${if (goals.size > 1) "s" else ""} in backlog",
            actionText = if (goals.isEmpty()) "Add Goal" else null,
            onActionClick = if (goals.isEmpty()) onAddGoal else null,
        )
        
        Spacer(modifier = Modifier.height(Spacing.small))

        if (goals.isEmpty()) {
            EmptyGoalsPlaceholder()
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    goals.forEachIndexed { index, goal ->
                        BacklogItemRow(
                            goal = goal,
                            currency = currency,
                            onAdjustSaved = { delta -> onAdjustSaved(goal.id, delta) }
                        )
                        if (index < goals.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = Spacing.small)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BacklogItemRow(
    goal: GoalEntry,
    currency: CurrencyOption,
    onAdjustSaved: (Double) -> Unit,
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else {
        0
    }
    val color = goalCategoryColor(goal.category)

    var dialogMode by remember { mutableStateOf<AdjustDialogMode?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { dialogMode = AdjustDialogMode.Add }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Drag handle (Grab handle)
        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = "Reorder handle",
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        // Raw Category Icon (no background box, size 22.dp)
        Icon(
            imageVector = goalCategoryIcon(goal.category),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )

        // Center: Title, elegant thin progress bar, saved vs target metrics
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(color),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Details info
            Text(
                text = "${formatAmount(goal.currentAmount, currency.code)} of ${formatAmount(goal.targetAmount, currency.code)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
            )
        }

        // Reorder options icon
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Options",
            tint = TextSecondary,
            modifier = Modifier
                .size(20.dp)
                .clickable { dialogMode = AdjustDialogMode.Add }
        )
    }

    dialogMode?.let { mode ->
        AdjustAmountDialog(
            mode = mode,
            currencyCode = currency.code,
            onDismiss = { dialogMode = null },
            onConfirm = { amount ->
                onAdjustSaved(amount)
                dialogMode = null
            }
        )
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
            title = "Completed Targets",
            subtitle = "${goals.size} target${if (goals.size == 1) "" else "s"} achieved",
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
    val color = goalCategoryColor(goal.category)

    var dialogState by remember { mutableStateOf<AdjustDialogMode?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
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
                        imageVector = goalCategoryIcon(goal.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp),
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
                            color = TextSecondary,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(goal.currentAmount, currency.code),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else TextPrimary,
                    )
                    Text(
                        text = "of ${formatAmount(goal.targetAmount, currency.code)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isCompleted) MaterialTheme.extendedColors.brandAccent else color),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$progressPercent% complete",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
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
        }
    }
}

private enum class AdjustDialogMode { Add, Subtract }

@Composable
private fun AdjustAmountDialog(
    mode: AdjustDialogMode,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val title = "Quick Adjust Target Fund"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "Specify the savings amount to allocate in $currencyCode",
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                OutlinedTextField(
                    value = input,
                    onValueChange = { new -> input = new.filter { it.isDigit() || it == '.' } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("e.g. 5000") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = input.toDoubleOrNull()?.let { it > 0 } == true,
                onClick = { input.toDoubleOrNull()?.let { onConfirm(it) } },
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun EmptyGoalsPlaceholder() {
    EmptyState(
        icon = Icons.Outlined.Flag,
        title = "No Backlog Targets",
        subtitle = "Set more goals to see them lined up in your backlog",
        iconTint = TextSecondary.copy(alpha = 0.5f),
        iconSize = 36.dp,
    )
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
