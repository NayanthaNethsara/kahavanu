package com.kahavanu.ui.home.components

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.home.SieveItem
import com.kahavanu.ui.home.SieveType
import com.kahavanu.ui.theme.AccentExpense
import com.kahavanu.ui.theme.AccentExpenseSoft
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.theme.AccentIncomeSoft
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun SieveSection(
    items: List<SieveItem>,
    onConfirm: (String) -> Unit,
    onIgnore: (String) -> Unit,
    onScanClick: () -> Unit,
    isScanning: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(horizontal = Spacing.extraLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SectionHeader(
                        title = "The Sieve",
                        subtitle = if (items.isEmpty()) "Tap scan to find new transactions" else "SMS suggestions to confirm",
                        badgeCount = if (items.isNotEmpty()) items.size.toString() else null,
                        actionText = null,
                        onActionClick = null
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(MaterialTheme.extendedColors.brandWashed, CircleShape)
                        .clip(CircleShape)
                        .clickable(enabled = !isScanning) { onScanClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scanning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Scan SMS",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = Spacing.extraLarge)
                    .fillMaxWidth(),
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.large),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No pending suggestions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Spacing.extraLarge),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                items(items) { item ->
                    SieveCard(
                        item = item,
                        onConfirm = { onConfirm(item.id) },
                        onIgnore = { onIgnore(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun SieveCard(
    item: SieveItem,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpense = item.type == SieveType.EXPENSE
    
    val icon = if (isExpense) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp
    val iconColor = if (isExpense) MaterialTheme.extendedColors.textDeep else AccentIncome
    
    val badgeColor = if (isExpense) AccentExpense else AccentIncome
    val badgeBg = if (isExpense) AccentExpenseSoft else AccentIncomeSoft
    val badgeLabel = if (isExpense) "Expense" else "Income"

    GlassCard(
        modifier = modifier
            .width(240.dp)
            .height(175.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Section: Icon & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .background(badgeBg, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle Section: Suggestion Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.currency} ${String.format("%,.0f", item.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = item.detectedFrom,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section: Row of actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val confirmGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
                        MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.75f),
                        MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
                    )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(confirmGradient, CircleShape)
                        .border(
                            width = 0.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.1f),
                                )
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Ignore / Close Button (Circular layout)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.6f), CircleShape)
                        // Clip to CircleShape before clickable to restrict ripple/active range
                        .clip(CircleShape)
                        .clickable { onIgnore() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
