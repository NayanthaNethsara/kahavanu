package com.kahavanu.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.home.SieveItem
import com.kahavanu.ui.home.SieveType
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.common.GlassCard
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

        Spacer(modifier = Modifier.height(Spacing.small))

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
    val accentColor = if (isExpense) RawColors.Red.Red600 else RawColors.Emerald.Emerald600
    val badgeBg = if (isExpense) RawColors.Red.Red50.copy(alpha = 0.6f) else RawColors.Emerald.Emerald50.copy(alpha = 0.6f)
    val badgeLabel = if (isExpense) "Expense" else "Income"

    GlassCard(
        modifier = modifier.width(180.dp),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        borderColor = RawColors.Slate.Slate200.copy(alpha = 0.6f),
        shadowElevation = Elevation.level1
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Top Row: Category/Type Badge + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Category/Type Badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(CornerRadius.small)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.2.sp
                        )
                    }
                }

                // Small minimal dismiss button with improved touch target size (28.dp)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(RawColors.Slate.Slate100.copy(alpha = 0.5f))
                        .clickable { onIgnore() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Ignore",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Transaction Details
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${item.currency} ${String.format("%,.0f", item.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Confirm Button (Full Width, Sleek)
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(CornerRadius.medium),
                colors = ButtonDefaults.buttonColors(containerColor = RawColors.Emerald.Emerald500),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
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
        }
    }
}

