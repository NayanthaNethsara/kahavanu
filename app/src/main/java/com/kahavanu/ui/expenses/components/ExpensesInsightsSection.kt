package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.outlined.CreditCard
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
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.AccentIncomeSoft
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.util.categoryColor
import com.kahavanu.ui.util.categoryIcon

@Composable
fun ExpensesInsightsSection(
    modifier: Modifier = Modifier,
    biggestSpendCategory: String = "Food",
    biggestSpendSubtitle: String = "LKR 45K · 30%",
    subscriptionCost: Double = 4850.0,
    subscriptionCount: Int = 3,
    onSubscriptionLongClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        SectionHeader(
            title = "Insights",
            subtitle = "Spending breakdown"
        )

        // 1. Performance breakdown Main Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                // Header with Progress & Saved Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "WEEK 2 SPENDING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-18%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 24.sp,
                            letterSpacing = (-0.4).sp
                        )
                    }

                    // Green Saved Pill
                    Box(
                        modifier = Modifier
                            .background(AccentIncomeSoft, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.TrendingDown,
                                contentDescription = null,
                                tint = AccentIncome,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Saved",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentIncome,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Canvas Bezier chart for Expenses
                ExpensesPerformanceChart()

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Chart Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.5.dp)
                                .background(Color(0xFF00BC7D), CircleShape)
                        )
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Last Month Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.5.dp)
                                .background(Color(0xFFCBD5E1), CircleShape)
                        )
                        Text(
                            text = "Last month",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Budget Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.width(4.dp).height(2.dp).background(Color(0xFFDC2626).copy(alpha = 0.6f)))
                            Box(modifier = Modifier.width(4.dp).height(2.dp).background(Color(0xFFDC2626).copy(alpha = 0.6f)))
                        }
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 2. Side-by-side Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card 1: Biggest Spend Card
            val biggestSpendColor = categoryColor(biggestSpendCategory)
            
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(134.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large)
                ) {
                    Icon(
                        imageVector = categoryIcon(biggestSpendCategory),
                        contentDescription = null,
                        tint = biggestSpendColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "BIGGEST SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = biggestSpendCategory,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(biggestSpendColor, CircleShape)
                        )
                        Text(
                            text = biggestSpendSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Card 2: Subscriptions Card
            val subsColor = Color(0xFF8B5CF6) // Purple matching Figma
            
            val subsClickModifier = if (onSubscriptionLongClick != null) {
                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                Modifier.combinedClickable(
                    onLongClick = onSubscriptionLongClick,
                    onClick = {}
                )
            } else {
                Modifier
            }

            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(134.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(subsClickModifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = subsColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SUBSCRIPTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "LKR ${String.format("%,.0f", subscriptionCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(subsColor, CircleShape)
                        )
                        Text(
                            text = "$subscriptionCount active",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
