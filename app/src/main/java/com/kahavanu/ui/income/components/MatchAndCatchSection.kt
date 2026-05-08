package com.kahavanu.ui.income.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.Spacing

@Composable
fun MatchAndCatchSection() {
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
    val figmaGreen = Color(0xFF00BC7D)
    
    Column(modifier = Modifier.padding(Spacing.large)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    text = "$source · $time",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.06.sp
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    letterSpacing = (-0.8).sp
                )
            }
            
            Surface(
                color = figmaGreen.copy(alpha = 0.12f),
                shape = CircleShape
            ) {
                Text(
                    text = matchPercent.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryEmerald,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RawColors.Slate.Slate900.copy(alpha = 0.03f),
            shape = KahavanuShapes.large,
            border = BorderStroke(1.16.dp, RawColors.Slate.Slate500.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = figmaGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextSecondary)) {
                            append("Likely for ")
                        }
                        withStyle(style = SpanStyle(color = TextPrimary, fontWeight = FontWeight.Medium)) {
                            append(likelyFor.removePrefix("Likely for "))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .shadow(elevation = 18.dp, spotColor = Color.Black.copy(alpha = 0.25f), shape = CircleShape),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
                                    RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
                                    RawColors.Emerald.Emerald500.copy(alpha = 0.9f)
                                )
                            )
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
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.extraSmall))
                        Text(
                            text = "Link this one",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Surface(
                onClick = { },
                modifier = Modifier.height(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.6f),
                border = BorderStroke(1.16.dp, RawColors.Slate.Slate900.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "It's new",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Surface(
                onClick = { },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.6f),
                border = BorderStroke(1.16.dp, RawColors.Slate.Slate900.copy(alpha = 0.08f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
