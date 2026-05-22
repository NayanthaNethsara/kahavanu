package com.kahavanu.ui.sources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.kahavanu.ui.common.AppSnackbarHost
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.SelectableChip
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeSourcesScreen(
    onBack: () -> Unit,
    viewModel: IncomeSourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        label = "Finance Settings",
        title = "Currencies & Sources",
        onBack = onBack,
        trailing = null,
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
            CurrencySetup(
                primaryCurrency = uiState.primaryCurrency,
                secondaryCurrency = uiState.secondaryCurrency,
                onPrimaryChange = viewModel::onPrimaryCurrencyDraftChange,
                onSecondaryChange = viewModel::onSecondaryCurrencyDraftChange,
                onSave = viewModel::saveCurrencySettings
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Income Sources")
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.openSheet() }
                            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Source",
                            tint = RawColors.Emerald.Emerald600,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Add",
                            color = RawColors.Emerald.Emerald600,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IncomeSourcesList(
                    sources = uiState.sources,
                    onEdit = viewModel::startEdit,
                    onDelete = viewModel::showDeleteConfirmation
                )
            }
            
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
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        uiState.sourceToDelete?.let { source ->
            DeleteConfirmationDialog(
                sourceName = source.name,
                onConfirm = { viewModel.deleteSource(source) },
                onDismiss = viewModel::dismissDeleteConfirmation
            )
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
private fun IncomeSourcesList(
    sources: List<IncomeSource>,
    onEdit: (IncomeSource) -> Unit,
    onDelete: (IncomeSource) -> Unit,
) {
    if (sources.isEmpty()) {
        Text(
            text = "No sources yet. Add your first source below.",
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.sm,
            color = TextSecondary,
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = KahavanuShapes.large,
            color = Color.White.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                sources.forEachIndexed { index, source ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = RawColors.Slate.Slate900.copy(alpha = 0.06f),
                            thickness = 1.dp
                        )
                    }
                    IncomeSourceRow(
                        source = source,
                        onEdit = { onEdit(source) },
                        onDelete = { onDelete(source) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IncomeSourceRow(
    source: IncomeSource,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val style = resolveSourceStyle(source.name, source.types)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onEdit,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.iconColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = style.subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete",
                tint = RawColors.Red.Red600,
                modifier = Modifier.size(20.dp),
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
private fun TypeToggleChip(
    type: IncomeSourceType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectableChip(
        text = type.label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        height = 56.dp,
        shape = KahavanuShapes.large,
        selectedBackgroundColor = RawColors.Emerald.Emerald400.copy(alpha = 0.12f),
        unselectedBackgroundColor = Color.White.copy(alpha = 0.4f),
        selectedBorderColor = RawColors.Emerald.Emerald300,
        unselectedBorderColor = RawColors.Slate.Slate900.copy(alpha = 0.08f),
        selectedTextColor = RawColors.Emerald.Emerald700,
        unselectedTextColor = TextSecondary,
        textStyle = MaterialTheme.typography.labelMedium,
        fontSize = TextSize.xs,
    )
}



private data class SourceStyle(
    val icon: ImageVector,
    val iconColor: Color,
    val subtitle: String,
)

private fun resolveSourceStyle(name: String, types: Set<IncomeSourceType>): SourceStyle {
    val normName = name.trim().lowercase(Locale.getDefault())
    return when {
        normName.contains("acme") || normName.contains("salary") -> {
            SourceStyle(
                icon = Icons.Outlined.WorkOutline,
                iconColor = RawColors.Blue.Blue800,
                subtitle = "Monthly · LKR 120,000"
            )
        }
        normName.contains("freelance") || normName.contains("web") -> {
            SourceStyle(
                icon = Icons.Outlined.Code,
                iconColor = RawColors.Indigo.Indigo700,
                subtitle = "Avg LKR 45,000/mo"
            )
        }
        normName.contains("adsense") || normName.contains("blog") || normName.contains("public") -> {
            SourceStyle(
                icon = Icons.Outlined.TrendingUp,
                iconColor = RawColors.Amber.Amber800,
                subtitle = "USD payouts"
            )
        }
        normName.contains("crypto") || normName.contains("bitcoin") || normName.contains("p2p") -> {
            SourceStyle(
                icon = Icons.Outlined.CurrencyBitcoin,
                iconColor = RawColors.Amber.Amber600,
                subtitle = "Variable"
            )
        }
        else -> {
            val typeStr = types.joinToString(" · ") { it.label }
            SourceStyle(
                icon = Icons.Outlined.AccountBalanceWallet,
                iconColor = RawColors.Emerald.Emerald700,
                subtitle = typeStr.ifEmpty { "Other source" }
            )
        }
    }
}
