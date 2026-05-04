package com.kahavanu.ui

/**
 * # KAHAVANU DESIGN SYSTEM - Material Design 3 (Light Mode Only)
 *
 * ## Architecture
 *
 * ### Design Tokens (theme/DesignTokens.kt)
 * Central source of truth for all design values:
 * - **Spacing**: 4dp to 64dp scale (extraSmall through jumbo)
 * - **CornerRadius**: 4dp to 50dp (full pill shape)
 * - **Elevation**: 0dp to 12dp (level0 through level5)
 * - **BlurValues**: 8dp to 60dp for decorative overlays
 * - **AnimationDuration**: 50ms to 500ms timing constants
 * - **OnboardingTokens**: Screen-specific sizes and spacing
 * - **TypographyTokens**: TextStyle values for cases outside the M3 type scale
 *
 * ### Color System (theme/Color.kt)
 * - **Primary**: #2855D8 (Blue)
 * - **Secondary**: #1BA97E (Green)
 * - **Tertiary**: #006E3C (Dark Green)
 * - **Error**: #B3261E (Red)
 * - **Semantic**: OnboardingButtonGreen (#00BC7D), gradient colors
 *
 * ### Typography (theme/Type.kt)
 * Full Material Design 3 scale: Display, Headline, Title, Body, Label
 * at Large/Medium/Small variants with proper weights and letter spacing.
 *
 * ### Shapes (theme/DesignTokens.kt)
 * KahavanuShapes: extraSmall(4dp) through extraLarge(28dp), plus full(50dp) for pills.
 *
 * ### Theme (theme/Theme.kt)
 * Light-only KahavanuTheme composable providing colorScheme, typography, and shapes.
 *
 * ## Components (component/)
 *
 * ### AppPrimaryButton
 * Full-width CTA button with green background and arrow icon.
 * Uses OnboardingTokens.buttonHeight, M3 shapes.extraLarge, and appButtonHighlightBrush overlay.
 *
 * ### AppDecorativeGradientOverlay
 * Blurred circular gradient for visual interest.
 * Uses OnboardingTokens.gradientSize and gradientBlur.
 *
 * ## Navigation (navigation/)
 *
 * ### AppDestination
 * Sealed class providing type-safe route constants: Onboarding, Home, Login.
 *
 * ## Screens (screen/)
 *
 * ### OnboardingScreen
 * Scrollable layout: gradient overlay -> logo -> illustration -> headline -> subheading -> CTA button.
 * All sizing via OnboardingTokens, all spacing via Spacing object.
 *
 * ### HomeScreen
 * Placeholder destination after onboarding.
 *
 * ## File Structure
 *
 * ```
 * ui/
 * +-- DesignSystemGuide.kt
 * +-- theme/
 * |   +-- Color.kt
 * |   +-- Type.kt
 * |   +-- DesignTokens.kt
 * |   +-- AppButtonStyles.kt
 * |   +-- Theme.kt
 * +-- component/
 * |   +-- AppPrimaryButton.kt
 * |   +-- AppDecorativeGradientOverlay.kt
 * +-- navigation/
 * |   +-- AppDestination.kt
 * +-- screen/
 *     +-- OnboardingScreen.kt
 *     +-- HomeScreen.kt
 * ```
 *
 * ## Rules
 *
 * - Always use design tokens; never hardcode colors, sizes, or spacing.
 * - Use the Spacing object for all padding and margins.
 * - Include contentDescription for all images.
 * - Use AppDestination for navigation routes.
 * - New screen-specific tokens go in OnboardingTokens or a similarly named object.
 */
