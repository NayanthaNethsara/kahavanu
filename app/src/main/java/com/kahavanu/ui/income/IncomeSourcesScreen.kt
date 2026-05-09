package com.kahavanu.ui.income

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import com.kahavanu.ui.income.components.CircularIconButton
import com.kahavanu.ui.income.components.CurrencyDropdown
import com.kahavanu.ui.income.components.CurrencyToggle
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
import com.kahavanu.ui.theme.TextSize
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeSourcesScreen(
    onBack: () -> Unit,
    viewModel: IncomeSourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                IncomeSourcesHeader(
                    onBack = onBack,
                    onAdd = viewModel::openSheet
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                    )
                }

                if (uiState.successMessage != null) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = RawColors.Emerald.Emerald600,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                    )
                }

                CurrencySetup(
                    primaryCurrency = uiState.primaryCurrency,
                    secondaryCurrency = uiState.secondaryCurrency,
                    primaryDraft = uiState.primaryCurrencyDraft,
                    secondaryDraft = uiState.secondaryCurrencyDraft,
                    isEditing = uiState.isCurrencyEditing,
                    onStartEdit = viewModel::startCurrencyEdit,
                    onCancelEdit = viewModel::cancelCurrencyEdit,
                    onSave = viewModel::saveCurrencySettings,
                    onPrimaryChange = viewModel::onPrimaryCurrencyDraftChange,
                    onSecondaryChange = viewModel::onSecondaryCurrencyDraftChange
                )

                IncomeSourcesList(
                    sources = uiState.sources,
                    onEdit = viewModel::startEdit,
                    onDelete = viewModel::showDeleteConfirmation
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
                    IncomeSourceForm(
                        nameInput = uiState.nameInput,
                        selectedTypes = uiState.selectedTypes,
                        isSaving = uiState.isSaving,
                        editingSourceId = uiState.editingSourceId,
                        onNameChange = viewModel::onNameChange,
                        onTypeToggle = viewModel::onTypeToggle,
                        onSave = viewModel::saveSource,
                        onCancel = viewModel::closeSheet
                    )
                }
            }

            uiState.sourceToDelete?.let { source ->
                DeleteConfirmationDialog(
                    sourceName = source.name,
                    onConfirm = { viewModel.deleteSource(source) },
                    onDismiss = viewModel::dismissDeleteConfirmation
                )
            }
        }
    }
}

@Composable
private fun IncomeSourcesHeader(
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
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
                    text = "INCOME SOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                    letterSpacing = 0.72.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Customize your sources",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.lg,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.8).sp,
                    color = TextPrimary,
                )
            }
        }

        CircularIconButton(
            icon = Icons.Outlined.Add,
            contentDescription = "Add Source",
            onClick = onAdd,
        )
    }
}

@Composable
private fun IncomeSourcesList(
    sources: List<IncomeSource>,
    onEdit: (IncomeSource) -> Unit,
    onDelete: (IncomeSource) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SectionLabel("Your sources")
        if (sources.isEmpty()) {
            Text(
                text = "No sources yet. Add your first source below.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.sm,
                color = TextSecondary,
            )
        } else {
            sources.forEach { source ->
                IncomeSourceRow(
                    source = source,
                    onEdit = { onEdit(source) },
                    onDelete = { onDelete(source) },
                )
            }
        }
    }
}

@Composable
private fun IncomeSourceRow(
    source: IncomeSource,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.5f), KahavanuShapes.large)
            .border(1.dp, RawColors.Slate.Slate200.copy(alpha = 0.6f), KahavanuShapes.large)
            .padding(Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(RawColors.Slate.Slate100, KahavanuShapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = sourceIconFor(source.name),
                contentDescription = null,
                tint = RawColors.Slate.Slate600,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                fontSize = TextSize.base,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                source.types.forEach { type ->
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = RawColors.Emerald.Emerald700,
                        modifier = Modifier
                            .background(RawColors.Emerald.Emerald50, CircleShape)
                            .border(0.5.dp, RawColors.Emerald.Emerald200, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            IconButtonSmall(
                icon = Icons.Outlined.Edit,
                contentDescription = "Edit",
                onClick = onEdit,
                tint = RawColors.Slate.Slate500,
            )
            IconButtonSmall(
                icon = Icons.Outlined.Delete,
                contentDescription = "Delete",
                onClick = onDelete,
                tint = RawColors.Red.Red400,
            )
        }
    }
}

@Composable
private fun IncomeSourceForm(
    nameInput: String,
    selectedTypes: Set<IncomeSourceType>,
    isSaving: Boolean,
    editingSourceId: Long?,
    onNameChange: (String) -> Unit,
    onTypeToggle: (IncomeSourceType) -> Unit,
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
                text = if (editingSourceId == null) "New Source" else "Edit Source",
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextSize.lg,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Source name")
            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Salary, Side project") },
                singleLine = true,
                shape = KahavanuShapes.large,
                colors = textFieldColors(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SectionLabel("Supported types")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                IncomeSourceType.values().forEach { type ->
                    TypeToggleChip(
                        type = type,
                        selected = selectedTypes.contains(type),
                        onClick = { onTypeToggle(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        PrimaryActionButton(
            text = if (isSaving) "Saving..." else "Confirm",
            enabled = !isSaving,
            onClick = onSave,
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}

@Composable
private fun DeleteConfirmationDialog(
    sourceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Source?") },
        text = { Text("Are you sure you want to delete '$sourceName'? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = RawColors.Red.Red500)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = KahavanuShapes.large
    )
}

@Composable
private fun CurrencySetup(
    primaryCurrency: CurrencyOption,
    secondaryCurrency: CurrencyOption,
    primaryDraft: CurrencyOption,
    secondaryDraft: CurrencyOption,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onPrimaryChange: (CurrencyOption) -> Unit,
    onSecondaryChange: (CurrencyOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("Currency settings")
            if (!isEditing) {
                TextButton(onClick = onStartEdit) {
                    Text(
                        text = "Edit",
                        color = RawColors.Emerald.Emerald600,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = TextSize.sm,
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    TextButton(onClick = onCancelEdit) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = TextSize.sm,
                        )
                    }
                    TextButton(onClick = onSave) {
                        Text(
                            text = "Save",
                            color = RawColors.Emerald.Emerald600,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = TextSize.sm,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.5f), KahavanuShapes.large)
                .border(1.dp, RawColors.Slate.Slate200.copy(alpha = 0.6f), KahavanuShapes.large)
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    Text(
                        text = "Active configuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Primary is ${primaryCurrency.code} (${primaryCurrency.symbol}) and secondary is ${secondaryCurrency.code} (${secondaryCurrency.symbol})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = TextSize.sm,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    Text(
                        text = "Primary",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    CurrencyDropdown(
                        selected = primaryDraft,
                        onSelect = onPrimaryChange
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    Text(
                        text = "Secondary",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    CurrencyDropdown(
                        selected = secondaryDraft,
                        onSelect = onSecondaryChange
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonSmall(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TypeToggleChip(
    type: IncomeSourceType,
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
            .height(84.dp)
            .background(backgroundColor, KahavanuShapes.large)
            .border(1.dp, borderColor, KahavanuShapes.large)
            .clip(KahavanuShapes.large)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = when (type) {
                    IncomeSourceType.ONE_TIME -> Icons.Outlined.CalendarMonth
                    IncomeSourceType.RECURRENT -> Icons.Outlined.Autorenew
                    IncomeSourceType.PENDING -> Icons.Outlined.Schedule
                },
                contentDescription = null,
                tint = if (selected) RawColors.Emerald.Emerald600 else RawColors.Slate.Slate400,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = TextSize.xs,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) RawColors.Emerald.Emerald700 else TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
