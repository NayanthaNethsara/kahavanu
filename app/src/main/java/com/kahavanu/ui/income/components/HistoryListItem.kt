package com.kahavanu.ui.income.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.ui.income.HistoryItem
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryListItem(
    item: HistoryItem,
    onMarkAsReceived: (() -> Unit)? = null,
) {
    var showDetail by remember { mutableStateOf(false) }

    val isOverdue = item is HistoryItem.Scheduled && isOverdue(item.scheduled.scheduledDateEpochMillis)
    val isPending = item is HistoryItem.Scheduled &&
        item.scheduled.type == IncomeSourceType.PENDING &&
        item.scheduled.lastGeneratedEpochMillis == null
    val isRecurrent = item is HistoryItem.Scheduled && item.scheduled.type == IncomeSourceType.RECURRENT
    val isPaid = item is HistoryItem.Log

    val accentColor = when {
        isOverdue -> RawColors.Red.Red600
        isPaid -> RawColors.Emerald.Emerald600
        else -> RawColors.Amber.Amber600
    }
    val statusLabel = when {
        isOverdue -> "Overdue"
        isPending -> "Pending"
        isPaid -> "Received"
        isRecurrent -> "Recurrent"
        else -> "One-time"
    }

    val sourceName = when (item) {
        is HistoryItem.Log -> item.log.sourceName
        is HistoryItem.Scheduled -> item.scheduled.sourceName
    }
    val dueText = when (item) {
        is HistoryItem.Scheduled -> getDueText(item.scheduled.scheduledDateEpochMillis)
        is HistoryItem.Log -> formatDate(item.log.receivedAtEpochMillis)
    }
    val icon: ImageVector = incomeSourceIcon(item.title, sourceName)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "HistoryListItem.scale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
                onLongClick = { showDetail = true },
            )
            .padding(horizontal = Spacing.large, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        // Source icon — no background
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp),
        )

        // Row 1: Title  |  Row 2: Source · Date
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Row 1: Title — Source name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (!sourceName.isNullOrBlank()) {
                    Text(
                        text = sourceName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                    )
                }
            }
            // Row 2: Date — Status + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                    )
                    if (isPending && onMarkAsReceived != null) {
                        MiniActionButton(
                            label = "Receive",
                            color = RawColors.Emerald.Emerald600,
                            onClick = onMarkAsReceived,
                        )
                    }
                    if (isOverdue) {
                        MiniActionButton(
                            label = "Nudge",
                            color = RawColors.Red.Red600,
                            icon = Icons.AutoMirrored.Outlined.Send,
                            onClick = {},
                        )
                    }
                }
            }
        }

        // Amount
        Text(
            text = formatAmount(item.amount, item.currency),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
    }

    if (showDetail) {
        HistoryDetailSheet(
            item = item,
            isOverdue = isOverdue,
            isPending = isPending,
            isRecurrent = isRecurrent,
            isPaid = isPaid,
            statusLabel = statusLabel,
            accentColor = accentColor,
            onMarkAsReceived = onMarkAsReceived,
            onDismiss = { showDetail = false },
        )
    }
}

@Composable
private fun TypeBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), CircleShape)
            .border(0.5.dp, color.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
        )
    }
}

@Composable
private fun MiniActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), CircleShape)
            .border(0.5.dp, color.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun HistoryDetailSheet(
    item: HistoryItem,
    isOverdue: Boolean,
    isPending: Boolean,
    isRecurrent: Boolean,
    isPaid: Boolean,
    statusLabel: String,
    accentColor: Color,
    onMarkAsReceived: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val frequency = when (item) {
        is HistoryItem.Log -> item.log.frequency
        is HistoryItem.Scheduled -> item.scheduled.frequency
    }
    val sourceName = when (item) {
        is HistoryItem.Log -> item.log.sourceName
        is HistoryItem.Scheduled -> item.scheduled.sourceName
    }
    val contactName = when (item) {
        is HistoryItem.Log -> item.log.contactName
        is HistoryItem.Scheduled -> item.scheduled.contactName
    }
    val contactNumber = when (item) {
        is HistoryItem.Log -> item.log.contactNumber
        is HistoryItem.Scheduled -> item.scheduled.contactNumber
    }
    val dateLabel = when (item) {
        is HistoryItem.Scheduled -> getDueText(item.scheduled.scheduledDateEpochMillis)
        is HistoryItem.Log -> formatDate(item.log.receivedAtEpochMillis)
    }
    val occurrenceCount = (item as? HistoryItem.Scheduled)?.scheduled?.occurrenceCount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.extraLarge)
                .padding(bottom = Spacing.large)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                Icon(
                    imageVector = incomeSourceIcon(
                        title = item.title,
                        sourceName = when (item) {
                            is HistoryItem.Log -> item.log.sourceName
                            is HistoryItem.Scheduled -> item.scheduled.sourceName
                        },
                    ),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.lg,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    TypeBadge(label = statusLabel, color = accentColor)
                }
            }

            // Amount
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SheetLabel("Amount")
                Text(
                    text = formatAmount(item.amount, item.currency),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Detail rows
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                DetailRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = if (isPaid) "Received on" else if (isOverdue) "Due date" else "Expected",
                    value = dateLabel,
                    valueColor = accentColor,
                )
                if (!sourceName.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Source,
                        label = "Source",
                        value = sourceName,
                    )
                }
                if (!contactName.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.AccountCircle,
                        label = "Contact",
                        value = contactName,
                    )
                }
                if (!contactNumber.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone",
                        value = contactNumber,
                    )
                }
                if (!frequency.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Autorenew,
                        label = "Frequency",
                        value = frequency,
                    )
                }
                if (occurrenceCount != null && occurrenceCount > 0) {
                    DetailRow(
                        icon = Icons.Outlined.Schedule,
                        label = "Occurrences",
                        value = occurrenceCount.toString(),
                    )
                }
            }

            if (isPending && onMarkAsReceived != null) {
                Button(
                    onClick = {
                        onMarkAsReceived()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RawColors.Emerald.Emerald600,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Mark as Received", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        letterSpacing = 0.7.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                color = valueColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Picks a meaningful money/work icon based on keywords in [title] and
 * [sourceName]. Falls back to a status icon when no keyword matches.
 */
fun incomeSourceIcon(title: String, sourceName: String?): ImageVector {
    val text = "${title.lowercase()} ${sourceName?.lowercase().orEmpty()}"
    return when {
        text.containsAny("salary", "wage", "payroll", "employment", "job", "paycheck") ->
            Icons.Outlined.Work
        text.containsAny("freelance", "contract", "consulting", "project", "client", "retainer", "dev", "design", "code") ->
            Icons.Outlined.Laptop
        text.containsAny("rent", "rental", "property", "lease", "tenant", "airbnb") ->
            Icons.Outlined.Home
        text.containsAny("crypto", "bitcoin", "btc", "eth", "ethereum", "token") ->
            Icons.Outlined.TrendingUp
        text.containsAny("invest", "dividend", "stock", "share", "equity", "fund", "portfolio") ->
            Icons.Outlined.TrendingUp
        text.containsAny("bank", "interest", "savings", "deposit", "fixed") ->
            Icons.Outlined.AccountBalance
        text.containsAny("business", "shop", "store", "sales", "revenue", "profit") ->
            Icons.Outlined.Store
        text.containsAny("gift", "bonus", "reward", "prize", "award", "tip") ->
            Icons.Outlined.CardGiftcard
        text.containsAny("commission", "referral", "affiliate", "sell") ->
            Icons.Outlined.Sell
        text.containsAny("transfer", "payment", "settle", "refund") ->
            Icons.Outlined.Payments
        text.containsAny("cash", "atm", "withdrawal") ->
            Icons.Outlined.LocalAtm
        else -> Icons.Outlined.MonetizationOn
    }
}

private fun String.containsAny(vararg keywords: String) = keywords.any { contains(it, ignoreCase = true) }
