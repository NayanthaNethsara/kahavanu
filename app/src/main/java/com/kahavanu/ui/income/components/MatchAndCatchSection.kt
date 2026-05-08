package com.kahavanu.ui.income.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
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
    Column(modifier = Modifier.padding(Spacing.large)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$source · $time",
                    style = MaterialTheme.typography.labelSmall,
                    color = RawColors.Slate.Slate500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = RawColors.Slate.Slate900
                )
            }
            
            Box(
                modifier = Modifier
                    .background(RawColors.Emerald.Emerald50, shape = CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RawColors.Slate.Slate50.copy(alpha = 0.5f), shape = KahavanuShapes.small)
                .padding(Spacing.small)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = RawColors.Emerald.Emerald600,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.extraSmall))
                Text(
                    text = likelyFor,
                    style = MaterialTheme.typography.bodySmall,
                    color = RawColors.Slate.Slate600
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                shape = KahavanuShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = RawColors.Slate.Slate900),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Match Now", style = MaterialTheme.typography.labelMedium)
            }
            
            Surface(
                onClick = { },
                shape = KahavanuShapes.medium,
                color = RawColors.Slate.Slate100.copy(alpha = 0.5f),
                border = BorderStroke(0.5.dp, RawColors.Slate.Slate200)
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("New Entry", style = MaterialTheme.typography.labelMedium, color = RawColors.Slate.Slate700)
                }
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = RawColors.Slate.Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
