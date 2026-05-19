package com.kahavanu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    currentSession: UserSession?,
    onGoalClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background glowing ambient circles matching Figma coordinates
        AmbientGlow(
            color = RawColors.Emerald.Emerald400.copy(alpha = 0.18f),
            size = 360.dp,
            modifier = Modifier
                .offset(x = (-96).dp, y = (-128).dp)
        )

        AmbientGlow(
            color = RawColors.Emerald.Emerald400.copy(alpha = 0.12f),
            size = 320.dp,
            modifier = Modifier
                .offset(x = 170.dp, y = 284.dp)
        )

        AmbientGlow(
            color = RawColors.Slate.Slate900.copy(alpha = 0.06f),
            size = 300.dp,
            modifier = Modifier
                .offset(x = 98.dp, y = 648.dp)
        )

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp) // Provide spacing to avoid floating BottomNavBar overlap
        ) {
            // Page Title
            Spacer(modifier = Modifier.height(Spacing.large))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                ScreenHeader(
                    label = "Overview",
                    title = "Kahavanu"
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // The Treasure Card Section (Goal)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                val activeGoal = uiState.featuredGoal
                val goalTitle = activeGoal?.title ?: "MacBook Pro M4"
                val goalSaved = activeGoal?.currentAmount ?: 11200.0
                val goalTarget = activeGoal?.targetAmount ?: 490000.0
                val goalProgress = if (goalTarget > 0) (goalSaved / goalTarget).toFloat() else 0f
                val progressLabel = "${(goalProgress * 100).toInt()}%"

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
                            
                            // Badge indicating goal progress percentage
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

                        // High fidelity Linear progress bar
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

                        // Stash tip card containing motivational statement
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

            Spacer(modifier = Modifier.height(Spacing.extraLarge))

            // The Sieve (SMS Suggestions horizontal row)
            if (uiState.sieveItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.extraLarge),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(
                            title = "The Sieve",
                            subtitle = "SMS suggestions to confirm",
                            actionText = "See all",
                            badgeCount = uiState.sieveItems.size.toString()
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.extraLarge),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        items(uiState.sieveItems) { item ->
                            SieveCard(
                                item = item,
                                onConfirm = { viewModel.confirmSieveItem(item.id) },
                                onIgnore = { viewModel.dismissSieveItem(item.id) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.extraLarge))
            }

            // The Streams Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                SectionHeader(
                    title = "The Streams",
                    subtitle = "Unified income snapshot",
                    actionText = "Details",
                    onActionClick = onIncomeClick
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIncomeClick() }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.incomeStreams.forEachIndexed { index, stream ->
                            IncomeStreamRow(stream = stream)
                            if (index < uiState.incomeStreams.lastIndex) {
                                HorizontalDivider(
                                    color = RawColors.Slate.Slate200.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = Spacing.medium)
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
fun AmbientGlow(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(80.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent)
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun SieveCard(
    item: SieveItem,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = item.type == SieveType.EXPENSE
    
    // Choose icon and color configurations for card categories
    val icon = if (isExpense) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp
    val categoryColor = if (isExpense) RawColors.Red.Red500 else RawColors.Emerald.Emerald600
    val categoryBg = if (isExpense) RawColors.Red.Red50.copy(alpha = 0.5f) else RawColors.Emerald.Emerald50.copy(alpha = 0.5f)
    val badgeLabel = if (isExpense) "Expense" else "Income"

    Surface(
        modifier = modifier
            .width(240.dp)
            .shadow(Elevation.level3, shape = RoundedCornerShape(CornerRadius.large)),
        shape = RoundedCornerShape(CornerRadius.large),
        color = Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RawColors.Slate.Slate200.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top small category icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(categoryBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badgeLabel,
                        tint = categoryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Small classification pill
                Surface(
                    color = categoryBg,
                    shape = CircleShape
                ) {
                    Text(
                        text = badgeLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${item.currency} ${String.format("%,.0f", item.amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            Text(
                text = item.detectedFrom,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confirms matching and adds record to dynamic flow
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = RawColors.Emerald.Emerald500),
                    contentPadding = PaddingValues(horizontal = Spacing.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Confirm",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.extraSmall))
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Ignores matching suggestion and dismisses it
                IconButton(
                    onClick = onIgnore,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, RawColors.Slate.Slate200, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Ignore",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeStreamRow(
    stream: IncomeStreamItem,
    modifier: Modifier = Modifier
) {
    val icon = when (stream.iconIndex) {
        12 -> Icons.Outlined.AccountBalanceWallet
        13 -> Icons.Outlined.Language
        else -> Icons.Outlined.CurrencyBitcoin
    }
    
    val iconBg = when (stream.iconIndex) {
        12 -> RawColors.Slate.Slate100.copy(alpha = 0.5f)
        13 -> RawColors.Emerald.Emerald100.copy(alpha = 0.45f)
        else -> RawColors.Amber.Amber100.copy(alpha = 0.45f)
    }
    
    val iconColor = when (stream.iconIndex) {
        12 -> RawColors.Slate.Slate700
        13 -> RawColors.Emerald.Emerald600
        else -> RawColors.Amber.Amber600
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stream Icon Container
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        // Title and pending status information
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stream.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = stream.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // Received Aggregate Amount Column
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Received",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = stream.receivedFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
