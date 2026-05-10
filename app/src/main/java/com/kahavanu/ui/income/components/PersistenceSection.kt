package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSize
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.ui.income.components.getDueText
import com.kahavanu.ui.income.components.isOverdue
import com.kahavanu.ui.income.components.formatAmount

@Composable
fun PersistenceSection(
    pendingLogs: List<IncomeLogEntry>,
    onViewAll: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Persistence",
            subtitle = "Pending & overdue payments",
            actionText = "View all",
            onActionClick = onViewAll
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (pendingLogs.isEmpty()) {
                Text(
                    text = "No pending payments found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(Spacing.large),
                    textAlign = TextAlign.Center
                )
            } else {
                pendingLogs.forEachIndexed { index, log ->
                    PersistenceItem(
                        title = log.title,
                        dueText = getDueText(log.receivedAtEpochMillis),
                        isOverdue = isOverdue(log.receivedAtEpochMillis),
                        statusText = if (log.isInvoiceSent) "Invoice sent" else "Expected",
                        amount = formatAmount(log.amount, log.currency),
                        hasNudge = isOverdue(log.receivedAtEpochMillis),
                        isInvoiceSent = log.isInvoiceSent
                    )
                    if (index < pendingLogs.size - 1) {
                        HorizontalDivider(color = RawColors.Slate.Slate900.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PersistenceItem(
    title: String,
    dueText: String,
    isOverdue: Boolean,
    statusText: String,
    amount: String,
    hasNudge: Boolean,
    isInvoiceSent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isOverdue) RawColors.Red.Red500.copy(alpha = 0.1f) 
                            else RawColors.Amber.Amber500.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isOverdue) Icons.Outlined.ErrorOutline else Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isOverdue) RawColors.Red.Red600 else RawColors.Amber.Amber600
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 15.sp,
                letterSpacing = (-0.15).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) RawColors.Red.Red600 else RawColors.Amber.Amber600,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isInvoiceSent) TextSecondary else RawColors.Red.Red600
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isInvoiceSent) TextSecondary else RawColors.Red.Red600,
                    fontSize = 12.sp
                )
            }
        }
        
        // Amount and Action Column
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 15.sp,
                letterSpacing = (-0.15).sp
            )
            
            if (hasNudge) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Surface(
                    onClick = { },
                    modifier = Modifier
                        .height(24.dp)
                        .width(72.dp),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .border(
                                width = 0.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nudge",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = TextSize.xs,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
