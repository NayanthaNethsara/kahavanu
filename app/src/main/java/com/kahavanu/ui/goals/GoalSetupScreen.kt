package com.kahavanu.ui.goals

import com.kahavanu.ui.util.categoryIcon
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import com.kahavanu.ui.common.AppSnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.ui.common.GradientBlob
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.income.components.CurrencyDropdown
import com.kahavanu.ui.theme.circularIconButton
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: GoalSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    }
    val dateLabel = uiState.targetDate?.format(dateFormatter).orEmpty()

    if (uiState.isDatePickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.targetDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli() ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerOpenChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onDateChange(date)
                        } else {
                            viewModel.onDatePickerOpenChange(false)
                        }
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDatePickerOpenChange(false) }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            onSaved()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GradientBlob(
                modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
                size = 360.dp,
                colors = listOf(
                    MaterialTheme.extendedColors.brandGlow.copy(alpha = 0.14f),
                    MaterialTheme.extendedColors.brandDark.copy(alpha = 0.07f),
                    Color.Transparent,
                ),
            )
            GradientBlob(
                modifier = Modifier.offset(x = 170.dp, y = 320.dp),
                size = 300.dp,
                colors = listOf(
                    MaterialTheme.extendedColors.utility.copy(alpha = 0.08f),
                    Color.Transparent,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(
                        start = Spacing.extraLarge,
                        end = Spacing.extraLarge,
                        top = 30.dp,
                        bottom = Spacing.large,
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                GoalSetupTopBar(onBack = onBack)

                GoalNameSection(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                )

                GoalCategorySection(
                    selected = uiState.category,
                    onSelect = viewModel::onCategoryChange,
                )

                GoalAmountSection(
                    targetAmount = uiState.targetAmount,
                    currentAmount = uiState.currentAmount,
                    currency = uiState.currency,
                    availableCurrencies = uiState.availableCurrencies,
                    onTargetAmountChange = viewModel::onTargetAmountChange,
                    onCurrentAmountChange = viewModel::onCurrentAmountChange,
                    onCurrencyChange = viewModel::onCurrencyChange,
                )

                GoalTargetDateSection(
                    dateLabel = dateLabel,
                    onOpenDatePicker = { viewModel.onDatePickerOpenChange(true) },
                )

                GoalInfoBanner()



                PrimaryActionButton(
                    text = if (uiState.isSaving) "Saving..." else "Create Goal",
                    enabled = !uiState.isSaving,
                    onClick = viewModel::saveGoal,
                )

                Spacer(modifier = Modifier.height(Spacing.large))
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun GoalSetupTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.circularIconButton(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimaryEmerald,
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column {
            Text(
                text = "SET UP GOAL",
                style = MaterialTheme.typography.labelSmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
                letterSpacing = 0.72.sp,
        )
            Text(
                text = "Define your target",
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextSize.lg,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.8).sp,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun GoalNameSection(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Goal Name")
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Europe Trip, Emergency Fund") },
            singleLine = true,
            shape = KahavanuShapes.large,
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun GoalCategorySection(
    selected: GoalCategory,
    onSelect: (GoalCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Category")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            contentPadding = PaddingValues(horizontal = Spacing.extraSmall),
        ) {
            items(GoalCategory.entries) { category ->
                CategoryChip(
                    category = category,
                    selected = selected == category,
                    onClick = { onSelect(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: GoalCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedBg = MaterialTheme.extendedColors.brandGlow.copy(alpha = 0.12f)
    val unselectedBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val selectedBorder = MaterialTheme.extendedColors.brandGlow
    val unselectedBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .height(44.dp)
            .background(
                if (selected) selectedBg else unselectedBg,
                KahavanuShapes.medium,
            )
            .border(
                1.dp,
                if (selected) selectedBorder else unselectedBorder,
                KahavanuShapes.medium,
            )
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
                imageVector = categoryIcon(category),
                contentDescription = null,
                tint = if (selected) MaterialTheme.extendedColors.brandText else TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.extendedColors.brandText else TextSecondary,
            )
        }
    }
}

@Composable
private fun GoalAmountSection(
    targetAmount: String,
    currentAmount: String,
    currency: CurrencyOption,
    availableCurrencies: List<CurrencyOption>,
    onTargetAmountChange: (String) -> Unit,
    onCurrentAmountChange: (String) -> Unit,
    onCurrencyChange: (CurrencyOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Target Amount")
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
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = TextSize.lg,
                ),
                colors = textFieldColors(),
            )
            CurrencyDropdown(
                selected = currency,
                onSelect = onCurrencyChange,
                modifier = Modifier.width(110.dp),
                options = availableCurrencies,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        SectionLabel("Already Saved (optional)")
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
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = TextSize.lg,
            ),
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun GoalTargetDateSection(
    dateLabel: String,
    onOpenDatePicker: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Target Date (optional)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(KahavanuShapes.large)
                .clickable(onClick = onOpenDatePicker),
        ) {
            OutlinedTextField(
                value = dateLabel,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Select target date") },
                singleLine = true,
                readOnly = true,
                enabled = false,
                shape = KahavanuShapes.large,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.extendedColors.iconMuted,
                    )
                },
                colors = textFieldColors(),
            )
            Box(modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun GoalInfoBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.7.dp,
            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.2f),
        ),
    ) {
        Text(
            text = "Set a clear target and track how much you've saved toward it. You can update progress anytime.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.sm,
            color = MaterialTheme.extendedColors.brandText,
            lineHeight = 18.sp,
        )
    }
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
