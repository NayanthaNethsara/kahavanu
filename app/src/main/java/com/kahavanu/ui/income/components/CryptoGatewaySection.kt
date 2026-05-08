package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondaryEmerald
import com.kahavanu.ui.theme.TextTertiaryEmerald

@Composable
fun CryptoGatewaySection() {
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
                            Text("Money in", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text("$ 540", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            Text("Money out", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text("$ 380", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Net position", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text("LKR 51,200", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            color = TextSecondaryEmerald,
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
                    Text("Withdrawn to bank · $ 250", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                        Text("Verify deposit", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
