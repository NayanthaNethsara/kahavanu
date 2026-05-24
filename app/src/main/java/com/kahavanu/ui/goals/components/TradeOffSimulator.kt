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
fun TradeOffSimulator(
    featuredGoal: GoalEntry?,
    currency: CurrencyOption,
    baselineRcs: Double = 0.0,
    modifier: Modifier = Modifier,
) {
    if (featuredGoal == null || baselineRcs <= 0.0) return
    val remainingTarget = (featuredGoal.targetAmount - featuredGoal.currentAmount).coerceAtLeast(0.0)
    val baselineMonths = ceil(remainingTarget / baselineRcs).toInt().coerceAtLeast(1)

    var diningSpend by remember { mutableFloatStateOf(24000f) }

    // diningSpend can range from 4,000 to 48,000
    val minSpend = 4000f
    val maxSpend = 48000f

    val deltaDining = diningSpend.toDouble() - 24000.0
    // new RCS: if dining spend is reduced (delta is negative), RCS increases!
    val newRcs = (baselineRcs - deltaDining).coerceAtLeast(1000.0)
    val newMonths = ceil(remainingTarget / newRcs).toInt().coerceAtLeast(1)

    val targetDate = LocalDate.now().plusMonths(newMonths.toLong())
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
    val targetDateText = "Around ${targetDate.format(formatter)}"

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
                        text = "$newMonths mo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (newMonths < baselineMonths) Color(0xFF00BC7D) else if (newMonths > baselineMonths) Color(0xFFEF4444) else TextPrimary,
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
                    text = "Casual Dining Spend",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Text(
                    text = formatSimAmount(diningSpend.toDouble(), currency.code),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Slider(
                value = diningSpend,
                onValueChange = { diningSpend = it },
                valueRange = minSpend..maxSpend,
                steps = 43, // step by 1,000 LKR
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00BC7D),
                    activeTrackColor = Color(0xFF00BC7D),
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
                    text = "LKR 4K (Min)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
                Text(
                    text = "Baseline: LKR 24K",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
                Text(
                    text = "LKR 48K (Max)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Speed/Delay Pill Indicator
            val deltaMonths = baselineMonths - newMonths
            val (pillText, pillColor, pillBg) = when {
                deltaMonths > 0 -> Triple("Faster by $deltaMonths month${if (deltaMonths > 1) "s" else ""}!", Color(0xFF00BC7D), Color(0xFF00BC7D).copy(alpha = 0.08f))
                deltaMonths < 0 -> Triple("Slower by ${-deltaMonths} month${if (-deltaMonths > 1) "s" else ""}", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.08f))
                else -> Triple("Baseline dining budget", TextSecondary, MaterialTheme.colorScheme.surfaceVariant)
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
                    text = "Move the slider to preview the impact on your arrival date.",
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
