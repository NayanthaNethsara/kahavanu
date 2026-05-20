package com.kahavanu.ui.sieve

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kahavanu.domain.model.SmsSender
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.GradientBlob
import com.kahavanu.ui.common.PrimaryActionButton
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.circularIconButton
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun SmsSenderSettingsScreen(
    onBack: () -> Unit,
    viewModel: SmsSenderSettingsViewModel = hiltViewModel()
) {
    val senders by viewModel.senders.collectAsState()
    var newSenderName by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            RawColors.Slate.Slate50,
            RawColors.Emerald.Emerald50.copy(alpha = 0.5f),
            RawColors.Slate.Slate100,
        ),
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            GradientBlob(
                modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
                size = 360.dp,
                colors = listOf(
                    RawColors.Emerald.Emerald400.copy(alpha = 0.14f),
                    RawColors.Emerald.Emerald800.copy(alpha = 0.07f),
                    Color.Transparent,
                ),
            )
            GradientBlob(
                modifier = Modifier.offset(x = 170.dp, y = 320.dp),
                size = 300.dp,
                colors = listOf(
                    RawColors.Violet.Violet400.copy(alpha = 0.08f),
                    Color.Transparent,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Spacing.extraLarge,
                            end = Spacing.extraLarge,
                            top = Spacing.large,
                            bottom = Spacing.medium
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.circularIconButton()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryEmerald
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column {
                        Text(
                            text = "SMS FILTERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            letterSpacing = 0.72.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "SMS Sender Filters",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.8).sp,
                            color = TextPrimary
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = Spacing.extraLarge,
                        end = Spacing.extraLarge,
                        top = Spacing.small,
                        bottom = Spacing.large
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White.copy(alpha = 0.9f),
                            borderColor = RawColors.Slate.Slate200.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.large),
                                verticalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                Text(
                                    text = "Add Authorized Sender",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Only SMS messages matching these sender names will suggest transaction entries on your home feed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                                OutlinedTextField(
                                    value = newSenderName,
                                    onValueChange = { newSenderName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. PickMe, ComBank") },
                                    singleLine = true,
                                    shape = KahavanuShapes.large,
                                    colors = textFieldColors(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (newSenderName.isNotBlank()) {
                                                viewModel.addSender(newSenderName)
                                                newSenderName = ""
                                            }
                                        }
                                    )
                                )
                                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                                PrimaryActionButton(
                                    text = "Add Sender Filter",
                                    enabled = newSenderName.isNotBlank(),
                                    onClick = {
                                        viewModel.addSender(newSenderName)
                                        newSenderName = ""
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.extraSmall))
                            SectionLabel("Authorized Senders list (${senders.size})")
                        }
                    }

                    if (senders.isEmpty()) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White.copy(alpha = 0.6f),
                                borderColor = RawColors.Slate.Slate200.copy(alpha = 0.4f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Spacing.huge),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No authorized SMS senders set",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    } else {
                        items(senders, key = { it.id }) { sender ->
                            SmsSenderRowItem(
                                sender = sender,
                                onToggle = { isEnabled -> viewModel.toggleSender(sender.id, isEnabled) },
                                onDelete = { viewModel.deleteSender(sender.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsSenderRowItem(
    sender: SmsSender,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        borderColor = RawColors.Slate.Slate200.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large, vertical = Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sender.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (sender.isEnabled) "Active suggestions" else "Filtered/Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sender.isEnabled) RawColors.Emerald.Emerald600 else TextTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Switch(
                    checked = sender.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RawColors.Emerald.Emerald500,
                        uncheckedThumbColor = RawColors.Slate.Slate400,
                        uncheckedTrackColor = RawColors.Slate.Slate200
                    )
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete sender filter",
                        tint = RawColors.Red.Red500
                    )
                }
            }
        }
    }
}
