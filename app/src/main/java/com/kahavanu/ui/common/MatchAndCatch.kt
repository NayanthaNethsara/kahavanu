package com.kahavanu.ui.common

import androidx.compose.material3.MaterialTheme
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.TextSize

data class MatchItemState(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val matchPercent: Int,
    val likelyFor: String,
    val icon: ImageVector? = null,
    val iconTint: Color? = null,
    val primaryActionLabel: String = "Link this one",
    val secondaryActionLabel: String = "It's new"
)

@Composable
fun MatchingSection(
    title: String,
    subtitle: String,
    items: List<MatchItemState>,
    onPrimaryAction: (MatchItemState) -> Unit,
    onSecondaryAction: (MatchItemState) -> Unit,
    onDismiss: (MatchItemState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            badgeCount = if (items.isNotEmpty()) items.size.toString() else null
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                MatchItem(
                    item = item,
                    onPrimaryAction = { onPrimaryAction(item) },
                    onSecondaryAction = { onSecondaryAction(item) },
                    onDismiss = { onDismiss(item) }
                )
                if (index != items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun MatchItem(
    item: MatchItemState,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val figmaGreen = Color(0xFF00BC7D)
    
    Column(modifier = Modifier.padding(Spacing.large)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    text = "${item.title} · ${item.subtitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = TextSize.xs,
                    letterSpacing = 0.06.sp
                )
                Text(
                    text = item.amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontSize = TextSize.lg,
                    letterSpacing = (-0.8).sp
                )
            }
            
            Surface(
                color = figmaGreen.copy(alpha = 0.12f),
                shape = CircleShape
            ) {
                Text(
                    text = "${item.matchPercent}% MATCH",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryEmerald,
                    fontWeight = FontWeight.Medium,
                    fontSize = TextSize.xs,
                    letterSpacing = 0.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
            shape = KahavanuShapes.large,
            border = BorderStroke(1.16.dp, MaterialTheme.extendedColors.iconMuted.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon ?: Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = item.iconTint ?: figmaGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextSecondary)) {
                            append("Likely for ")
                        }
                        withStyle(style = SpanStyle(color = TextPrimary, fontWeight = FontWeight.Medium)) {
                            append(item.likelyFor.removePrefix("Likely for "))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.sm
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
                onClick = onPrimaryAction,
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
                                    MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
                                    MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.75f),
                                    MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f)
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
                            text = item.primaryActionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = TextSize.sm,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Surface(
                onClick = onSecondaryAction,
                modifier = Modifier.height(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.6f),
                border = BorderStroke(1.16.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.secondaryActionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = TextSize.sm,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Surface(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.6f),
                border = BorderStroke(1.16.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
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
