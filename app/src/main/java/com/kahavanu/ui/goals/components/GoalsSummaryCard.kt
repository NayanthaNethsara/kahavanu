package com.kahavanu.ui.goals.components

import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GoalsSummaryCard(
    totalSaved: Double,
    totalTarget: Double,
    activeCount: Int,
    completedCount: Int,
    currency: CurrencyOption,
) {
    val progressPercent = if (totalTarget > 0.0) {
        ((totalSaved / totalTarget) * 100).roundToInt().coerceIn(0, 100)
    } else {
        0
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "Total saved across all goals",
                style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = formatAmount(totalSaved, currency.code),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextSize.xxxl,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                letterSpacing = (-0.18).sp,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = "Target: ${formatAmount(totalTarget, currency.code)} · $progressPercent% reached",
                style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))
            GoalProgressBar(progressPercent = progressPercent)

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GoalStatItem(label = "Active", value = "$activeCount")
                GoalStatItem(label = "Completed", value = "$completedCount")
            }
        }
    }
}

@Composable
private fun GoalProgressBar(progressPercent: Int) {
    val ratio = (progressPercent / 100f).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio)
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.extendedColors.brandAccent),
        )
    }
}

@Composable
private fun GoalStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontSize = TextSize.xl,
            color = TextPrimary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

fun formatAmount(amount: Double, currencyCode: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "$currencyCode ${formatter.format(amount)}"
}
