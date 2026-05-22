package com.kahavanu.ui.subscriptions

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHostState
import com.kahavanu.ui.common.AppSnackbarHost
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.domain.model.Subscription
import com.kahavanu.ui.common.AppSegmentedToggle
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.GradientBlob
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.circularIconButton
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.common.KahavanuSubScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubscriptionsScreen(
    onBack: () -> Unit,
    viewModel: ManageSubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    KahavanuSubScreen(
        label = "Recurring Leaks",
        title = "Manage Subscriptions",
        onBack = onBack,
        trailing = {
            IconButton(
                onClick = viewModel::openSheet,
                modifier = Modifier.circularIconButton(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subscription",
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
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
            SpendAnalysisCard(
                totalSpend = uiState.totalMonthlySpend,
                activeCount = uiState.subscriptions.count { !it.isPaused }
            )

            SubscriptionsList(
                subscriptions = uiState.subscriptions,
                onTogglePause = viewModel::toggleSubscriptionPause,
                onDelete = viewModel::deleteSubscription
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
                    SubscriptionForm(
                        nameInput = uiState.nameInput,
                        costInput = uiState.costInput,
                        currencyInput = uiState.currencyInput,
                        frequencyInput = uiState.frequencyInput,
                        nextBillingInput = uiState.nextBillingInput,
                        onNameChange = viewModel::onNameChange,
                        onCostChange = viewModel::onCostChange,
                        onCurrencyChange = viewModel::onCurrencyChange,
                        onFrequencyChange = viewModel::onFrequencyChange,
                        onNextBillingChange = viewModel::onNextBillingChange,
                        onSave = viewModel::addSubscription,
                        onCancel = viewModel::closeSheet
                    )
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (!uiState.isSheetOpen) {
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


@Composable
private fun SpendAnalysisCard(
    totalSpend: Double,
    activeCount: Int
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL MONTHLY SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = TextSecondary,
            fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format("$%.2f", totalSpend),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
            letterSpacing = (-1).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.extendedColors.dangerWashed.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.extendedColors.dangerAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(Spacing.medium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active renewals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "$activeCount active subscriptions",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SubscriptionsList(
    subscriptions: List<Subscription>,
    onTogglePause: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SectionLabel("Your active & paused subscriptions")
        if (subscriptions.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.huge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subscriptions added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ) {
                subscriptions.forEachIndexed { index, sub ->
                    SubscriptionRow(
                        subscription = sub,
                        onToggle = { onTogglePause(sub.id) },
                        onDelete = { onDelete(sub.id) }
                    )
                    if (index < subscriptions.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (subscription.isPaused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.extendedColors.infoWashed,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = if (subscription.isPaused) TextTertiary else MaterialTheme.extendedColors.infoAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (subscription.isPaused) TextSecondary else TextPrimary
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = if (subscription.isPaused) "Paused" else "Active",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subscription.isPaused) MaterialTheme.extendedColors.iconMuted else MaterialTheme.extendedColors.brandText,
                    modifier = Modifier
                        .background(
                            if (subscription.isPaused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.extendedColors.brandWashed,
                            CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Next billing: ${subscription.nextBillingDate}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format("$%.2f", subscription.cost),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (subscription.isPaused) TextSecondary else TextPrimary
            )
            Text(
                text = "/ ${subscription.frequency.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 9.sp,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Switch(
                checked = !subscription.isPaused,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.extendedColors.infoAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent,
                ),
                modifier = Modifier.scale(0.8f)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.extendedColors.dangerAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Scale utility extension for compose Switch inside list
private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size((48 * scale).dp)
)

@Composable
private fun SubscriptionForm(
    nameInput: String,
    costInput: String,
    currencyInput: String,
    frequencyInput: String,
    nextBillingInput: String,
    onNameChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onNextBillingChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Subscription",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Subscription name")
            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Netflix, Spotify") },
                singleLine = true,
                shape = KahavanuShapes.large,
                colors = textFieldColors()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionLabel("Price")
                OutlinedTextField(
                    value = costInput,
                    onValueChange = onCostChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 15.49") },
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
            }

            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionLabel("Frequency")
                AppSegmentedToggle(
                    items = listOf("monthly", "yearly"),
                    selectedItem = frequencyInput.lowercase(),
                    onSelect = onFrequencyChange,
                    labelFor = { if (it == "monthly") "Monthly" else "Yearly" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    height = 56.dp,
                    shape = KahavanuShapes.large,
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    indicatorColor = MaterialTheme.extendedColors.infoAccent,
                    indicatorShadow = 1.dp,
                    selectedTextColor = Color.White,
                    unselectedTextColor = TextSecondary,
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    itemWidth = 80.dp,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Next billing date")
            OutlinedTextField(
                value = nextBillingInput,
                onValueChange = onNextBillingChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. June 15, 2026") },
                singleLine = true,
                shape = KahavanuShapes.large,
                colors = textFieldColors()
            )
        }

        PrimaryActionButton(
            text = "Track Subscription",
            enabled = nameInput.isNotBlank() && costInput.isNotBlank() && nextBillingInput.isNotBlank(),
            onClick = onSave,
        )

        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}
