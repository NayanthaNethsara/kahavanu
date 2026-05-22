package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.AccentExpense
import com.kahavanu.ui.theme.AccentExpenseBorder
import com.kahavanu.ui.theme.AccentExpenseSoft
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.SurfaceCard
import com.kahavanu.ui.theme.SurfaceIconBorder
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val expenseCategories = listOf(
    CategoryUi("Food", Icons.Outlined.LocalPizza),
    CategoryUi("Transport", Icons.Outlined.LocalTaxi),
    CategoryUi("Utilities", Icons.Outlined.Bolt),
    CategoryUi("Shopping", Icons.Outlined.ShoppingCart),
    CategoryUi("Health", Icons.Outlined.HealthAndSafety),
    CategoryUi("Fun", Icons.Outlined.SportsEsports),
    CategoryUi("Rent", Icons.Outlined.Home),
    CategoryUi("Other", Icons.Outlined.Wallet),
)

private data class CategoryUi(
    val label: String,
    val icon: ImageVector,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpenseLogScreen(
    onBack: () -> Unit,
    onLogged: () -> Unit,
    viewModel: ExpenseLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    }
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(uiState.didSubmitSuccessfully) {
        if (uiState.didSubmitSuccessfully) {
            viewModel.onSubmitHandled()
            onLogged()
        }
    }

    if (uiState.isDatePickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.spentDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli() ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerOpenChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = pickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onDateChange(selectedDate)
                        }
                        viewModel.onDatePickerOpenChange(false)
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDatePickerOpenChange(false) },
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    KahavanuSubScreen(
        label = "Add Expense",
        title = "Track your spending",
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.extraLarge, vertical = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Category")
                expenseCategories.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        rowItems.forEach { category ->
                            CategoryButton(
                                category = category,
                                selected = uiState.category == category.label,
                                onClick = { viewModel.onCategoryChange(category.label) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Amount")
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    placeholder = {
                        Text("0", fontSize = 34.sp, lineHeight = 40.sp, color = TextSecondary.copy(alpha = 0.8f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 34.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                    ),
                    supportingText = {
                        Text(
                            text = uiState.currencyCode,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Merchant / Description")
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Keells, Uber, Netflix") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Payment Method")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    PaymentMethodButton(
                        icon = Icons.Outlined.Wallet,
                        label = "Cash",
                        selected = uiState.paymentMethod == "Cash",
                        onClick = { viewModel.onPaymentMethodChange("Cash") },
                        modifier = Modifier.weight(1f),
                    )
                    PaymentMethodButton(
                        icon = Icons.Outlined.CreditCard,
                        label = "Card",
                        selected = uiState.paymentMethod == "Card",
                        onClick = { viewModel.onPaymentMethodChange("Card") },
                        modifier = Modifier.weight(1f),
                    )
                    PaymentMethodButton(
                        icon = Icons.Outlined.PhoneAndroid,
                        label = "Digital",
                        selected = uiState.paymentMethod == "Digital",
                        onClick = { viewModel.onPaymentMethodChange("Digital") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Date")
                OutlinedTextField(
                    value = uiState.spentDate?.format(dateFormatter).orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.onDatePickerOpenChange(true) },
                        ),
                    placeholder = { Text("Select a date") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pick date",
                            tint = TextSecondary,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                FieldLabel("Notes (Optional)")
                OutlinedTextField(
                    value = uiState.notes.orEmpty(),
                    onValueChange = viewModel::onNotesChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    placeholder = { Text("Add any additional details...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors(),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentExpenseSoft, RoundedCornerShape(16.dp))
                    .border(0.7.dp, AccentExpenseBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = Spacing.medium, vertical = 14.dp),
            ) {
                Text(
                    text = "This expense will be categorized under ${uiState.category}. You can view and manage all your expenses from the expenses tab.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AccentExpense,
                )
            }

            Button(
                onClick = viewModel::logExpense,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentExpense,
                    contentColor = Color.White,
                    disabledContainerColor = AccentExpense.copy(alpha = 0.5f),
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(
                    text = if (uiState.isSaving) "Adding..." else "Add Expense",
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.55).sp,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.huge))
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 13.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun CategoryButton(
    category: CategoryUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) AccentExpense else SurfaceIconBorder
    val containerColor = if (selected) AccentExpenseSoft else SurfaceCard
    val iconContainerColor = if (selected) AccentExpense else MaterialTheme.colorScheme.surfaceVariant
    val iconColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (selected) AccentExpense else TextSecondary

    Box(
        modifier = modifier
            .height(90.dp)
            .background(containerColor, RoundedCornerShape(16.dp))
            .border(0.7.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.small, vertical = Spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainerColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = textColor,
            )
        }
    }
}

@Composable
private fun PaymentMethodButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else SurfaceIconBorder
    val containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary else TextSecondary

    Box(
        modifier = modifier
            .height(66.dp)
            .background(containerColor, RoundedCornerShape(14.dp))
            .border(0.7.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = textColor,
            )
        }
    }
}
