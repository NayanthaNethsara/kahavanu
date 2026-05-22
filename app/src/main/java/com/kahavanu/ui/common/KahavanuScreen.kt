package com.kahavanu.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.AmbientGlowPrimary
import com.kahavanu.ui.theme.AmbientGlowSecondary
import com.kahavanu.ui.theme.AmbientGlowTertiary
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.ScreenBackground
import com.kahavanu.ui.theme.Spacing

/**
 * Shared screen scaffold that matches the HomeScreen pattern: ambient glow
 * background, top-aligned ScreenHeader, and a LazyColumn with consistent
 * content padding and section spacing.
 *
 * Use [screenSection] inside [content] to get the standard horizontal padding
 * applied to each section card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KahavanuScreen(
    headerLabel: String,
    headerTitle: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(top = 140.dp, bottom = 120.dp),
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val pullState = rememberPullToRefreshState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        AmbientGlow(
            color = AmbientGlowPrimary,
            size = 360.dp,
            modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
        )
        AmbientGlow(
            color = AmbientGlowSecondary,
            size = 320.dp,
            modifier = Modifier.offset(x = 170.dp, y = 284.dp),
        )
        AmbientGlow(
            color = AmbientGlowTertiary,
            size = 300.dp,
            modifier = Modifier.offset(x = 98.dp, y = 648.dp),
        )

        val lazyColumnContent = @Composable { pullProgress: Float ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Premium rubber band dampening: translate = max * (1 - e^(-progress * k))
                        val maxTranslation = 200f
                        val dampenedTranslation = if (pullProgress > 0f) {
                            maxTranslation * (1f - kotlin.math.exp(-pullProgress * 0.8f))
                        } else {
                            0f
                        }
                        translationY = dampenedTranslation
                    },
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.extraLarge),
                    ) {
                        ScreenHeader(label = headerLabel, title = headerTitle)
                    }
                }
                content()
            }
        }

        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                val maxIndicatorTranslation = 80.dp.toPx()
                                val indicatorTranslation = if (pullState.distanceFraction > 0f) {
                                    maxIndicatorTranslation * (1f - kotlin.math.exp(-pullState.distanceFraction * 0.8f))
                                } else {
                                    0f
                                }
                                translationY = indicatorTranslation - 16.dp.toPx()
                                alpha = pullState.distanceFraction.coerceIn(0f, 1f)
                            }
                            .padding(top = 16.dp)
                    ) {
                        BouncingDotsIndicator(
                            progress = pullState.distanceFraction,
                            isRefreshing = isRefreshing
                        )
                    }
                }
            ) {
                lazyColumnContent(pullState.distanceFraction)
            }
        } else {
            lazyColumnContent(0f)
        }
    }
}

/**
 * Premium custom loading indicator for the pull-to-refresh feature.
 * Shows three horizontal emerald dots inside a glassmorphic pill background.
 * Dots scale dynamically with drag progress or bounce out of phase during active refreshing.
 */
@Composable
fun BouncingDotsIndicator(
    progress: Float,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 12.dp,
            spotColor = RawColors.Gray.Gray300,
            ambientColor = RawColors.Gray.Gray400,
            shape = CircleShape,
        ),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "BouncingDots")

            // Dot 1
            val animatedScale1 by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(0)
                ),
                label = "dotScale_0"
            )
            val dragScale1 = (progress * 3f - 0f).coerceIn(0f, 1f) * 0.9f
            val scale1 = if (isRefreshing) animatedScale1 else dragScale1
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale1)
                    .background(
                        color = RawColors.Emerald.Emerald500,
                        shape = CircleShape
                    )
            )

            // Dot 2
            val animatedScale2 by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(180)
                ),
                label = "dotScale_1"
            )
            val dragScale2 = (progress * 3f - 1f).coerceIn(0f, 1f) * 0.9f
            val scale2 = if (isRefreshing) animatedScale2 else dragScale2
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale2)
                    .background(
                        color = RawColors.Emerald.Emerald500,
                        shape = CircleShape
                    )
            )

            // Dot 3
            val animatedScale3 by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(360)
                ),
                label = "dotScale_2"
            )
            val dragScale3 = (progress * 3f - 2f).coerceIn(0f, 1f) * 0.9f
            val scale3 = if (isRefreshing) animatedScale3 else dragScale3
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale3)
                    .background(
                        color = RawColors.Emerald.Emerald500,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Adds a section to a [KahavanuScreen] with the standard horizontal padding.
 */
fun LazyListScope.screenSection(
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.extraLarge),
        ) {
            content()
        }
    }
}
