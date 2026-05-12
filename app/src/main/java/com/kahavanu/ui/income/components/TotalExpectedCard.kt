package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.income.IncomeBreakdownItem
import com.kahavanu.ui.common.AppSegmentedToggle
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlin.math.roundToInt

@Composable
fun TotalExpectedCard(
    totalIncomeByCurrency: Map<String, Double>,
    totalReceivedByCurrency: Map<String, Double>,
    primaryCurrency: String,
    monthLabel: String,
    breakdownsByCurrency: Map<String, List<IncomeBreakdownItem>>,
) {
    val currencies = totalIncomeByCurrency.keys.toList()
        .sortedWith(compareBy<String> { it != primaryCurrency }.thenBy { it })
    var selectedCurrency by remember(currencies) { 
        mutableStateOf(if (currencies.contains(primaryCurrency)) primaryCurrency else currencies.firstOrNull() ?: primaryCurrency) 
    }

    val currentTotalIncome = totalIncomeByCurrency[selectedCurrency] ?: 0.0
    val currentTotalReceived = totalReceivedByCurrency[selectedCurrency] ?: 0.0
    val progress = if (currentTotalIncome > 0.0) (currentTotalReceived / currentTotalIncome).toFloat() else 0f
    val progressPercentage = "${(progress * 100).roundToInt()}%"
    
    val totalIncomeText = formatAmount(currentTotalIncome, selectedCurrency)
    val totalReceivedText = formatAmount(currentTotalReceived, selectedCurrency)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            // Header with optional Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total income · $monthLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                if (currencies.size > 1) {
                    AppSegmentedToggle(
                        items = currencies,
                        selectedItem = selectedCurrency,
                        onSelect = { selectedCurrency = it },
                        labelFor = { it },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(34.dp),
                        height = 34.dp,
                        shape = CircleShape,
                        containerColor = RawColors.Emerald.Emerald500.copy(alpha = 0.08f),
                        indicatorColor = RawColors.Emerald.Emerald600.copy(alpha = 0.9f),
                        indicatorShadow = 4.dp,
                        selectedTextColor = Color.White,
                        unselectedTextColor = RawColors.Emerald.Emerald600.copy(alpha = 0.9f),
                        textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        itemWidth = 64.dp,
                    )
                }
            }
            Text(
                text = totalIncomeText,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextSize.xxxl,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalReceivedText received",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary
                )
                Text(
                    text = progressPercentage,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = TextSize.sm,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = RawColors.Emerald.Emerald500,
                trackColor = RawColors.Slate.Slate100.copy(alpha = 0.5f),
                drawStopIndicator = {}
            )

            val items = breakdownsByCurrency[selectedCurrency] ?: emptyList()
            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))
                
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    items.forEach { item ->
                        SummaryItem(
                            label = item.label,
                            value = formatAmount(item.amount, selectedCurrency),
                            color = item.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.xs,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
