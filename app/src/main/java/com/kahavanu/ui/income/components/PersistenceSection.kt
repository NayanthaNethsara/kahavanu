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
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isRecurrent
import com.kahavanu.ui.income.components.PersistenceListItem

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
                    PersistenceListItem(
                        title = log.title,
                        dueText = getDueText(log.receivedAtEpochMillis),
                        isOverdue = isOverdue(log.receivedAtEpochMillis),
                        isPending = isPending(log.sourceType),
                        isRecurrent = isRecurrent(log.sourceType),
                        amount = formatAmount(log.amount, log.currency),
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
