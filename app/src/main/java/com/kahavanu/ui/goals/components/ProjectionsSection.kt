package com.kahavanu.ui.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun ProjectionsSection(
    remainingAmount: Double,
    rcsAmount: Double,
    currency: CurrencyOption,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        EstimatedArrivalCard(
            remainingAmount = remainingAmount,
            rcsAmount = rcsAmount,
            currency = currency
        )
        
        RealCapacityToSaveCard(
            rcsAmount = rcsAmount,
            currency = currency
        )
    }
}

@Composable
fun EstimatedArrivalCard(
    remainingAmount: Double,
    rcsAmount: Double,
    currency: CurrencyOption,
) {
    // If remaining is 0 or rcs is <= 0, fall back to MacBook Pro M4 mock projections
    val months = if (rcsAmount > 0.0 && remainingAmount > 0.0) {
        ceil(remainingAmount / rcsAmount).toInt().coerceIn(1, 120)
    } else {
        9
    }

    val targetDate = LocalDate.now().plusMonths(months.toLong())
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val targetDateText = "Around ${targetDate.format(formatter)}"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "ESTIMATED ARRIVAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$months mo",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BC7D),
                    )
                    Text(
                        text = targetDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF00BC7D).copy(alpha = 0.8f),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Boost & Drift Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Last Boost (Green)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00BC7D).copy(alpha = 0.06f))
                        .border(1.dp, Color(0xFF00BC7D).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFF00BC7D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = "-12 days",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BC7D),
                        )
                        Text(
                            text = "Last boost (AdSense USD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 9.sp,
                        )
                    }
                }

                // Last Drift (Red)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.06f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = "+5 days",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                        )
                        Text(
                            text = "Last drift (Dining overspend)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 9.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealCapacityToSaveCard(
    rcsAmount: Double,
    currency: CurrencyOption,
) {
    val totalIncome = 165000.0
    val committed = 34000.0
    val discretionary = 78000.0
    val formattedRcs = formatGoalAmount(rcsAmount, currency.code)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "REAL CAPACITY TO SAVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = formattedRcs,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = TextSize.xxl,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Text(
                text = "Income: ${formatGoalAmount(totalIncome, currency.code)} · Safe to allocate",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            // Capacity Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Committed Bar (Gray/Slate)
                Box(
                    modifier = Modifier
                        .weight(committed.toFloat())
                        .height(10.dp)
                        .background(Color(0xFF475569))
                )
                // Discretionary Bar (Dark Emerald/Green soft)
                Box(
                    modifier = Modifier
                        .weight(discretionary.toFloat())
                        .height(10.dp)
                        .background(Color(0xFF189065).copy(alpha = 0.55f))
                )
                // RCS Bar (Bright Emerald/Green)
                Box(
                    modifier = Modifier
                        .weight(rcsAmount.toFloat().coerceAtLeast(1f))
                        .height(10.dp)
                        .background(Color(0xFF00BC7D))
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Legend labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF475569))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Committed 34K",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF189065).copy(alpha = 0.55f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Discretionary 78K",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF00BC7D))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RCS ${rcsAmount.roundToInt() / 1000}K",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00BC7D).copy(alpha = 0.06f))
                    .border(1.dp, Color(0xFF00BC7D).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF00BC7D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "You can safely move $formattedRcs toward the MacBook fund this month.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00BC7D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun formatGoalAmount(amount: Double, currencyCode: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "$currencyCode ${formatter.format(amount)}"
}
