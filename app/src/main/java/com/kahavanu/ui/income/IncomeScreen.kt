package com.kahavanu.ui.income

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

enum class IncomeFilter {
    ALL, PENDING
}

@Composable
fun IncomeScreen(
    onLogIncome: () -> Unit,
    viewModel: IncomeOverviewViewModel = hiltViewModel(),
) {
    val logs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    val totalForMonth by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val breakdowns by viewModel.breakdowns.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val monthLabel = currentMonthLabel()
    var selectedFilter by remember { mutableStateOf(IncomeFilter.ALL) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Emerald.Emerald50.copy(alpha = 0.5f),
                        RawColors.Slate.Slate100
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 120.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
    ) {
        item { IncomeHeader() }
        item {
            TotalExpectedCard(
                totalForMonth = totalForMonth,
                currency = currency,
                monthLabel = monthLabel,
                breakdowns = breakdowns,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }
        item { IncomeActionButtons(onLogIncome = onLogIncome) }
        item { MatchAndCatchSection() }
        item { PersistenceSection() }
        item { CryptoGatewaySection() }
        item { IncomeLogSection(logs = logs) }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                spotColor = RawColors.Gray.Gray400,
                ambientColor = RawColors.Gray.Gray500,
                shape = KahavanuShapes.large
            ),
        shape = KahavanuShapes.large,
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(content = content)
    }
}

@Composable
private fun ActionGlassCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                spotColor = RawColors.Gray.Gray400,
                ambientColor = RawColors.Gray.Gray500,
                shape = KahavanuShapes.large
            ),
        shape = KahavanuShapes.large,
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun LiquidEmeraldIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(Elevation.level2, CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Emerald.Emerald400.copy(alpha = 0.9f),
                        RawColors.Emerald.Emerald400.copy(alpha = 0.75f),
                        RawColors.Emerald.Emerald400.copy(alpha = 0.9f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun IncomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Income",
            style = MaterialTheme.typography.bodyLarge,
            color = RawColors.Emerald.Emerald700
        )
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = "Wealth in the Air",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RawColors.Emerald.Emerald900
        )
    }
}

@Composable
private fun TotalExpectedCard(
    totalForMonth: Double,
    currency: String,
    monthLabel: String,
    breakdowns: List<IncomeBreakdownItem>,
    selectedFilter: IncomeFilter,
    onFilterSelected: (IncomeFilter) -> Unit,
) {
    // If Pending is selected, we show 0 for now as it's not yet implemented in the data layer
    val displayTotal = if (selectedFilter == IncomeFilter.ALL) totalForMonth else 0.0
    val totalText = formatAmount(displayTotal, currency)
    val progress = if (displayTotal > 0.0) 1f else 0f
    val progressLabel = "${(progress * 100).roundToInt()}%"
    val receivedLabel = when {
        selectedFilter == IncomeFilter.PENDING -> "Pending payments this month"
        displayTotal > 0.0 -> "$totalText received"
        else -> "No income received"
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total received · $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RawColors.Slate.Slate500
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = totalText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = RawColors.Emerald.Emerald700
                    )
                }

                Row(
                    modifier = Modifier
                        .background(RawColors.Slate.Slate100.copy(alpha = 0.5f), shape = KahavanuShapes.small)
                        .padding(2.dp)
                ) {
                    IncomeFilterTab(
                        text = "All",
                        isSelected = selectedFilter == IncomeFilter.ALL,
                        onClick = { onFilterSelected(IncomeFilter.ALL) }
                    )
                    IncomeFilterTab(
                        text = "Pending",
                        isSelected = selectedFilter == IncomeFilter.PENDING,
                        onClick = { onFilterSelected(IncomeFilter.PENDING) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receivedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = RawColors.Slate.Slate500
                )
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RawColors.Emerald.Emerald600
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
            )

            if (selectedFilter == IncomeFilter.ALL && breakdowns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))
                
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    breakdowns.forEach { item ->
                        SummaryItem(
                            label = item.label,
                            value = formatAmount(item.amount, currency),
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
                color = RawColors.Slate.Slate500
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = RawColors.Slate.Slate900
        )
    }
}

@Composable
private fun IncomeFilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
        label = "tabBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) RawColors.Slate.Slate900 else RawColors.Slate.Slate500,
        label = "tabText"
    )

    Box(
        modifier = Modifier
            .clip(KahavanuShapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun IncomeActionButtons(onLogIncome: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        ActionCard(
            icon = Icons.Outlined.AddCircle,
            title = "Log Income",
            subtitle = "Track one-time, recurrent, or pending income",
            onClick = onLogIncome,
        )
        ActionCard(
            icon = Icons.Outlined.Refresh,
            title = "Recurrent Income",
            subtitle = "View and manage recurring income streams",
            onClick = { },
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ActionGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        LiquidEmeraldIconBox(icon = icon)
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RawColors.Slate.Slate900
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = RawColors.Slate.Slate500
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RawColors.Slate.Slate400
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, actionText: String? = null, badgeCount: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RawColors.Slate.Slate900
                )
                if (badgeCount != null) {
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        RawColors.Emerald.Emerald400,
                                        RawColors.Emerald.Emerald500
                                    )
                                ),
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeCount,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = RawColors.Slate.Slate500
            )
        }
        if (actionText != null) {
            TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                Text(text = actionText, color = RawColors.Emerald.Emerald600, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = RawColors.Emerald.Emerald600)
            }
        }
    }
    Spacer(modifier = Modifier.height(Spacing.medium))
}

@Composable
private fun MatchAndCatchSection() {
    Column {
        SectionHeader(
            title = "Match & Catch",
            subtitle = "Unmatched deposits from SMS",
            badgeCount = "2"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            MatchItem(
                source = "Commercial Bank",
                time = "Today, 10:42",
                amount = "LKR 50,000",
                matchPercent = "86% match",
                likelyFor = "Likely for SME WordPress build"
            )
            HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
            MatchItem(
                source = "Sampath Bank",
                time = "Yesterday",
                amount = "LKR 18,500",
                matchPercent = "64% match",
                likelyFor = "Likely for Logo retainer · Aprco"
            )
        }
    }
}

@Composable
private fun MatchItem(source: String, time: String, amount: String, matchPercent: String, likelyFor: String) {
    Column(modifier = Modifier.padding(Spacing.large)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "$source · $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = RawColors.Slate.Slate500
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RawColors.Slate.Slate900
                )
            }
            Box(
                modifier = Modifier
                    .background(RawColors.Emerald.Emerald50.copy(alpha = 0.7f), shape = KahavanuShapes.small)
                    .border(0.5.dp, RawColors.Emerald.Emerald200, KahavanuShapes.small)
                    .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
            ) {
                Text(
                    text = matchPercent,
                    style = MaterialTheme.typography.labelSmall,
                    color = RawColors.Emerald.Emerald700,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.small))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = RawColors.Slate.Slate400, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(Spacing.extraSmall))
            Text(text = likelyFor, style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate600)
        }
        Spacer(modifier = Modifier.height(Spacing.medium))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Button(
                onClick = { },
                shape = KahavanuShapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = RawColors.Slate.Slate900)
            ) {
                Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.extraSmall))
                Text("Link this one")
            }
            OutlinedButton(
                onClick = { },
                shape = KahavanuShapes.small,
                border = BorderStroke(1.dp, RawColors.Slate.Slate300)
            ) {
                Text("It's new", color = RawColors.Slate.Slate700)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Outlined.Close, contentDescription = "Dismiss", tint = RawColors.Slate.Slate400)
            }
        }
    }
}

@Composable
private fun PersistenceSection() {
    Column {
        SectionHeader(
            title = "Persistence",
            subtitle = "Pending & overdue payments",
            actionText = "View all"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            PersistenceItem(
                title = "Nimal — React build",
                dueText = "Due 3 days ago",
                isOverdue = true,
                statusText = "Invoice sent",
                amount = "LKR 40,000",
                hasNudge = true
            )
            HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
            PersistenceItem(
                title = "Aprco — Logo retainer",
                dueText = "Due in 4 days",
                isOverdue = false,
                statusText = "Invoice sent",
                amount = "LKR 18,500",
                hasNudge = false
            )
            HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
            PersistenceItem(
                title = "BlogX — Article batch",
                dueText = "Due in 8 days",
                isOverdue = false,
                statusText = "No invoice",
                amount = "$ 120",
                hasNudge = false
            )
        }
    }
}

@Composable
private fun PersistenceItem(title: String, dueText: String, isOverdue: Boolean, statusText: String, amount: String, hasNudge: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                .border(0.5.dp, RawColors.Slate.Slate200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(title.first().toString(), fontWeight = FontWeight.Bold, color = RawColors.Slate.Slate700)
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = RawColors.Slate.Slate900)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) RawColors.Red.Red500 else RawColors.Slate.Slate500
                )
                Text(text = " • ", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate300)
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = RawColors.Slate.Slate400
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = RawColors.Slate.Slate500
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RawColors.Slate.Slate900)
            if (hasNudge) {
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                OutlinedButton(
                    onClick = { },
                    contentPadding = PaddingValues(horizontal = Spacing.small, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    shape = KahavanuShapes.small,
                    border = BorderStroke(1.dp, RawColors.Slate.Slate300)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = RawColors.Slate.Slate700)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nudge", style = MaterialTheme.typography.labelSmall, color = RawColors.Slate.Slate700)
                }
            }
        }
    }
}

@Composable
private fun CryptoGatewaySection() {
    Column {
        SectionHeader(
            title = "Crypto Gateway",
            subtitle = "P2P flow → disposable cash"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(RawColors.Emerald.Emerald50.copy(alpha = 0.8f), CircleShape)
                                    .border(0.5.dp, RawColors.Emerald.Emerald200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = RawColors.Emerald.Emerald600, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text("Money in", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate500)
                        }
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text("$ 540", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RawColors.Slate.Slate900)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(RawColors.Red.Red50.copy(alpha = 0.8f), CircleShape)
                                    .border(0.5.dp, RawColors.Red.Red200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = RawColors.Red.Red600, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text("Money out", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate500)
                        }
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text("$ 380", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RawColors.Slate.Slate900)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Net position", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate500)
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text("LKR 51,200", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = RawColors.Emerald.Emerald700)
                    }
                    Box(
                        modifier = Modifier
                            .background(RawColors.Emerald.Emerald50.copy(alpha = 0.8f), shape = KahavanuShapes.small)
                            .border(0.5.dp, RawColors.Emerald.Emerald200, KahavanuShapes.small)
                            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
                    ) {
                        Text(
                            text = "Profit",
                            style = MaterialTheme.typography.labelSmall,
                            color = RawColors.Emerald.Emerald700,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Withdrawn to bank · $ 250", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate500)
                    TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                        Text("Verify deposit", style = MaterialTheme.typography.labelMedium, color = RawColors.Emerald.Emerald600, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeLogSection(logs: List<IncomeLogEntry>) {
    Column {
        SectionHeader(
            title = "Income Log",
            subtitle = "Recent payments",
            actionText = "View all"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (logs.isEmpty()) {
                Text(
                    text = "No income logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RawColors.Slate.Slate500,
                    modifier = Modifier.padding(Spacing.large),
                )
            } else {
                logs.forEachIndexed { index, log ->
                    LogItem(
                        title = log.title,
                        type = log.note?.takeIf { it.isNotBlank() } ?: "Income",
                        date = formatDate(log.receivedAtEpochMillis),
                        amount = formatAmount(log.amount, log.currency),
                    )
                    if (index != logs.lastIndex) {
                        HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(title: String, type: String, date: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                .border(0.5.dp, RawColors.Slate.Slate200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = RawColors.Emerald.Emerald500, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = RawColors.Slate.Slate900)
            Text(text = "$type • $date", style = MaterialTheme.typography.bodySmall, color = RawColors.Slate.Slate500)
        }
        Text(text = amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RawColors.Slate.Slate900)
    }
}

private fun formatAmount(amount: Double, currency: String): String {
    val formatted = String.format(Locale.getDefault(), "%,.2f", amount)
    return "$currency $formatted"
}

private fun formatDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$month ${date.dayOfMonth}, ${date.year}"
}

private fun currentMonthLabel(): String {
    val month = YearMonth.now()
    return month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}
