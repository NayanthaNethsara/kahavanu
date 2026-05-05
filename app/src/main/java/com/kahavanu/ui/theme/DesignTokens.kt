package com.kahavanu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Spacing scale following Material Design 3
 * Used for consistent padding, margins, and gaps throughout the app
 */
@Immutable
object Spacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp
    val huge: Dp = 32.dp
    val massive: Dp = 48.dp
    val jumbo: Dp = 64.dp
}

/**
 * Corner radius scale following Material Design 3
 * Used for consistent shape styling
 */
@Immutable
object CornerRadius {
    // Extra small: 4dp (for small components)
    val extraSmall: Dp = 4.dp

    // Small: 8dp (for cards, chips)
    val small: Dp = 8.dp

    // Medium: 12dp (for default components)
    val medium: Dp = 12.dp

    // Large: 16dp (for large surfaces)
    val large: Dp = 16.dp

    // Extra large: 28dp (for expanded surfaces)
    val extraLarge: Dp = 28.dp

    // Full: for pill-shaped buttons
    val full: Dp = 50.dp
}

/**
 * Material Design 3 shapes
 */
val KahavanuShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadius.extraSmall),
    small = RoundedCornerShape(CornerRadius.small),
    medium = RoundedCornerShape(CornerRadius.medium),
    large = RoundedCornerShape(CornerRadius.large),
    extraLarge = RoundedCornerShape(CornerRadius.extraLarge),
)

/**
 * Shadow and elevation scale following Material Design 3
 */
@Immutable
object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
}

/**
 * Blur effects for decorative elements
 */
@Immutable
object BlurValues {
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 100.dp
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

