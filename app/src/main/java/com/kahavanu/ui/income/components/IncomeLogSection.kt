package com.kahavanu.ui.income.components

import com.kahavanu.ui.util.formatDate
import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.TextSize

@Composable
fun IncomeLogSection(
    logs: List<IncomeLogEntry>,
    onViewAll: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Income Log",
            subtitle = "Recent payments",
            actionText = "View all",
            onActionClick = onViewAll
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
                    val icon = when {
                        log.title.contains("ACME", ignoreCase = true) -> Icons.Outlined.AccountBalanceWallet
                        log.title.contains("WordPress", ignoreCase = true) || log.title.contains("SME", ignoreCase = true) -> Icons.Outlined.BusinessCenter
                        log.title.contains("Blog", ignoreCase = true) -> Icons.Outlined.Language
                        else -> Icons.Outlined.AccountBalanceWallet
                    }
                    
                    val subtitle = buildString {
                        if (!log.sourceName.isNullOrBlank()) {
                            append(log.sourceName)
                            append(" • ")
                        }
                        append(formatDate(log.receivedAtEpochMillis))
                    }

                    LogItem(
                        title = log.title,
                        subtitle = subtitle,
                        amount = formatAmount(log.amount, log.currency),
                        icon = icon
                    )
                    if (index != logs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.large),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextTertiaryEmerald,
                modifier = Modifier.size(16.dp)
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
                letterSpacing = (-0.07).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
                letterSpacing = 0.06.sp
            )
        }
        
        // Amount
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontSize = TextSize.sm,
            color = TextTertiaryEmerald,
            letterSpacing = (-0.29).sp
        )
    }
}
