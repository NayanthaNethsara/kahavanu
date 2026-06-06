package com.kahavanu.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.AppSnackbarHost
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.AppTextButton
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.income.components.CurrencyDropdown
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.circularIconButton
import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.util.goalCategoryColor
import com.kahavanu.ui.util.goalCategoryIcon
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGoalsScreen(
    onBack: () -> Unit,
    onAddGoal: () -> Unit,
    viewModel: ManageGoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    var goalToDelete by remember { mutableStateOf<GoalEntry?>(null) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = {
                Text(
                    text = "Delete Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            },
            text = {
                Text(
                    text = "Delete '${goal.title}'? Saved progress and adjustment history will also be removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                AppTextButton(
                    text = "Delete",
                    destructive = true,
                    onClick = {
                        viewModel.deleteGoal(goal.id)
                        goalToDelete = null
                    },
                )
            },
            dismissButton = {
                AppTextButton(text = "Cancel", onClick = { goalToDelete = null }, muted = true)
            },
            containerColor = Color.White,
            shape = KahavanuShapes.large,
        )
    }

    KahavanuSubScreen(
        label = "GOALS",
        title = "Manage your targets",
        onBack = onBack,
        trailing = {
            IconButton(
                onClick = onAddGoal,
                modifier = Modifier.circularIconButton(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Goal",
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(
                    start = Spacing.extraLarge,
                    end = Spacing.extraLarge,
                    bottom = Spacing.large,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            GoalsTotalsCard(
                activeAndBacklog = listOfNotNull(uiState.activeGoal) + uiState.backlogGoals,
                completed = uiState.completedGoals,
                currency = uiState.primaryCurrency,
            )

            GoalGroup(
                title = "Active",
                subtitle = "Featured on Home & Goals",
                goals = listOfNotNull(uiState.activeGoal),
                currency = uiState.primaryCurrency,
                emptyText = "No active goal yet. Promote one from your backlog.",
                onEdit = viewModel::openEditSheet,
                onDelete = { goalToDelete = it },
                onMakeActive = null,
            )

            GoalGroup(
                title = "Backlog",
                subtitle = "Lined up next",
                goals = uiState.backlogGoals,
                currency = uiState.primaryCurrency,
                emptyText = "No backlog goals.",
                onEdit = viewModel::openEditSheet,
                onDelete = { goalToDelete = it },
                onMakeActive = viewModel::setActiveGoal,
            )

            GoalGroup(
                title = "Completed",
                subtitle = "Archived achievements",
                goals = uiState.completedGoals,
                currency = uiState.primaryCurrency,
                emptyText = "No completed goals yet.",
                onEdit = viewModel::openEditSheet,
                onDelete = { goalToDelete = it },
                onMakeActive = null,
            )

            Spacer(modifier = Modifier.height(Spacing.large))
        }

        if (uiState.isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeSheet,
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null,
                shape = KahavanuShapes.large,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    EditGoalForm(
                        title = uiState.titleInput,
                        targetAmount = uiState.targetAmountInput,
                        currentAmount = uiState.currentAmountInput,
                        currency = uiState.currencyInput,
                        availableCurrencies = uiState.availableCurrencies,
                        category = uiState.categoryInput,
                        targetDateLabel = uiState.targetDateInput?.format(
                            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()),
                        ).orEmpty(),
                        isSaving = uiState.isSaving,
                        onTitleChange = viewModel::onTitleChange,
                        onTargetAmountChange = viewModel::onTargetAmountChange,
                        onCurrentAmountChange = viewModel::onCurrentAmountChange,
                        onCurrencyChange = viewModel::onCurrencyChange,
                        onCategoryChange = viewModel::onCategoryChange,
                        onOpenDatePicker = { viewModel.onDatePickerOpenChange(true) },
                        onClearDate = viewModel::clearTargetDate,
                        onSave = viewModel::saveEdits,
                        onCancel = viewModel::closeSheet,
                    )
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        if (uiState.isDatePickerOpen) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.targetDateInput
                    ?.atStartOfDay(ZoneOffset.UTC)
                    ?.toInstant()
                    ?.toEpochMilli() ?: System.currentTimeMillis(),
            )
            DatePickerDialog(
                onDismissRequest = { viewModel.onDatePickerOpenChange(false) },
                confirmButton = {
                    AppTextButton(
                        text = "OK",
                        onClick = {
                            val millis = pickerState.selectedDateMillis
                            if (millis != null) {
                                val date = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                viewModel.onDateChange(date)
                            } else {
                                viewModel.onDatePickerOpenChange(false)
                            }
                        },
                    )
                },
                dismissButton = {
                    AppTextButton(text = "Cancel", onClick = { viewModel.onDatePickerOpenChange(false) })
                },
            ) {
                DatePicker(state = pickerState)
            }
        }

        if (!uiState.isSheetOpen) {
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun GoalsTotalsCard(
    activeAndBacklog: List<GoalEntry>,
    completed: List<GoalEntry>,
    currency: CurrencyOption,
) {
    val totalTarget = activeAndBacklog.sumOf { it.targetAmount }
    val totalSaved = activeAndBacklog.sumOf { it.currentAmount }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Text(
                text = "OVERVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = formatAmount(totalSaved, currency.code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = formatAmount(totalTarget, currency.code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
            }
            Text(
                text = "${activeAndBacklog.size} in flight · ${completed.size} completed",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
            )
        }
    }
}

@Composable
private fun GoalGroup(
    title: String,
    subtitle: String,
    goals: List<GoalEntry>,
    currency: CurrencyOption,
    emptyText: String,
    onEdit: (GoalEntry) -> Unit,
    onDelete: (GoalEntry) -> Unit,
    onMakeActive: ((String) -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
            )
        }

        if (goals.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.huge),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )
                }
            }
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ) {
                goals.forEachIndexed { index, goal ->
                    GoalRow(
                        goal = goal,
                        currency = currency,
                        onEdit = { onEdit(goal) },
                        onDelete = { onDelete(goal) },
                        onMakeActive = onMakeActive?.let { { it(goal.id) } },
                    )
                    if (index < goals.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalRow(
    goal: GoalEntry,
    currency: CurrencyOption,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMakeActive: (() -> Unit)?,
) {
    val color = goalCategoryColor(goal.category)
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).roundToInt().coerceIn(0, 100)
    } else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = goalCategoryIcon(goal.category),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1,
                )
                if (goal.isActive) {
                    Spacer(modifier = Modifier.width(Spacing.extraSmall))
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Active goal",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatAmount(goal.currentAmount, currency.code)} of ${formatAmount(goal.targetAmount, currency.code)} · $progressPercent%",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextTertiary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (onMakeActive != null) {
                IconChip(
                    icon = Icons.Outlined.StarOutline,
                    tint = MaterialTheme.colorScheme.tertiary,
                    background = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                    description = "Make active",
                    onClick = onMakeActive,
                )
            }
            IconChip(
                icon = Icons.Outlined.Edit,
                tint = Color(0xFF0F172A),
                background = Color(0x0F0F172A),
                description = "Edit",
                onClick = onEdit,
            )
            IconChip(
                icon = Icons.Outlined.DeleteOutline,
                tint = Color(0xFFDC2626),
                background = Color(0x14DC2626),
                description = "Delete",
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(background, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun EditGoalForm(
    title: String,
    targetAmount: String,
    currentAmount: String,
    currency: CurrencyOption,
    availableCurrencies: List<CurrencyOption>,
    category: GoalCategory,
    targetDateLabel: String,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onCurrentAmountChange: (String) -> Unit,
    onCurrencyChange: (CurrencyOption) -> Unit,
    onCategoryChange: (GoalCategory) -> Unit,
    onOpenDatePicker: () -> Unit,
    onClearDate: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Edit Goal",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            AppTextButton(text = "Cancel", onClick = onCancel, muted = true)
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Goal name")
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Europe Trip, Emergency Fund") },
                singleLine = true,
                shape = KahavanuShapes.large,
                colors = textFieldColors(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Category")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                contentPadding = PaddingValues(horizontal = Spacing.extraSmall),
            ) {
                items(GoalCategory.entries) { cat ->
                    CategoryChipSmall(
                        category = cat,
                        selected = cat == category,
                        onClick = { onCategoryChange(cat) },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Target amount")
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = onTargetAmountChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = { Text("0") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors(),
                )
                CurrencyDropdown(
                    selected = currency,
                    onSelect = onCurrencyChange,
                    modifier = Modifier.width(110.dp),
                    options = availableCurrencies,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Already saved")
            OutlinedTextField(
                value = currentAmount,
                onValueChange = onCurrentAmountChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = { Text("0") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = textFieldColors(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Target date")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(KahavanuShapes.large)
                    .clickable(onClick = onOpenDatePicker),
            ) {
                OutlinedTextField(
                    value = targetDateLabel,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("No target date") },
                    singleLine = true,
                    readOnly = true,
                    enabled = false,
                    shape = KahavanuShapes.large,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    },
                    colors = textFieldColors(),
                )
                Box(modifier = Modifier.matchParentSize())
            }
            if (targetDateLabel.isNotBlank()) {
                AppTextButton(text = "Clear target date", onClick = onClearDate, muted = true)
            }
        }

        PrimaryActionButton(
            text = if (isSaving) "Saving..." else "Save Changes",
            enabled = !isSaving && title.isNotBlank() && targetAmount.isNotBlank(),
            onClick = onSave,
        )

        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}

@Composable
private fun CategoryChipSmall(
    category: GoalCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = goalCategoryColor(category)
    val bg = if (selected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val borderColor = if (selected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .height(40.dp)
            .background(bg, KahavanuShapes.medium)
            .border(1.dp, borderColor, KahavanuShapes.medium)
            .clip(KahavanuShapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        ) {
            Icon(
                imageVector = goalCategoryIcon(category),
                contentDescription = null,
                tint = if (selected) color else TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = TextSize.sm,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) color else TextSecondary,
            )
        }
    }
}
