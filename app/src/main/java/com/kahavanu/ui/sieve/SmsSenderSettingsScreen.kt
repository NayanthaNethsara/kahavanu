package com.kahavanu.ui.sieve

import com.kahavanu.ui.theme.extendedColors
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.domain.model.SmsSender
import com.kahavanu.ui.common.AppSnackbarHost
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.NestedSearchField
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.circularIconButton
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmsSenderSettingsScreen(
    onBack: () -> Unit,
    viewModel: SmsSenderSettingsViewModel = hiltViewModel()
) {
    val senders by viewModel.senders.collectAsState()
    var newSenderName by remember { mutableStateOf("") }
    var newSenderSubtitle by remember { mutableStateOf("") }
    var isSheetOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var senderToEdit by remember { mutableStateOf<SmsSender?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredSenders = remember(senders, searchQuery) {
        senders.filter { sender ->
            sender.senderName.contains(searchQuery, ignoreCase = true)
        }
    }

    KahavanuSubScreen(
        label = "SMS Scanning",
        title = "Sender IDs",
        onBack = onBack,
        trailing = {
            IconButton(
                onClick = {
                    senderToEdit = null
                    newSenderName = ""
                    newSenderSubtitle = ""
                    isSheetOpen = true
                },
                modifier = Modifier.circularIconButton(
                    backgroundColor = MaterialTheme.extendedColors.brandAccent,
                    borderColor = Color.Transparent,
                    borderWidth = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Sender",
                    tint = Color.White
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacing.extraLarge,
                    end = Spacing.extraLarge,
                    bottom = Spacing.large
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            NestedSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search senders...",
                modifier = Modifier.fillMaxWidth()
            )

            if (filteredSenders.isEmpty()) {
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
                            text = if (searchQuery.isEmpty()) "No authorized SMS senders set" else "No matching senders found",
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        filteredSenders.forEachIndexed { index, sender ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    thickness = 0.5.dp
                                )
                            }
                            SmsSenderRowItem(
                                sender = sender,
                                onToggle = { isEnabled -> viewModel.toggleSender(sender.id, isEnabled) },
                                onLongClick = {
                                    senderToEdit = sender
                                    newSenderName = sender.senderName
                                    newSenderSubtitle = sender.subtitle
                                    isSheetOpen = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    isSheetOpen = false
                    newSenderName = ""
                    newSenderSubtitle = ""
                    senderToEdit = null
                },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null,
                shape = KahavanuShapes.large
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SmsSenderForm(
                        isEditing = senderToEdit != null,
                        nameInput = newSenderName,
                        onNameChange = { newSenderName = it },
                        subtitleInput = newSenderSubtitle,
                        onSubtitleChange = { newSenderSubtitle = it },
                        onSave = {
                            if (newSenderName.isNotBlank()) {
                                val editTarget = senderToEdit
                                if (editTarget != null) {
                                    viewModel.updateSender(editTarget.id, newSenderName, newSenderSubtitle)
                                } else {
                                    viewModel.addSender(newSenderName, newSenderSubtitle)
                                }
                                newSenderName = ""
                                newSenderSubtitle = ""
                                senderToEdit = null
                                isSheetOpen = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sender name cannot be empty")
                                }
                            }
                        },
                        onCancel = {
                            isSheetOpen = false
                            newSenderName = ""
                            newSenderSubtitle = ""
                            senderToEdit = null
                        }
                    )
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (!isSheetOpen) {
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun SmsSenderForm(
    isEditing: Boolean,
    nameInput: String,
    onNameChange: (String) -> Unit,
    subtitleInput: String,
    onSubtitleChange: (String) -> Unit,
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
                text = if (isEditing) "Edit Sender Filter" else "New Sender Filter",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Sender name")
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. PickMe, ComBank") },
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Text(
                    text = "Only SMS messages matching this sender name will suggest transaction entries on your home feed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    lineHeight = 16.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SectionLabel("Subtitle / Description")
                OutlinedTextField(
                    value = subtitleInput,
                    onValueChange = onSubtitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Taxi Service, Commercial Bank") },
                    singleLine = true,
                    shape = KahavanuShapes.large,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSave()
                        }
                    )
                )
                Text(
                    text = "A friendly label to display under the sender name. If left blank, it will automatically resolve if it matches a known institution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    lineHeight = 16.sp
                )
            }
        }

        PrimaryActionButton(
            text = if (isEditing) "Update Sender Filter" else "Add Sender Filter",
            enabled = true,
            onClick = onSave
        )

        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}

@Composable
private fun SmsSenderRowItem(
    sender: SmsSender,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconColor = if (sender.isEnabled) MaterialTheme.extendedColors.brandAccent else MaterialTheme.extendedColors.textTertiary
        val iconBackground = if (sender.isEnabled) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackground, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sender.senderName.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium,
            fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sender.subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Switch(
            checked = sender.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.extendedColors.brandAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            ),
            modifier = Modifier.scale(0.7f)
        )
    }
}
