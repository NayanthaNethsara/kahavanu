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
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.kahavanu.ui.income.components.CircularIconButton
import com.kahavanu.ui.income.components.CurrencyDropdown
import com.kahavanu.ui.income.components.GradientBlob
import com.kahavanu.ui.income.components.PrimaryActionButton
import com.kahavanu.ui.income.components.SectionLabel
import com.kahavanu.ui.income.components.sourceIconFor
import com.kahavanu.ui.income.components.textFieldColors
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextPrimaryEmerald
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
            val projection = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts._ID
            )
            context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                    
                    var phoneNumber: String? = null
                    if (hasPhone) {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                phoneNumber = phoneCursor.getString(0)
                            }
                        }
                    }
                    viewModel.onContactSaved(name, phoneNumber)
                }
            }
        }
    }

    if (uiState.isDatePickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.receivedDate
                ?.atStartOfDay(ZoneId.systemDefault())
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

                ContactSelectionSection(
                    contacts = uiState.contacts,
                    onContactSelected = viewModel::onContactSelected,
                    onPickContact = { contactPickerLauncher.launch(null) }
                )

                LabeledTextField(
                    label = "Client / Description",
                    value = uiState.clientDescription,
                    placeholder = "e.g., ACME Corp, Freelance project",
                    onValueChange = viewModel::onClientDescriptionChange,
                    trailingIcon = null
                )

                AmountSection(
                    amount = uiState.amount,
                    currency = uiState.currency,
                    onAmountChange = viewModel::onAmountChange,
                    onCurrencyChange = viewModel::onCurrencyChange,
                    currencyOptions = uiState.availableCurrencies,
                )

                if (uiState.incomeType == IncomeSourceType.RECURRENT) {
                    FrequencySection(
                        selected = uiState.frequency,
                        onSelect = viewModel::onFrequencyChange
                    )
                }

                val dateLabelText = when (uiState.incomeType) {
                    IncomeSourceType.RECURRENT -> "Recurrence Start"
                    IncomeSourceType.PENDING -> "Date Expected"
                    else -> "Date Received"
                }

                DateSection(
                    label = dateLabelText,
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
            .height(72.dp)
            .background(backgroundColor, KahavanuShapes.large)
            .border(1.dp, borderColor, KahavanuShapes.large)
            .clip(KahavanuShapes.large)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
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
            .height(56.dp)
            .width(110.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .background(backgroundColor, KahavanuShapes.large)
            .border(1.dp, borderColor, KahavanuShapes.large)
            .clip(KahavanuShapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = source.name,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Bold,
            color = if (selected) RawColors.Emerald.Emerald600 else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = Spacing.small)
        )
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
    currencyOptions: List<CurrencyOption>,
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
            CurrencyDropdown(
                selected = currency,
                onSelect = onCurrencyChange,
                modifier = Modifier.width(110.dp),
                options = currencyOptions
            )
        }
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
            RecurrenceFrequency.entries.forEach { freq ->
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

private fun infoTextFor(type: IncomeSourceType): String = when (type) {
    IncomeSourceType.ONE_TIME ->
        "One-time income is for single payments you've already received. It will appear in your income log immediately."
    IncomeSourceType.RECURRENT ->
        "Recurrent income tracks regular payments. Keep it updated to forecast monthly earnings."
    IncomeSourceType.PENDING ->
        "Pending income is expected in the future. It will show up once you mark it as received."
}

@Composable
private fun ContactSelectionSection(
    contacts: List<com.kahavanu.domain.model.Contact>,
    onContactSelected: (com.kahavanu.domain.model.Contact) -> Unit,
    onPickContact: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("Client / Contact")
            Text(
                text = "Pick from phone",
                style = MaterialTheme.typography.labelSmall,
                color = RawColors.Emerald.Emerald600,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onPickContact() }
            )
        }
        
        if (contacts.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(contacts) { contact ->
                    ContactChip(
                        contact = contact,
                        onClick = { onContactSelected(contact) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactChip(
    contact: com.kahavanu.domain.model.Contact,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = RawColors.Slate.Slate900.copy(alpha = 0.04f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, RawColors.Slate.Slate200.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = RawColors.Slate.Slate500
            )
            Text(
                text = contact.name,
                style = MaterialTheme.typography.labelMedium,
                fontSize = TextSize.xs,
                color = TextPrimary
            )
        }
    }
}
