package com.kahavanu.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.extendedColors

@Composable
fun TreasureCard(
    activeGoal: GoalEntry?,
    onGoalClick: () -> Unit,
    onCreateGoalClick: () -> Unit = onGoalClick,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (activeGoal == null) {
            ActiveGoalEmptyState(onCreateGoalClick = onCreateGoalClick)
        } else {
            ActiveGoalCard(activeGoal = activeGoal, onGoalClick = onGoalClick)
        }
    }
}

@Composable
private fun ActiveGoalCard(
    activeGoal: GoalEntry,
    onGoalClick: () -> Unit,
) {
    val goalProgress = if (activeGoal.targetAmount > 0) {
        (activeGoal.currentAmount / activeGoal.targetAmount).toFloat()
    } else 0f
    val progressLabel = "${(goalProgress * 100).toInt()}%"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoalClick() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = activeGoal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = CircleShape,
                ) {
                    Text(
                        text = progressLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.brandText,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            LinearProgressIndicator(
                progress = { goalProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.extendedColors.brandAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Total stash",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = "${activeGoal.currency.code} ${String.format("%,.0f", activeGoal.currentAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = "${activeGoal.currency.code} ${String.format("%,.0f", activeGoal.targetAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.5f),
                shape = RoundedCornerShape(CornerRadius.large),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "You are earning faster than you are spending this week. Keep it up!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.extendedColors.brandText,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveGoalEmptyState(
    onCreateGoalClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateGoalClick() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.brandAccent,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = "No active goal yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            Text(
                text = "Pick a target to chase and Kahavanu will track your savings momentum here.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Surface(
                shape = RoundedCornerShape(CornerRadius.large),
                color = MaterialTheme.extendedColors.brandAccent,
                modifier = Modifier.clickable { onCreateGoalClick() },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Set Your First Goal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}
