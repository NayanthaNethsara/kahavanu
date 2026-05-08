package com.kahavanu.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun IncomeSourcesScreen(
    onBack: () -> Unit,
    viewModel: IncomeSourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.extraLarge, vertical = Spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RawColors.Slate.Slate700,
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Column {
                    Text(
                        text = "INCOME SOURCES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Customize your sources",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState.successMessage != null) {
                Text(
                    text = uiState.successMessage ?: "",
                    color = RawColors.Emerald.Emerald600,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SectionTitle("Your sources")
            if (uiState.sources.isEmpty()) {
                Text(
                    text = "No sources yet. Add your first source below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    uiState.sources.forEach { source ->
                        IncomeSourceRow(
                            source = source,
                            onEdit = { viewModel.startEdit(source) },
                            onDelete = { viewModel.deleteSource(source) },
                        )
                    }
                }
            }

            SectionTitle(
                if (uiState.editingSourceId == null) "Add source" else "Edit source"
            )

            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = viewModel::onNameChange,
                label = { Text("Source name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                IncomeSourceType.values().forEach { type ->
                    TypeToggleChip(
                        type = type,
                        selected = uiState.selectedTypes.contains(type),
                        onClick = { viewModel.onTypeToggle(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                val saveLabel = if (uiState.editingSourceId == null) {
                    "Save source"
                } else {
                    "Update source"
                }
                Button(
                    onClick = viewModel::saveSource,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (uiState.isSaving) "Saving..." else saveLabel)
                }
                if (uiState.editingSourceId != null) {
                    TextButton(
                        onClick = viewModel::cancelEdit,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, RawColors.Slate.Slate200, RoundedCornerShape(16.dp))
            .padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit source",
                        tint = TextSecondary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete source",
                        tint = RawColors.Red.Red500,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            IncomeSourceType.values()
                .filter { source.types.contains(it) }
                .forEach { type ->
                    TypeLabelChip(type)
                }
        }
    }
}

@Composable
private fun TypeToggleChip(
    type: IncomeSourceType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) RawColors.Blue.Blue50 else RawColors.Slate.Slate50,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) RawColors.Blue.Blue200 else RawColors.Slate.Slate200,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) RawColors.Blue.Blue700 else TextSecondary,
            )
        }
    }
}

@Composable
private fun TypeLabelChip(type: IncomeSourceType) {
    BoxedLabel(
        text = type.label,
        background = RawColors.Blue.Blue50,
        border = RawColors.Blue.Blue200,
        textColor = RawColors.Blue.Blue700,
    )
}

@Composable
private fun BoxedLabel(
    text: String,
    background: Color,
    border: Color,
    textColor: Color,
) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .background(background, CircleShape)
            .border(1.dp, border, CircleShape)
            .padding(horizontal = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
    )
}
