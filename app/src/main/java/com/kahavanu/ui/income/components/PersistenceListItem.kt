package com.kahavanu.ui.income.components

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun PersistenceListItem(
    title: String,
    dueText: String,
    isOverdue: Boolean,
    isPending: Boolean,
    isRecurrent: Boolean,
    amount: String,
    isInvoiceSent: Boolean,
    onMarkAsReceived: (() -> Unit)? = null,
    onNudge: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when {
                        isOverdue -> MaterialTheme.extendedColors.dangerAccent.copy(alpha = 0.1f)
                        isRecurrent -> MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.14f)
                        !isPending -> MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.14f)
                        else -> MaterialTheme.extendedColors.warningAccent.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isOverdue -> Icons.Outlined.ErrorOutline
                    isRecurrent -> Icons.Default.Autorenew
                    !isPending -> Icons.Default.CheckCircle
                    else -> Icons.Outlined.Schedule
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when {
                    isOverdue -> MaterialTheme.colorScheme.error
                    isRecurrent -> MaterialTheme.colorScheme.primary
                    !isPending -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.extendedColors.warning
                }
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        !isPending -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.extendedColors.warning
                    },)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
        )
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isInvoiceSent) TextSecondary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isInvoiceSent) "Invoice sent" else "No invoice",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isInvoiceSent) TextSecondary else MaterialTheme.colorScheme.error,
        )
            }
        }
        
        // Amount and Action Column
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                if (isPending && onMarkAsReceived != null) {
                    Surface(
                        onClick = onMarkAsReceived,
                        modifier = Modifier.height(24.dp),
                        shape = CircleShape,
                        color = MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Receive",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.extendedColors.brandText,
            fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isOverdue) {
                    Surface(
                        onClick = { onNudge?.invoke() },
                        modifier = Modifier
                            .height(24.dp)
                            .width(72.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
                                            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.75f),
                                            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Nudge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
        )
                            }
                        }
                    }
                }
            }
        }
    }
}
