package com.kahavanu.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun TreasureCard(
    activeGoal: GoalEntry?,
    onGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goalTitle = activeGoal?.title ?: "MacBook Pro M4"
    val goalSaved = activeGoal?.currentAmount ?: 11200.0
    val goalTarget = activeGoal?.targetAmount ?: 490000.0
    val goalProgress = if (goalTarget > 0) (goalSaved / goalTarget).toFloat() else 0f
    val progressLabel = "${(goalProgress * 100).toInt()}%"

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "The Treasure",
            subtitle = "Target Goal Progress",
            actionText = "Details",
            onActionClick = onGoalClick
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onGoalClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goalTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        color = RawColors.Emerald.Emerald100.copy(alpha = 0.45f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = progressLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = RawColors.Emerald.Emerald700,
                            fontWeight = FontWeight.Bold
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
                    color = RawColors.Emerald.Emerald500,
                    trackColor = RawColors.Slate.Slate100,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total stash",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "LKR ${String.format("%,.0f", goalSaved)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "LKR ${String.format("%,.0f", goalTarget)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RawColors.Emerald.Emerald600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RawColors.Emerald.Emerald50.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(CornerRadius.large),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RawColors.Emerald.Emerald100.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Tips",
                            tint = RawColors.Emerald.Emerald600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = "You are earning faster than you are spending this week. Keep it up!",
                            style = MaterialTheme.typography.bodySmall,
                            color = RawColors.Emerald.Emerald700,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
