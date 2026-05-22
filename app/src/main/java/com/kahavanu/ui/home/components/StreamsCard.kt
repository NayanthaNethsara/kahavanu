package com.kahavanu.ui.home.components

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.home.IncomeStreamItem
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun StreamsCard(
    incomeStreams: List<IncomeStreamItem>,
    onIncomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "The Streams",
            subtitle = "Unified income snapshot",
            actionText = "Details",
            onActionClick = onIncomeClick
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIncomeClick() }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                incomeStreams.forEachIndexed { index, stream ->
                    IncomeStreamRow(stream = stream)
                    if (index < incomeStreams.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = Spacing.medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeStreamRow(
    stream: IncomeStreamItem,
    modifier: Modifier = Modifier
) {
    val icon = when (stream.iconIndex) {
        12 -> Icons.Outlined.AccountBalanceWallet
        13 -> Icons.Outlined.Language
        else -> Icons.Outlined.CurrencyBitcoin
    }
    
    val iconBg = when (stream.iconIndex) {
        12 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        13 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else -> MaterialTheme.extendedColors.warningContainer.copy(alpha = 0.45f)
    }
    
    val iconColor = when (stream.iconIndex) {
        12 -> MaterialTheme.extendedColors.textStrong
        13 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.extendedColors.warning
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stream.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = stream.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Received",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = stream.receivedFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
