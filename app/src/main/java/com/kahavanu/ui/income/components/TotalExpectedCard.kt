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
import androidx.compose.material3.Surface
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.income.IncomeBreakdownItem
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
                    color = TextSecondary
                )
                
                if (currencies.size > 1) {
                    val selectedIndex = currencies.indexOf(selectedCurrency)
                    val itemWidth = 56.dp // Fixed width for each toggle item
                    val indicatorOffset by animateDpAsState(
                        targetValue = itemWidth * selectedIndex,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "indicatorOffset"
                    )

                    Surface(
                        shape = CircleShape,
                        color = RawColors.Emerald.Emerald500.copy(alpha = 0.08f),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(34.dp)
                            .width(itemWidth * currencies.size)
                    ) {
                        Box {
                            // Animated Indicator
                            Surface(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(width = itemWidth - 4.dp, height = 30.dp)
                                    .offset { IntOffset(indicatorOffset.roundToPx(), 0) },
                                shape = CircleShape,
                                color = RawColors.Emerald.Emerald600.copy(alpha = 0.9f),
                                shadowElevation = 4.dp,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {}

                            // Text Labels
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                currencies.forEach { code ->
                                    val isSelected = selectedCurrency == code
                                    Box(
                                        modifier = Modifier
                                            .width(itemWidth)
                                            .height(34.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { selectedCurrency = code },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = code,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else RawColors.Emerald.Emerald600.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
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
