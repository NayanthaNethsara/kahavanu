package com.kahavanu.ui.income

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun IncomeScreen() {
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
            top = 110.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
    ) {
        item { IncomeHeader() }
        item { TotalExpectedCard() }
        item { IncomeActionButtons() }
        item { MatchAndCatchSection() }
        item { PersistenceSection() }
        item { CryptoGatewaySection() }
        item { IncomeLogSection() }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
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
    content: @Composable RowScope.() -> Unit
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
            color = RawColors.Slate.Slate500
        )
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = "Wealth in the Air",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RawColors.Slate.Slate900
        )
    }
}

@Composable
private fun TotalExpectedCard() {
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
                        text = "Total expected · May",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RawColors.Slate.Slate500
                    )
                    Text(
                        text = "LKR 447,120",
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
                    TextButton(
                        onClick = { },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), shape = KahavanuShapes.small)
                            .shadow(Elevation.level1, shape = KahavanuShapes.small),
                        contentPadding = PaddingValues(horizontal = Spacing.small, vertical = Spacing.extraSmall)
                    ) {
                        Text("LKR", style = MaterialTheme.typography.labelMedium, color = RawColors.Slate.Slate900)
                    }
                    TextButton(
                        onClick = { },
                        contentPadding = PaddingValues(horizontal = Spacing.small, vertical = Spacing.extraSmall)
                    ) {
                        Text("USD", style = MaterialTheme.typography.labelMedium, color = RawColors.Slate.Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LKR 306,720 received",
                    style = MaterialTheme.typography.bodySmall,
                    color = RawColors.Slate.Slate500
                )
                Text(
                    text = "69%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RawColors.Emerald.Emerald600
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            LinearProgressIndicator(
                progress = { 0.69f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = RawColors.Emerald.Emerald500,
                trackColor = RawColors.Slate.Slate100.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun IncomeActionButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        ActionCard(
            icon = Icons.Outlined.AddCircle,
            title = "Log Income",
            subtitle = "Track one-time, recurrent, or pending income"
        )
        ActionCard(
            icon = Icons.Outlined.Refresh,
            title = "Recurrent Income",
            subtitle = "View and manage recurring income streams"
        )
    }
}

@Composable
private fun ActionCard(icon: ImageVector, title: String, subtitle: String) {
    ActionGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
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
private fun IncomeLogSection() {
    Column {
        SectionHeader(
            title = "Income Log",
            subtitle = "Recent payments",
            actionText = "View all"
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            LogItem(
                title = "ACME Corp",
                type = "Salary",
                date = "May 1, 2026",
                amount = "LKR 120,000"
            )
            HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
            LogItem(
                title = "SME WordPress build",
                type = "Freelance",
                date = "May 3, 2026",
                amount = "LKR 50,000"
            )
            HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.5f))
            LogItem(
                title = "Blog revenue",
                type = "AdSense",
                date = "May 2, 2026",
                amount = "$ 280"
            )
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
