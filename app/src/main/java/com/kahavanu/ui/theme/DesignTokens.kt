package com.kahavanu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

@Immutable
object TextSize {
    val xs: TextUnit = 12.sp    // text-xs
    val sm: TextUnit = 14.sp    // text-sm
    val base: TextUnit = 16.sp  // text-base
    val lg: TextUnit = 18.sp    // text-lg
    val xl: TextUnit = 20.sp    // text-xl
    val xxl: TextUnit = 24.sp   // text-2xl
    val xxxl: TextUnit = 30.sp  // text-3xl
}

@Immutable
object Spacing {
    val extraSmall: Dp = 4.dp  // p-1
    val small: Dp = 8.dp       // p-2
    val medium: Dp = 12.dp     // p-3
    val large: Dp = 16.dp      // p-4
    val extraLarge: Dp = 24.dp // p-6
    val huge: Dp = 32.dp       // p-8
    val massive: Dp = 48.dp    // p-12
    val jumbo: Dp = 64.dp      // p-16
}

@Immutable
object CornerRadius {
    val extraSmall: Dp = 4.dp  // rounded
    val small: Dp = 6.dp       // rounded-md
    val medium: Dp = 8.dp      // rounded-lg
    val large: Dp = 12.dp      // rounded-xl
    val extraLarge: Dp = 24.dp // rounded-3xl
    val full: Dp = 9999.dp     // rounded-full (Tailwind uses 9999px for pills)
}

val KahavanuShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadius.extraSmall),
    small = RoundedCornerShape(CornerRadius.small),
    medium = RoundedCornerShape(CornerRadius.medium),
    large = RoundedCornerShape(CornerRadius.large),
    extraLarge = RoundedCornerShape(CornerRadius.extraLarge),
)

@Immutable
object Elevation {
    val level0: Dp = 0.dp  // shadow-none
    val level1: Dp = 1.dp  // shadow-sm
    val level2: Dp = 2.dp  // shadow
    val level3: Dp = 4.dp  // shadow-md
    val level4: Dp = 10.dp // shadow-lg
    val level5: Dp = 20.dp // shadow-xl
}

@Immutable
object BlurValues {
    val small: Dp = 4.dp       // blur-sm
    val medium: Dp = 12.dp     // blur-md
    val large: Dp = 16.dp      // blur-lg
    val extraLarge: Dp = 64.dp // blur-3xl
}

/**
 * Animation timing constants (milliseconds)
 */
@Immutable
object AnimationDuration {
    const val extraShort = 50
    const val short = 100
    const val medium = 200
    const val long = 300
    const val extraLong = 500
}

/**
 * Onboarding-specific design tokens
 */
@Immutable
object OnboardingTokens {
    // Header
    val headerWidth: Dp = 128.dp
    val headerHeight: Dp = 64.dp

    // Image
    val imageWidth: Dp = 300.dp
    val imageHeight: Dp = 400.dp

    // Button
    val buttonHeight: Dp = 54.dp
    val buttonWidth: Dp = 330.dp
    val buttonRadius: Dp = CornerRadius.full
    val buttonIconSize: Dp = 20.dp

    // Decorative overlay
    val gradientSize: Dp = 200.dp
    val gradientBlur: Dp = BlurValues.extraLarge
    val gradientTopOffset: Dp = 80.dp
    val gradientLeftOffset: Dp = 97.51.dp

    // Spacing
    val verticalSpacing: Dp = 60.dp
    val headerBottomSpacing: Dp = 100.dp
    val contentBottomSpacing: Dp = 80.dp
    val buttonBottomSpacing: Dp = 48.dp
}

/**
 * Typography styles for screen-specific use cases that fall outside
 * the standard Material 3 type scale.
 */
@Immutable
object TypographyTokens {
    val onboardingHeadline = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 28.sp,
        letterSpacing = (-0.85).sp,
    )

    val onboardingSubheading = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = (-0.15).sp,
    )

    val buttonLabel = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.5.sp,
        letterSpacing = (-0.23).sp,
    )
}

