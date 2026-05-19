package com.kahavanu.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.home.SieveItem
import com.kahavanu.ui.home.SieveType
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun SieveSection(
    items: List<SieveItem>,
    onConfirm: (String) -> Unit,
    onIgnore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(horizontal = Spacing.extraLarge)) {
            SectionHeader(
                title = "The Sieve",
                subtitle = "SMS suggestions to confirm",
                actionText = "See all",
                badgeCount = items.size.toString()
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Spacing.extraLarge),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            items(items) { item ->
                SieveCard(
                    item = item,
                    onConfirm = { onConfirm(item.id) },
                    onIgnore = { onIgnore(item.id) }
                )
            }
        }
    }
}

@Composable
fun SieveCard(
    item: SieveItem,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = item.type == SieveType.EXPENSE
    
    val icon = if (isExpense) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp
    val categoryColor = if (isExpense) RawColors.Red.Red500 else RawColors.Emerald.Emerald600
    val categoryBg = if (isExpense) RawColors.Red.Red50.copy(alpha = 0.5f) else RawColors.Emerald.Emerald50.copy(alpha = 0.5f)
    val badgeLabel = if (isExpense) "Expense" else "Income"

    Surface(
        modifier = modifier
            .width(240.dp)
            .shadow(Elevation.level3, shape = RoundedCornerShape(CornerRadius.large)),
        shape = RoundedCornerShape(CornerRadius.large),
        color = Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RawColors.Slate.Slate200.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(categoryBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badgeLabel,
                        tint = categoryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Surface(
                    color = categoryBg,
                    shape = CircleShape
                ) {
                    Text(
                        text = badgeLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${item.currency} ${String.format("%,.0f", item.amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            Text(
                text = item.detectedFrom,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = RawColors.Emerald.Emerald500),
                    contentPadding = PaddingValues(horizontal = Spacing.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Confirm",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.extraSmall))
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onIgnore,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, RawColors.Slate.Slate200, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Ignore",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
