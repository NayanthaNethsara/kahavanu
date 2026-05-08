package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSize

@Composable
fun IncomeLogSection(logs: List<IncomeLogEntry>) {
    Column {
        SectionHeader(
            title = "Income Log",
            subtitle = "Recent payments",
            actionText = "View all"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (logs.isEmpty()) {
                Text(
                    text = "No income logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(Spacing.large),
                )
            } else {
                logs.forEachIndexed { index, log ->
                    LogItem(
                        title = log.title,
                        type = log.note?.takeIf { it.isNotBlank() } ?: "Income",
                        date = formatDate(log.receivedAtEpochMillis),
                        amount = formatAmount(log.amount, log.currency),
                    )
                    if (index != logs.lastIndex) {
                        HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(title: String, type: String, date: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                .border(0.5.dp, RawColors.Slate.Slate200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TextTertiaryEmerald, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontSize = TextSize.base,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "$type • $date",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.xs,
                color = TextSecondary
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            fontSize = TextSize.base,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
