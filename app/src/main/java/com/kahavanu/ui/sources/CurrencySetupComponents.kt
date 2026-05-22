package com.kahavanu.ui.sources

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun CurrencySetup(
    primaryCurrency: CurrencyOption,
    secondaryCurrency: CurrencyOption,
    onPrimaryChange: (CurrencyOption) -> Unit,
    onSecondaryChange: (CurrencyOption) -> Unit,
    onSave: () -> Unit,
) {
    var primaryExpanded by remember { mutableStateOf(false) }
    var secondaryExpanded by remember { mutableStateOf(false) }

    val currencies = remember { CurrencyOption.values().toList() }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SectionLabel("Currencies")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = KahavanuShapes.large,
            color = Color.White.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Primary Currency Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                primaryExpanded = !primaryExpanded
                                if (primaryExpanded) {
                                    secondaryExpanded = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Primary currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 13.sp,
                                color = TextPrimary,
        )
                            Text(
                                text = "${primaryCurrency.fullName} · ${primaryCurrency.code}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        val rotationAngle by animateFloatAsState(
                            targetValue = if (primaryExpanded) 180f else 0f,
                            label = "PrimaryArrowRotation"
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Select Primary Currency",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotationAngle)
                        )
                    }

                    AnimatedVisibility(
                        visible = primaryExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currencies.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { option ->
                                        CurrencyOptionCard(
                                            currency = option,
                                            symbol = option.symbol,
                                            fullName = option.fullName,
                                            selected = primaryCurrency == option,
                                            onClick = {
                                                onPrimaryChange(option)
                                                onSave()
                                                primaryExpanded = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    thickness = 1.dp
                )

                // Secondary Currency Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                secondaryExpanded = !secondaryExpanded
                                if (secondaryExpanded) {
                                    primaryExpanded = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.extendedColors.iconMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Secondary currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 13.sp,
                                color = TextPrimary,
        )
                            Text(
                                text = "${secondaryCurrency.fullName} · ${secondaryCurrency.code}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        val rotationAngle by animateFloatAsState(
                            targetValue = if (secondaryExpanded) 180f else 0f,
                            label = "SecondaryArrowRotation"
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Select Secondary Currency",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotationAngle)
                        )
                    }

                    AnimatedVisibility(
                        visible = secondaryExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currencies.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { option ->
                                        CurrencyOptionCard(
                                            currency = option,
                                            symbol = option.symbol,
                                            fullName = option.fullName,
                                            selected = secondaryCurrency == option,
                                            onClick = {
                                                onSecondaryChange(option)
                                                onSave()
                                                secondaryExpanded = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyOptionCard(
    currency: CurrencyOption,
    symbol: String,
    fullName: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) MaterialTheme.extendedColors.brandAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = if (selected) Color.White else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
