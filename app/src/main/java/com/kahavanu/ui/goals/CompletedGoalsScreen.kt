package com.kahavanu.ui.goals

import com.kahavanu.ui.util.goalCategoryColor
import com.kahavanu.ui.util.goalCategoryIcon
import com.kahavanu.ui.util.formatAmount
import com.kahavanu.ui.theme.IconMuted
import com.kahavanu.ui.theme.Romance
import com.kahavanu.ui.theme.UtilityAccent
import com.kahavanu.ui.theme.Warning
import com.kahavanu.ui.theme.Info
import com.kahavanu.ui.theme.Primary
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalAdjustmentLog
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.ui.common.EmptyState
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CompletedGoalsScreen(
    onBack: () -> Unit,
    viewModel: CompletedGoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
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
                        text = "Completed Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "${uiState.goals.size} goal${if (uiState.goals.size == 1) "" else "s"} achieved",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = TextSize.sm,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (uiState.goals.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.CheckCircle,
                    title = "No completed goals yet",
                    subtitle = "Keep saving — you'll get there!",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Spacing.large),
                ) {
                    items(uiState.goals, key = { it.id }) { goal ->
                        CompletedGoalCard(
                            goal = goal,
                            currency = uiState.currency,
                            logsFlow = { viewModel.observeLogsForGoal(goal.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedGoalCard(
    goal: GoalEntry,
    currency: CurrencyOption,
    logsFlow: () -> kotlinx.coroutines.flow.Flow<List<GoalAdjustmentLog>>,
) {
    var expanded by remember { mutableStateOf(false) }
    val logs by remember { logsFlow() }.collectAsState(initial = emptyList())
    val completedDate = Instant.ofEpochMilli(goal.lastUpdatedEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val color = goalCategoryColor(goal.category)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.extendedColors.brandBorder, RoundedCornerShape(16.dp)),
    ) {
        // Card header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = goalCategoryIcon(goal.category),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = TextSize.base,
                    )
                    Text(
                        text = "Completed ${completedDate.format(formatter)}",
                        style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmount(goal.currentAmount, currency.code),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
        )
                Text(
                    text = "Target: ${formatAmount(goal.targetAmount, currency.code)}",
                    style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(Spacing.small))
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        // Expandable log section
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.large)
                    .padding(bottom = Spacing.large),
            ) {
                HorizontalDivider(color = MaterialTheme.extendedColors.brandBorder)
                Spacer(modifier = Modifier.height(Spacing.medium))
                Text(
                    text = "Savings Log",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontSize = TextSize.xs,
                    modifier = Modifier.padding(bottom = Spacing.small),
                )
                if (logs.isEmpty()) {
                    Text(
                        text = "No adjustment history recorded",
                        style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        logs.forEach { log ->
                            AdjustmentLogRow(log = log, currency = currency)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentLogRow(
    log: GoalAdjustmentLog,
    currency: CurrencyOption,
) {
    val isAddition = log.delta >= 0
    val color = if (isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.extendedColors.romance
    val icon = if (isAddition) Icons.Outlined.Add else Icons.Outlined.Remove
    val date = Instant.ofEpochMilli(log.timestampEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(Spacing.extraSmall))
            Text(
                text = "${if (isAddition) "+" else "−"} ${formatAmount(Math.abs(log.delta), currency.code)}",
                style = MaterialTheme.typography.labelSmall,
            color = color,
                fontSize = TextSize.xs,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.labelSmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
            )
            Text(
                text = "Balance: ${formatAmount(log.newAmount, currency.code)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
            )
        }
    }
}

