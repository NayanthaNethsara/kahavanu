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
 * ## Components (common/)
 *
 * ### AppPrimaryButton
 * Full-width CTA button with green background and arrow icon.
 * Uses OnboardingTokens.buttonHeight, M3 shapes.extraLarge, and appButtonHighlightBrush overlay.
 *
 * ### AuthPrimaryButton / AuthOutlinedButton
 * Auth-specific button variants for sign-in/sign-up flows.
 *
 * ### AuthScaffold
 * Shared auth screen layout shell (logo, back button, title/subtitle, gradient overlay).
 *
 * ### AppSurfaces / AppControls / AppSelection / AppToggles
 * Shared UI building blocks split by category:
 * - AppSurfaces: GlassCard, SectionHeader, SectionLabel
 * - AppControls: CircularIconButton, PrimaryActionButton, GradientBlob, textFieldColors
 * - AppSelection: SelectableChip, SelectableCard
 * - AppToggles: AppSegmentedToggle
 *
 * ### AuthTextField
 * Labeled outlined text field with leading/trailing icon support.
 *
 * ### AppDecorativeGradientOverlay
 * Blurred circular gradient for visual interest.
 * Uses OnboardingTokens.gradientSize and gradientBlur.
 *
 * ## Navigation (navigation/)
 *
 * ### AppDestination
 * Sealed class providing type-safe route constants: Onboarding, AuthChoice, Login, Signup, Home.
 *
 * ### AppNavGraph
 * Composable function encapsulating the full NavHost and all destination wiring.
 *
 * ## Feature Packages
 *
 * ### auth/
 * AuthChoiceScreen, LoginScreen, SignupScreen, AuthViewModel, AuthUiState, GoogleSignInHelper
 *
 * ### onboarding/
 * OnboardingScreen
 *
 * ### home/
 * HomeScreen
 *
 * ## File Structure
 *
 * ```
 * com.kahavanu/
 * +-- MainActivity.kt
 * +-- core/
 * |   +-- config/
 * |       +-- AppConfig.kt
 * +-- data/
 * |   +-- auth/
 * |       +-- DefaultAuthRepository.kt
 * +-- domain/
 * |   +-- model/
 * |   |   +-- UserSession.kt
 * |   +-- repository/
 * |       +-- AuthRepository.kt
 * +-- di/
 * |   +-- AuthModule.kt
 * +-- ui/
 *     +-- DesignSystemGuide.kt
 *     +-- auth/
 *     |   +-- AuthChoiceScreen.kt
 *     |   +-- AuthUiState.kt
 *     |   +-- AuthViewModel.kt
 *     |   +-- GoogleSignInHelper.kt
 *     |   +-- LoginScreen.kt
 *     |   +-- SignupScreen.kt
 *     +-- common/
 *     |   +-- AppDecorativeGradientOverlay.kt
 *     |   +-- AppPrimaryButton.kt
 *     |   +-- AuthButtons.kt
 *     |   +-- AuthScaffold.kt
 *     |   +-- AuthTextField.kt
 *     |   +-- AppControls.kt
 *     |   +-- AppSelection.kt
 *     |   +-- AppSurfaces.kt
 *     |   +-- AppToggles.kt
 *     +-- home/
 *     |   +-- HomeScreen.kt
 *     +-- navigation/
 *     |   +-- AppDestination.kt
 *     |   +-- AppNavGraph.kt
 *     +-- onboarding/
 *     |   +-- OnboardingScreen.kt
 *     +-- theme/
 *         +-- AppButtonStyles.kt
 *         +-- Color.kt
 *         +-- DesignTokens.kt
 *         +-- Theme.kt
 *         +-- Type.kt
 * ```
 *
 * ## Rules
 *
 * - Always use design tokens; never hardcode colors, sizes, or spacing.
 * - Use the Spacing object for all padding and margins.
 * - Include contentDescription for all images.
 * - Use AppDestination for navigation routes.
 * - New screen-specific tokens go in OnboardingTokens or a similarly named object.
 * - Repository interfaces live in domain/repository; implementations in data/.
 * - ViewModels depend on domain interfaces, never on data implementations directly.
 * - All dependency wiring goes through di/AuthModule (Hilt @Module).
 * - Use @HiltViewModel + @Inject constructor on ViewModels; use hiltViewModel() in screens.
 * - No manual ViewModelProvider.Factory or singleton containers in Composables.
 */
