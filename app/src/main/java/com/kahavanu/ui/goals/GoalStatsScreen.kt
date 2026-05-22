package com.kahavanu.ui.goals

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.goals.components.formatAmount
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlin.math.roundToInt

@Composable
fun GoalStatsScreen(
    onBack: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val allGoals = uiState.activeGoals + uiState.completedGoals
    val totalGoals = allGoals.size
    val completedCount = uiState.completedGoals.size
    val completionRate = if (totalGoals > 0) (completedCount * 100f / totalGoals).roundToInt() else 0
    val totalSavedAllTime = allGoals.sumOf { it.currentAmount }
    val totalTargetAllTime = allGoals.sumOf { it.targetAmount }
    val avgActiveProgress = if (uiState.activeGoals.isNotEmpty()) {
        uiState.activeGoals.map { g ->
            if (g.targetAmount > 0) (g.currentAmount / g.targetAmount * 100).roundToInt() else 0
        }.average().roundToInt()
    } else 0

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.background, MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.3f))
                        )
                    )
                    .padding(horizontal = Spacing.large, vertical = Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Column {
                    Text(
                        text = "Goal Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Your savings at a glance",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = TextSize.sm,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Spacing.large),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Flag,
                            iconColor = MaterialTheme.extendedColors.info,
                            label = "Total Goals",
                            value = "$totalGoals",
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.CheckCircle,
                            iconColor = MaterialTheme.extendedColors.brandAccent,
                            label = "Completed",
                            value = "$completedCount",
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.TrendingUp,
                            iconColor = MaterialTheme.extendedColors.utilityAccent,
                            label = "Completion Rate",
                            value = "$completionRate%",
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Savings,
                            iconColor = MaterialTheme.extendedColors.warning,
                            label = "Avg Progress",
                            value = "$avgActiveProgress%",
                        )
                    }
                }
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.large)) {
                            Text(
                                text = "All-time Savings",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
        )
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            Text(
                                text = formatAmount(totalSavedAllTime, uiState.currency.code),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Text(
                                text = "of ${formatAmount(totalTargetAllTime, uiState.currency.code)} across all goals",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = TextSize.sm,
                                color = TextSecondary,
                            )
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            val overallPct = if (totalTargetAllTime > 0)
                                (totalSavedAllTime / totalTargetAllTime * 100f).roundToInt().coerceIn(0, 100)
                            else 0
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(overallPct / 100f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(MaterialTheme.extendedColors.brandAccent),
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.extraSmall))
                            Text(
                                text = "$overallPct% of all targets reached",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = TextSize.xs,
                                color = MaterialTheme.colorScheme.primary,
        )
                        }
                    }
                }

                if (uiState.activeGoals.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Goals Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = Spacing.small),
                        )
                    }
                    items(uiState.activeGoals.size) { index ->
                        val goal = uiState.activeGoals[index]
                        val pct = if (goal.targetAmount > 0)
                            (goal.currentAmount / goal.targetAmount * 100f).roundToInt().coerceIn(0, 100)
                        else 0
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                    )
                                    Text(
                                        text = "${formatAmount(goal.currentAmount, uiState.currency.code)} saved",
                                        style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
                                    )
                                }
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.extendedColors.brandAccent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            )
        }
    }
}
