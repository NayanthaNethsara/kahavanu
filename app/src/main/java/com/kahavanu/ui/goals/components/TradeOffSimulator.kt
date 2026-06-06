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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.compactAmount
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

private val AccentGreen = Color(0xFF00BC7D)
private val AccentRed = Color(0xFFEF4444)

/**
 * Lets the user trade discretionary spending against goal arrival, entirely from real numbers.
 *
 * The "flexible pool" is everything left after committed bills (income − committed = RCS +
 * discretionary): spend none of it and the whole pool becomes savings; spend it all and savings
 * drop to zero. The slider picks a simulated discretionary spend within that pool, the freed-up
 * money raises RCS, and the arrival date is re-projected as ceil(remaining / new RCS).
 */
@Composable
fun TradeOffSimulator(
    featuredGoal: GoalEntry?,
    currency: CurrencyOption,
    baselineRcs: Double = 0.0,
    discretionarySpend: Double = 0.0,
    modifier: Modifier = Modifier,
) {
    if (featuredGoal == null || baselineRcs <= 0.0) return
    val remainingTarget = (featuredGoal.targetAmount - featuredGoal.currentAmount).coerceAtLeast(0.0)
    if (remainingTarget <= 0.0) return
    val baselineMonths = ceil(remainingTarget / baselineRcs).toInt().coerceAtLeast(1)

    // Flexible pool = what you could save if you spent nothing discretionary (RCS + discretionary).
    val flexiblePool = (baselineRcs + discretionarySpend).coerceAtLeast(1.0)
    val baselineSpend = discretionarySpend.coerceIn(0.0, flexiblePool)

    // Re-seed the slider whenever the underlying figures change (new logs, new active goal).
    var simulatedSpend by remember(baselineSpend, flexiblePool) {
        mutableFloatStateOf(baselineSpend.toFloat())
    }

    val newRcs = (flexiblePool - simulatedSpend).coerceAtLeast(0.0)
    val newMonths = if (newRcs > 0.0) {
        ceil(remainingTarget / newRcs).toInt().coerceAtLeast(1)
    } else {
        null
    }

    val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
    val targetDateText = newMonths?.let {
        "Around ${LocalDate.now().plusMonths(it.toLong()).format(formatter)}"
    } ?: "Not reachable at this rate"

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "TRADE-OFF SIMULATOR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Impact Indicators Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Arrival Month Impact Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(Spacing.medium)
                ) {
                    Text(
                        text = "Estimated Arrival",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = newMonths?.let { "$it mo" } ?: "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            newMonths == null -> AccentRed
                            newMonths < baselineMonths -> AccentGreen
                            newMonths > baselineMonths -> AccentRed
                            else -> TextPrimary
                        },
                    )
                    Text(
                        text = targetDateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }

                // Adjusted Capacity Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(Spacing.medium)
                ) {
                    Text(
                        text = "New Capacity to Save",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatSimAmount(newRcs, currency.code),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Monthly allocation",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Slider Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discretionary Spend",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Text(
                    text = formatSimAmount(simulatedSpend.toDouble(), currency.code),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Slider(
                value = simulatedSpend,
                onValueChange = { simulatedSpend = it },
                valueRange = 0f..flexiblePool.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentGreen,
                    activeTrackColor = AccentGreen,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currency.code} 0",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
                Text(
                    text = "Now: ${compactAmount(currency.code, baselineSpend.toFloat())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
                Text(
                    text = compactAmount(currency.code, flexiblePool.toFloat()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Speed/Delay Pill Indicator
            val deltaMonths = newMonths?.let { baselineMonths - it }
            val (pillText, pillColor, pillBg) = when {
                deltaMonths == null -> Triple("Saving nothing — goal stalls", AccentRed, AccentRed.copy(alpha = 0.08f))
                deltaMonths > 0 -> Triple("Faster by $deltaMonths month${if (deltaMonths > 1) "s" else ""}!", AccentGreen, AccentGreen.copy(alpha = 0.08f))
                deltaMonths < 0 -> Triple("Slower by ${-deltaMonths} month${if (-deltaMonths > 1) "s" else ""}", AccentRed, AccentRed.copy(alpha = 0.08f))
                else -> Triple("At your current spending", TextSecondary, MaterialTheme.colorScheme.surfaceVariant)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(pillBg)
                    .border(1.dp, pillColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = Spacing.medium, vertical = 6.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = pillColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = pillText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pillColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Bottom Simulator Help Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Trim this month's discretionary spend to see how much sooner you arrive.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun formatSimAmount(amount: Double, currencyCode: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "$currencyCode ${formatter.format(amount)}"
}
