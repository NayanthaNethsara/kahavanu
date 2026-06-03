package com.kahavanu.ui.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.EmptyState
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import com.kahavanu.ui.theme.extendedColors
import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.util.goalCategoryColor
import com.kahavanu.ui.util.goalCategoryIcon
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val ROW_HEIGHT_DP = 72.dp

@Composable
fun BacklogSection(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    onAdjustSaved: (goalId: String, delta: Double) -> Unit,
    onAddGoal: () -> Unit,
    onMakeActive: (goalId: String) -> Unit,
    onReorder: (orderedIds: List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Backlog Targets",
            subtitle = when {
                goals.isEmpty() -> "No other targets"
                goals.size == 1 -> "1 target waiting in line"
                else -> "${goals.size} targets — drag to reprioritize"
            },
            actionText = if (goals.isEmpty()) "Add Goal" else null,
            onActionClick = if (goals.isEmpty()) onAddGoal else null,
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        if (goals.isEmpty()) {
            EmptyGoalsPlaceholder()
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                ReorderableBacklogList(
                    goals = goals,
                    currency = currency,
                    onAdjustSaved = onAdjustSaved,
                    onMakeActive = onMakeActive,
                    onReorder = onReorder,
                )
            }
        }
    }
}

@Composable
private fun ReorderableBacklogList(
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    onAdjustSaved: (goalId: String, delta: Double) -> Unit,
    onMakeActive: (goalId: String) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    val order: SnapshotStateList<GoalEntry> = remember { mutableStateListOf<GoalEntry>().apply { addAll(goals) } }
    LaunchedEffect(goals) {
        val incomingIds = goals.map { it.id }
        val currentIds = order.map { it.id }
        if (incomingIds != currentIds) {
            order.clear()
            order.addAll(goals)
        }
    }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT_DP.toPx() }

    Column(modifier = Modifier.padding(vertical = Spacing.small)) {
        order.forEachIndexed { index, goal ->
            val isDragging = draggingIndex == index
            BacklogItemRow(
                goal = goal,
                currency = currency,
                onAdjustSaved = { delta -> onAdjustSaved(goal.id, delta) },
                onMakeActive = { onMakeActive(goal.id) },
                isDragging = isDragging,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetY else 0f
                        alpha = if (isDragging) 0.95f else 1f
                    },
                dragHandleModifier = Modifier.pointerInput(goal.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingIndex = order.indexOfFirst { it.id == goal.id }
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            draggingIndex = null
                            dragOffsetY = 0f
                            onReorder(order.map { it.id })
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragOffsetY = 0f
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            val current = draggingIndex ?: return@detectDragGesturesAfterLongPress
                            dragOffsetY += drag.y
                            val moveDown = dragOffsetY > rowHeightPx / 2f && current < order.lastIndex
                            val moveUp = dragOffsetY < -rowHeightPx / 2f && current > 0
                            if (moveDown) {
                                val swapped = order.removeAt(current)
                                order.add(current + 1, swapped)
                                draggingIndex = current + 1
                                dragOffsetY -= rowHeightPx
                            } else if (moveUp) {
                                val swapped = order.removeAt(current)
                                order.add(current - 1, swapped)
                                draggingIndex = current - 1
                                dragOffsetY += rowHeightPx
                            }
                        },
                    )
                },
            )
            if (index < order.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                )
            }
        }
    }
}

@Composable
private fun BacklogItemRow(
    goal: GoalEntry,
    currency: CurrencyOption,
    onAdjustSaved: (Double) -> Unit,
    onMakeActive: () -> Unit,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else 0
    val color = goalCategoryColor(goal.category)

    var showAdjust by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val background = if (isDragging) {
        MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.6f)
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .clickable { showAdjust = true }
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = "Drag to reorder",
            tint = TextSecondary.copy(alpha = 0.6f),
            modifier = dragHandleModifier.size(22.dp),
        )

        Icon(
            imageVector = goalCategoryIcon(goal.category),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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

            Text(
                text = "${formatAmount(goal.currentAmount, currency.code)} of ${formatAmount(goal.targetAmount, currency.code)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
            )
        }

        Box {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Options",
                tint = TextSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { showMenu = true },
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Make active") },
                    leadingIcon = {
                        Icon(Icons.Outlined.StarOutline, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        onMakeActive()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Allocate savings") },
                    onClick = {
                        showMenu = false
                        showAdjust = true
                    },
                )
            }
        }
    }

    if (showAdjust) {
        AddSavingsSheet(
            goalTitle = goal.title,
            currencyCode = currency.code,
            onDismiss = { showAdjust = false },
            onConfirm = { amount ->
                onAdjustSaved(amount)
                showAdjust = false
            },
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

@Composable
private fun GoalCard(
    goal: GoalEntry,
    currency: CurrencyOption,
    isCompleted: Boolean = false,
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else 0
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val color = goalCategoryColor(goal.category)

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

/**
 * Bottom-sheet drawer for adding savings to a goal. Reused by the active goal card and
 * every backlog row so the "increment savings" experience is consistent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsSheet(
    goalTitle: String,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var input by remember { mutableStateOf("") }
    val amount by remember(input) { derivedStateOf { input.toDoubleOrNull() } }
    val quickAmounts = listOf(1_000L, 5_000L, 10_000L, 25_000L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Text(
                text = "Add to savings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "Allocate an amount toward “$goalTitle” in $currencyCode.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            OutlinedTextField(
                value = input,
                onValueChange = { new -> input = new.filter { it.isDigit() || it == '.' } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text("Amount ($currencyCode)") },
                placeholder = { Text("e.g. 5000") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                quickAmounts.forEach { quick ->
                    AssistChip(
                        onClick = {
                            val current = input.toDoubleOrNull() ?: 0.0
                            input = (current + quick).toLong().toString()
                        },
                        label = { Text("+${formatAmount(quick.toDouble(), currencyCode)}") },
                    )
                }
            }

            Button(
                onClick = { amount?.let { onConfirm(it) } },
                enabled = amount?.let { it > 0.0 } == true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Add to savings")
            }
        }
    }
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
