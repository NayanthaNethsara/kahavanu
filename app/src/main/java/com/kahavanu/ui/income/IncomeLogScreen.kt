package com.kahavanu.ui.income

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import android.database.Cursor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondaryEmerald
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IncomeLogScreen(
    onBack: () -> Unit,
    onLogged: () -> Unit,
    onManageSources: () -> Unit,
    viewModel: IncomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    }
    val dateLabel = uiState.receivedDate?.format(dateFormatter).orEmpty()

    val context = LocalContext.current
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = cursor.getString(nameIndex)
                    viewModel.onContactSelected(name)
                }
            }
        }
    }

    if (uiState.isDatePickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.receivedDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerOpenChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.onDateChange(selectedDate)
                        } else {
                            viewModel.onDatePickerOpenChange(false)
                        }
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDatePickerOpenChange(false) }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onLogged()
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
                    RawColors.Emerald.Emerald400.copy(alpha = 0.16f),
                    RawColors.Emerald.Emerald800.copy(alpha = 0.08f),
                    Color.Transparent,
                ),
            )
            GradientBlob(
                modifier = Modifier.offset(x = 170.dp, y = 284.dp),
                size = 320.dp,
                colors = listOf(
                    RawColors.Emerald.Emerald400.copy(alpha = 0.1f),
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
                        top = 48.dp,
                        bottom = Spacing.large,
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                TopBar(
                    onBack = onBack,
                    title = "Track your earnings",
                    onManageSources = onManageSources,
                )

                IncomeTypeSection(
                    selectedType = uiState.incomeType,
                    onTypeSelected = viewModel::onIncomeTypeChange,
                )

                IncomeSourceSection(
                    sources = uiState.sources,
                    selectedSourceId = uiState.selectedSourceId,
                    selectedType = uiState.incomeType,
                    onSourceSelected = viewModel::onSourceChange,
                )

                LabeledTextField(
                    label = "Client / Description",
                    value = uiState.clientDescription,
                    placeholder = "e.g., ACME Corp, Freelance project",
                    onValueChange = viewModel::onClientDescriptionChange,
                    trailingIcon = if (uiState.incomeType == IncomeSourceType.ONE_TIME) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = "Tag Contact",
                                tint = RawColors.Slate.Slate500,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { contactPickerLauncher.launch(null) }
                                    .padding(8.dp)
                            )
                        }
                    } else null
                )

                AmountSection(
                    amount = uiState.amount,
                    currency = uiState.currency,
                    onAmountChange = viewModel::onAmountChange,
                    onCurrencyChange = viewModel::onCurrencyChange,
                )

                if (uiState.incomeType == IncomeSourceType.RECURRENT) {
                    FrequencySection(
                        selected = uiState.frequency,
                        onSelect = viewModel::onFrequencyChange
                    )
                }

                DateSection(
                    label = if (uiState.incomeType == IncomeSourceType.RECURRENT) "Recurrence Start" else "Date Received",
                    dateLabel = dateLabel,
                    onOpenDatePicker = { viewModel.onDatePickerOpenChange(true) },
                )

                InfoBanner(text = infoTextFor(uiState.incomeType))

                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                    )
                }

                val successMessage = uiState.successMessage
                if (successMessage != null) {
                    Text(
                        text = successMessage,
                        color = TextPrimaryEmerald,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                    )
                }

                PrimaryActionButton(
                    text = if (uiState.isSaving) "Logging..." else "Log ${uiState.incomeType.label} Income",
                    enabled = !uiState.isSaving,
                    onClick = viewModel::logIncome,
                )

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    title: String,
    onManageSources: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
            Spacer(modifier = Modifier.width(Spacing.medium))
            Column {
                Text(
                    text = "LOG INCOME",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                    letterSpacing = 0.72.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.lg,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.8).sp,
                    color = TextPrimary,
                )
            }
        }
        CircularIconButton(
            icon = Icons.Outlined.Settings,
            contentDescription = "Manage income sources",
            onClick = onManageSources,
        )
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(6.dp, CircleShape)
            .background(Color.White.copy(alpha = 0.8f), CircleShape)
            .border(0.7.dp, RawColors.Slate.Slate200.copy(alpha = 0.7f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun IncomeTypeSection(
    selectedType: IncomeSourceType,
    onTypeSelected: (IncomeSourceType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Income Type")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            IncomeTypeCard(
                type = IncomeSourceType.ONE_TIME,
                icon = Icons.Outlined.CalendarMonth,
                selected = selectedType == IncomeSourceType.ONE_TIME,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(IncomeSourceType.ONE_TIME) },
            )
            IncomeTypeCard(
                type = IncomeSourceType.RECURRENT,
                icon = Icons.Outlined.Autorenew,
                selected = selectedType == IncomeSourceType.RECURRENT,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(IncomeSourceType.RECURRENT) },
            )
            IncomeTypeCard(
                type = IncomeSourceType.PENDING,
                icon = Icons.Outlined.Schedule,
                selected = selectedType == IncomeSourceType.PENDING,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(IncomeSourceType.PENDING) },
            )
        }
    }
}

@Composable
private fun IncomeTypeCard(
    type: IncomeSourceType,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        RawColors.Emerald.Emerald400.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }
    val borderColor = if (selected) {
        RawColors.Emerald.Emerald300
    } else {
        RawColors.Slate.Slate900.copy(alpha = 0.08f)
    }
    
    Box(
        modifier = modifier
            .height(123.dp)
            .background(backgroundColor, KahavanuShapes.large)
            .border(1.dp, borderColor, KahavanuShapes.large)
            .clip(KahavanuShapes.large)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) RawColors.Emerald.Emerald500 else RawColors.Slate.Slate900.copy(alpha = 0.06f),
                        KahavanuShapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else RawColors.Slate.Slate500,
                    modifier = Modifier.size(20.dp),
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = TextSize.sm,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) RawColors.Emerald.Emerald600 else TextPrimary,
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun IncomeSourceSection(
    sources: List<IncomeSource>,
    selectedSourceId: Long?,
    selectedType: IncomeSourceType,
    onSourceSelected: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Source")
        if (sources.isEmpty()) {
            Text(
                text = "No sources yet. Add one from settings.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.sm,
                color = RawColors.Slate.Slate500,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                contentPadding = PaddingValues(horizontal = Spacing.extraSmall),
            ) {
                items(sources, key = { it.id }) { source ->
                    val isSelected = selectedSourceId == source.id
                    val isEnabled = source.types.contains(selectedType)
                    SourceChip(
                        source = source,
                        icon = sourceIconFor(source.name),
                        selected = isSelected,
                        enabled = isEnabled,
                        onClick = { if (isEnabled) onSourceSelected(source.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceChip(
    source: IncomeSource,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        RawColors.Emerald.Emerald400.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }
    val borderColor = if (selected) {
        RawColors.Emerald.Emerald300
    } else {
        RawColors.Slate.Slate900.copy(alpha = 0.08f)
    }

    Box(
        modifier = Modifier
            .height(84.dp)
            .width(100.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .background(backgroundColor, KahavanuShapes.large)
            .border(1.dp, borderColor, KahavanuShapes.large)
            .clip(KahavanuShapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (selected) RawColors.Emerald.Emerald500 else RawColors.Slate.Slate900.copy(alpha = 0.06f),
                        KahavanuShapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else RawColors.Slate.Slate500,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = source.name,
                style = MaterialTheme.typography.labelSmall,
                fontSize = TextSize.xs,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) RawColors.Emerald.Emerald700 else TextSecondary,
            )
        }
    }
}

private fun sourceIconFor(name: String): ImageVector {
    return when (name.trim().lowercase(Locale.getDefault())) {
        "salary" -> Icons.Outlined.AccountBalanceWallet
        "freelance" -> Icons.Outlined.WorkOutline
        "adsense" -> Icons.Outlined.Public
        "crypto" -> Icons.Outlined.CurrencyBitcoin
        else -> Icons.Outlined.AccountBalanceWallet
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = KahavanuShapes.large,
            trailingIcon = trailingIcon,
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun AmountSection(
    amount: String,
    currency: CurrencyOption,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (CurrencyOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Amount")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
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
            CurrencyToggle(
                selected = currency,
                onSelect = onCurrencyChange,
            )
        }
    }
}

@Composable
private fun CurrencyToggle(
    selected: CurrencyOption,
    onSelect: (CurrencyOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .width(110.dp)
            .height(52.dp)
            .background(
                RawColors.Slate.Slate900.copy(alpha = 0.06f),
                RoundedCornerShape(14.dp),
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CurrencyOptionButton(
            text = CurrencyOption.LKR.code,
            selected = selected == CurrencyOption.LKR,
            onClick = { onSelect(CurrencyOption.LKR) },
            modifier = Modifier.weight(1f),
        )
        CurrencyOptionButton(
            text = CurrencyOption.USD.code,
            selected = selected == CurrencyOption.USD,
            onClick = { onSelect(CurrencyOption.USD) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CurrencyOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (selected) RawColors.Emerald.Emerald500 else Color.Transparent,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary,
        )
    }
}

@Composable
private fun DateSection(
    label: String,
    dateLabel: String,
    onOpenDatePicker: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel(label)
        OutlinedTextField(
            value = dateLabel,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(KahavanuShapes.large)
                .clickable(onClick = onOpenDatePicker),
            placeholder = { Text("Select date") },
            singleLine = true,
            readOnly = true,
            shape = KahavanuShapes.large,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = RawColors.Slate.Slate500,
                )
            },
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun FrequencySection(
    selected: RecurrenceFrequency,
    onSelect: (RecurrenceFrequency) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        SectionLabel("Frequency")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            RecurrenceFrequency.values().forEach { freq ->
                FrequencyChip(
                    label = freq.label,
                    selected = selected == freq,
                    onClick = { onSelect(freq) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FrequencyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (selected) RawColors.Emerald.Emerald500.copy(alpha = 0.12f) else RawColors.Slate.Slate900.copy(alpha = 0.04f),
                KahavanuShapes.medium
            )
            .border(
                1.dp,
                if (selected) RawColors.Emerald.Emerald500 else RawColors.Slate.Slate900.copy(alpha = 0.08f),
                KahavanuShapes.medium
            )
            .clip(KahavanuShapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.xs,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) RawColors.Emerald.Emerald700 else TextSecondary
        )
    }
}

@Composable
private fun InfoBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RawColors.Emerald.Emerald500.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.7.dp,
            RawColors.Emerald.Emerald500.copy(alpha = 0.2f),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.sm,
            color = RawColors.Emerald.Emerald700,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(elevation = 18.dp, spotColor = Color.Black.copy(alpha = 0.25f), shape = KahavanuShapes.large)
            .background(
                if (enabled) gradient else Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                KahavanuShapes.large
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = KahavanuShapes.large
            )
            .clip(KahavanuShapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontSize = TextSize.base,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.23).sp,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontSize = TextSize.sm,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
    )
}

@Composable
private fun GradientBlob(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    colors: List<Color>,
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(80.dp)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape,
            ),
    )
}

@Composable
private fun textFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White.copy(alpha = 0.7f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
        focusedBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.9f),
        unfocusedBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.9f),
        focusedTextColor = RawColors.Slate.Slate900,
        unfocusedTextColor = RawColors.Slate.Slate900,
        focusedPlaceholderColor = RawColors.Slate.Slate500.copy(alpha = 0.7f),
        unfocusedPlaceholderColor = RawColors.Slate.Slate500.copy(alpha = 0.7f),
        disabledBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.6f),
        disabledContainerColor = Color.White.copy(alpha = 0.6f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color.White.copy(alpha = 0.7f),
    )

private fun infoTextFor(type: IncomeSourceType): String = when (type) {
    IncomeSourceType.ONE_TIME ->
        "One-time income is for single payments you've already received. It will appear in your income log immediately."
    IncomeSourceType.RECURRENT ->
        "Recurrent income tracks regular payments. Keep it updated to forecast monthly earnings."
    IncomeSourceType.PENDING ->
        "Pending income is expected in the future. It will show up once you mark it as received."
}
