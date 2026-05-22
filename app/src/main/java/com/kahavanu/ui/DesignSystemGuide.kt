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
 * - **Primary**: Emerald600 (Main brand, success, growth)
 * - **Secondary**: Slate600 (Neutral actions, secondary labels)
 * - **Tertiary**: Indigo600 (Complementary tech accents)
 * - **Error**: Red600 (Alerts and critical states)
 * - **Background**: Slate50 (Crisp, modern background)
 * - **Surface**: White (Primary content surfaces)
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
 * - AppControls: circularIconButton modifier, PrimaryActionButton, GradientBlob, textFieldColors
 * - AppSelection: SelectableChip, SelectableCard
 * - AppToggles: AppSegmentedToggle
 *
 * ### MatchAndCatch (common/MatchAndCatch.kt)
 * Standardized transaction matching UI used in Income and Expenses modules.
 * - **MatchingSection**: Wrapper for "Match & Categorize" workflows.
 * - **MatchItemState**: Data structure for matchable items.
 *
 * ### SummaryItems (common/SummaryItems.kt)
 * Standardized row items for categorized data:
 * - **SummaryItem**: Row with icon, label, and formatted value.
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
 * Sealed class providing type-safe route constants: Onboarding, AuthChoice, Login, Signup, Home, Income, Expenses.
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
 * ### income/
 * IncomeScreen, IncomeViewModel, IncomeHistory, IncomeSources, IncomeSync
 *
 * ### expenses/
 * ExpensesScreen, ExpensesViewModel, ExpensesSync
 *
 * ## File Structure
 *
 * ```
 * com.kahavanu/
 * +-- MainActivity.kt
 * +-- data/
 * |   +-- auth/
 * |       +-- DefaultAuthRepository.kt
 * |   +-- income/
 * |       +-- DefaultIncomeRepository.kt
 * |       +-- IncomeFirestoreMappers.kt
 * |   +-- expenses/
 * |       +-- DefaultExpensesRepository.kt
 * |       +-- ExpenseFirestoreMappers.kt
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
 *     |   +-- MatchAndCatch.kt
 *     |   +-- SummaryItems.kt
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
 * - **Firestore Mapping**: Decouple Firestore `DocumentSnapshot` transformation into dedicated `FirestoreMappers.kt` files in the data layer.
 */
