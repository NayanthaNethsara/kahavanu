package com.kahavanu.ui.expenses.components

import com.kahavanu.ui.util.formatDate
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Store
import com.kahavanu.ui.common.DetailRow
import com.kahavanu.ui.common.SheetLabel
import com.kahavanu.ui.common.StatusBadge
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
import com.kahavanu.ui.util.categoryColor
import com.kahavanu.ui.util.categoryIcon
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.util.Locale

/**
 * Shared expense row used in both the main screen "Recent Expenses" list
 * and the "View All" history screen. Long-press opens a detail sheet.
 *
 * Layout:
 *   [Icon]  Title                  Amount
 *           Category · Date
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListItem(
    title: String,
    category: String,
    amount: Double,
    currencyCode: String,
    spentAtEpochMillis: Long? = null,
    merchant: String? = null,
    paymentMethod: String? = null,
    notes: String? = null,
    modifier: Modifier = Modifier,
) {
    var showDetail by remember { mutableStateOf(false) }
    val tint = categoryColor(category)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "ExpenseListItem.scale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
                onLongClick = { showDetail = true },
            )
            .padding(horizontal = Spacing.medium, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
                )
                if (spentAtEpochMillis != null) {
                    Text("·", style = MaterialTheme.typography.labelSmall,
            color = TextSecondary)
                    Text(
                        text = formatDate(spentAtEpochMillis),
                        style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
                    )
                }
            }
        }

        Text(
            text = "-$currencyCode ${String.format(Locale.getDefault(), "%,.0f", amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
    }

    if (showDetail) {
        ExpenseDetailSheet(
            title = title,
            category = category,
            amount = amount,
            currencyCode = currencyCode,
            spentAtEpochMillis = spentAtEpochMillis,
            merchant = merchant,
            paymentMethod = paymentMethod,
            notes = notes,
            tint = tint,
            onDismiss = { showDetail = false },
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ExpenseDetailSheet(
    title: String,
    category: String,
    amount: Double,
    currencyCode: String,
    spentAtEpochMillis: Long?,
    merchant: String?,
    paymentMethod: String?,
    notes: String?,
    tint: Color,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .wrapContentHeight()
                .padding(horizontal = Spacing.extraLarge)
                .padding(bottom = Spacing.large)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            // Header: icon + title + category badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                Icon(
                    imageVector = categoryIcon(category),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(28.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.lg,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    StatusBadge(label = category, color = tint)
                }
            }

            // Amount
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SheetLabel("Amount")
                Text(
                    text = "-$currencyCode ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                    style = MaterialTheme.typography.headlineMedium,
            lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Detail rows
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                if (spentAtEpochMillis != null) {
                    DetailRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Date",
                        value = formatDate(spentAtEpochMillis),
                    )
                }
                DetailRow(
                    icon = Icons.Outlined.Category,
                    label = "Category",
                    value = category,
                    valueColor = tint,
                )
                if (!merchant.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Store,
                        label = "Merchant",
                        value = merchant,
                    )
                }
                if (!paymentMethod.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Payments,
                        label = "Payment method",
                        value = paymentMethod,
                    )
                }
                if (!notes.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.Notes,
                        label = "Notes",
                        value = notes,
                    )
                }
            }
        }
    }
}

