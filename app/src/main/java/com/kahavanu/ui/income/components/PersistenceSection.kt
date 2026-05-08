package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun PersistenceSection() {
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
                .size(44.dp)
                .background(RawColors.Slate.Slate50, shape = KahavanuShapes.medium)
                .border(0.5.dp, RawColors.Slate.Slate200, KahavanuShapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = RawColors.Slate.Slate700
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = RawColors.Slate.Slate900
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) RawColors.Red.Red500 else RawColors.Slate.Slate500
                )
                Text(text = " · ", style = MaterialTheme.typography.labelSmall, color = RawColors.Slate.Slate300)
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = RawColors.Slate.Slate400
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = RawColors.Slate.Slate900
            )
            if (hasNudge) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    onClick = { },
                    shape = CircleShape,
                    color = RawColors.Slate.Slate900,
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nudge",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
