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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Info
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
import com.kahavanu.ui.goals.CapacityBreakdown
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

private val AccentGreen = Color(0xFF00BC7D)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentRed = Color(0xFFEF4444)

@Composable
fun ProjectionsSection(
    remainingAmount: Double,
    capacity: CapacityBreakdown,
    currency: CurrencyOption,
    activeGoalTitle: String?,
    targetDateEpochMillis: Long? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        EstimatedArrivalCard(
            remainingAmount = remainingAmount,
            rcsAmount = capacity.realCapacityToSave,
            targetDateEpochMillis = targetDateEpochMillis,
            currency = currency,
        )
        RealCapacityToSaveCard(
            capacity = capacity,
            currency = currency,
            activeGoalTitle = activeGoalTitle,
        )
    }
}

/**
 * Projects the goal's arrival from the Real Capacity to Save (treated as a sustainable monthly
 * saving rate): months = ceil(remaining / RCS). When the goal carries a target date we also
 * compute the pace it demands (remaining / months-until-deadline) and flag whether the current
 * RCS keeps that pace, so the user sees "on track" vs. how much more per month they'd need.
 */
@Composable
private fun EstimatedArrivalCard(
    remainingAmount: Double,
    rcsAmount: Double,
    targetDateEpochMillis: Long?,
    currency: CurrencyOption,
) {
    val canEstimate = rcsAmount > 0.0 && remainingAmount > 0.0
    val months = if (canEstimate) {
        ceil(remainingAmount / rcsAmount).toInt().coerceIn(1, 600)
    } else {
        null
    }
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val targetDateText = months?.let {
        "Around ${LocalDate.now().plusMonths(it.toLong()).format(monthFormatter)}"
    } ?: "Add income & expenses to project"

    // Compare the deadline the user set against the pace the current RCS can sustain.
    val zone = ZoneId.systemDefault()
    val deadline = targetDateEpochMillis?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
    }
    val monthsToDeadline = deadline?.let {
        ChronoUnit.MONTHS.between(YearMonth.now(zone), YearMonth.from(it))
    }
    val status: DeadlineStatus? = when {
        deadline == null || remainingAmount <= 0.0 -> null
        monthsToDeadline == null || monthsToDeadline <= 0L ->
            DeadlineStatus("Target date has passed", AccentRed)
        else -> {
            val requiredMonthly = remainingAmount / monthsToDeadline
            if (rcsAmount >= requiredMonthly) {
                DeadlineStatus(
                    "On track for ${deadline.format(monthFormatter)}",
                    AccentGreen,
                )
            } else {
                DeadlineStatus(
                    "Behind · save ${formatGoalAmount(requiredMonthly, currency.code)}/mo to make it",
                    AccentAmber,
                )
            }
        }
    }

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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = months?.let { "$it mo" } ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
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
                    tint = AccentGreen.copy(alpha = 0.8f),
                    modifier = Modifier.size(44.dp),
                )
            }

            if (status != null) {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(status.color.copy(alpha = 0.08f))
                        .border(1.dp, status.color.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = status.color,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = status.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = status.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private data class DeadlineStatus(val message: String, val color: Color)

@Composable
private fun RealCapacityToSaveCard(
    capacity: CapacityBreakdown,
    currency: CurrencyOption,
    activeGoalTitle: String?,
) {
    val formattedRcs = formatGoalAmount(capacity.realCapacityToSave, currency.code)

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
                text = "Income: ${formatGoalAmount(capacity.monthlyIncome, currency.code)} this month",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            CapacitySegmentedBar(capacity = capacity)

            Spacer(modifier = Modifier.height(Spacing.small))

            CapacityLegend(capacity = capacity)

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00BC7D).copy(alpha = 0.06f))
                    .border(1.dp, Color(0xFF00BC7D).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF00BC7D),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = if (activeGoalTitle != null) {
                        "You can safely move $formattedRcs toward $activeGoalTitle this month."
                    } else {
                        "Set an active goal to allocate $formattedRcs this month."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00BC7D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CapacitySegmentedBar(capacity: CapacityBreakdown) {
    val committedWeight = capacity.committed.toFloat().coerceAtLeast(0f)
    val discretionaryWeight = capacity.discretionary.toFloat().coerceAtLeast(0f)
    val rcsWeight = capacity.realCapacityToSave.toFloat().coerceAtLeast(0f)
    val totalWeight = committedWeight + discretionaryWeight + rcsWeight
    val emptyState = totalWeight <= 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(99.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (emptyState) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .background(Color(0xFF475569).copy(alpha = 0.25f)),
            )
        } else {
            if (committedWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(committedWeight)
                        .height(10.dp)
                        .background(Color(0xFF475569)),
                )
            }
            if (discretionaryWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(discretionaryWeight)
                        .height(10.dp)
                        .background(Color(0xFF189065).copy(alpha = 0.55f)),
                )
            }
            if (rcsWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(rcsWeight)
                        .height(10.dp)
                        .background(Color(0xFF00BC7D)),
                )
            }
        }
    }
}

@Composable
private fun CapacityLegend(capacity: CapacityBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LegendDot(color = Color(0xFF475569), label = "Committed ${shortAmount(capacity.committed)}")
        LegendDot(
            color = Color(0xFF189065).copy(alpha = 0.55f),
            label = "Discretionary ${shortAmount(capacity.discretionary)}",
        )
        LegendDot(color = Color(0xFF00BC7D), label = "RCS ${shortAmount(capacity.realCapacityToSave)}")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 9.sp,
        )
    }
}

private fun shortAmount(value: Double): String {
    val rounded = value.roundToInt()
    return when {
        rounded >= 1_000_000 -> "${rounded / 1_000_000}M"
        rounded >= 1_000 -> "${rounded / 1_000}K"
        else -> rounded.toString()
    }
}

private fun formatGoalAmount(amount: Double, currencyCode: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "$currencyCode ${formatter.format(amount)}"
}
