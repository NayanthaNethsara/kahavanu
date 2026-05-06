package com.kahavanu.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
) {
    val items = listOf(
        BottomNavItem(AppDestination.Home, "Home", Icons.Outlined.Home),
        BottomNavItem(AppDestination.Income, "Income", Icons.AutoMirrored.Outlined.TrendingUp),
        BottomNavItem(AppDestination.Expenses, "Expenses", Icons.AutoMirrored.Outlined.TrendingDown),
        BottomNavItem(AppDestination.Goals, "Goal", Icons.Outlined.Radar),
        BottomNavItem(AppDestination.Profile, "Profile", Icons.Outlined.Person),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.large, vertical = Spacing.large)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    spotColor = RawColors.Gray.Gray400,
                    ambientColor = RawColors.Gray.Gray500
                ),
            CircleShape,
            color = Color.White.copy(alpha = 0.85f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            var totalWidth by remember { mutableFloatStateOf(0f) }
            val coroutineScope = rememberCoroutineScope()
            
            val activeIndex = items.indexOfFirst { it.destination.route == currentRoute }.coerceAtLeast(0)
            val targetBias = (activeIndex / (items.size - 1).toFloat()) * 2 - 1

            val indicatorBias = remember { Animatable(targetBias) }
            var isDragging by remember { mutableStateOf(false) }

            val indicatorScale by animateFloatAsState(
                targetValue = if (isDragging) 1.15f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
                label = "indicatorScale"
            )

            LaunchedEffect(targetBias) {
                if (!isDragging) {
                    indicatorBias.animateTo(
                        targetValue = targetBias,
                        animationSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = 400f
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = Spacing.small)
                    .onSizeChanged { totalWidth = it.width.toFloat() }
                    .pointerInput(totalWidth) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val indexFloat = (indicatorBias.value + 1f) / 2f * (items.size - 1)
                                val nearestIndex = indexFloat.roundToInt().coerceIn(0, items.size - 1)
                                val predictedBias = (nearestIndex / (items.size - 1).toFloat()) * 2 - 1
                                
                                coroutineScope.launch {
                                    indicatorBias.animateTo(
                                        targetValue = predictedBias,
                                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                                    )
                                }
                                
                                if (nearestIndex != activeIndex) {
                                    onNavigate(items[nearestIndex].destination)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                val indexFloat = (indicatorBias.value + 1f) / 2f * (items.size - 1)
                                val nearestIndex = indexFloat.roundToInt().coerceIn(0, items.size - 1)
                                val predictedBias = (nearestIndex / (items.size - 1).toFloat()) * 2 - 1
                                
                                coroutineScope.launch {
                                    indicatorBias.animateTo(
                                        targetValue = predictedBias,
                                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                                    )
                                }
                                
                                if (nearestIndex != activeIndex) {
                                    onNavigate(items[nearestIndex].destination)
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                if (totalWidth > 0f) {
                                    val indicatorWidth = totalWidth / items.size
                                    val travelWidth = totalWidth - indicatorWidth
                                    if (travelWidth > 0f) {
                                        val biasDelta = (dragAmount / travelWidth) * 2f
                                        coroutineScope.launch {
                                            indicatorBias.snapTo((indicatorBias.value + biasDelta).coerceIn(-1f, 1f))
                                        }
                                    }
                                }
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(BiasAlignment(horizontalBias = indicatorBias.value, verticalBias = 0f))
                        .fillMaxWidth(1f / items.size)
                        .fillMaxHeight()
                        .scale(indicatorScale)
                        .padding(vertical = Spacing.small, horizontal = Spacing.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(Elevation.level2, RoundedCornerShape(CornerRadius.full))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        RawColors.Emerald.Emerald400.copy(alpha = 0.9f),
                                        RawColors.Emerald.Emerald400.copy(alpha = 0.75f),
                                        RawColors.Emerald.Emerald400.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(CornerRadius.full)
                            )
                            .border(
                                width = 0.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(CornerRadius.full)
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val itemBias = (index / (items.size - 1).toFloat()) * 2 - 1
                        val distance = kotlin.math.abs(indicatorBias.value - itemBias)
                        val threshold = 2f / (items.size - 1)
                        val colorWeight = (1f - (distance / (threshold * 0.8f))).coerceIn(0f, 1f)

                        BottomNavItem(
                            item = item,
                            colorWeight = colorWeight,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(item.destination) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    colorWeight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor = androidx.compose.ui.graphics.lerp(
        start = RawColors.Slate.Slate400,
        stop = Color.White,
        fraction = colorWeight
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (colorWeight > 0.5f) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = (-0.2).sp,
                fontSize = 11.sp
            ),
            color = contentColor,
        )
    }
}
