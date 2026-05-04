package com.kahavanu.ui

/**
 * # KAHAVANU DESIGN SYSTEM - Material Design 3 Implementation
 * 
 * This design system provides a comprehensive, consistent approach to building
 * the Kahavanu app using Material Design 3 principles and Jetpack Compose.
 * 
 * ## Design System Architecture
 * 
 * ### 1. Design Tokens (theme/DesignTokens.kt)
 * Central source of truth for all design values:
 * - **Spacing**: 4dp to 64dp scale for consistent padding/margins
 * - **CornerRadius**: 4dp to 50dp (full pill shape)
 * - **Elevation**: 0dp to 12dp shadow/elevation scale
 * - **BlurValues**: For decorative overlay effects
 * - **AnimationDuration**: Timing constants for animations
 * - **OnboardingTokens**: Screen-specific sizes and spacing
 * 
 * ### 2. Color System (theme/Color.kt)
 * Material Design 3 compliant color palette:
 * - **Primary**: #2855D8 (Blue) - Main brand color
 * - **Secondary**: #1BA97E (Green) - Accent color
 * - **Tertiary**: #006E3C (Dark Green) - Alternative accent
 * - **Error**: #B3261E (Red) - Error state
 * - **Semantic Colors**: 
 *   - OnboardingButtonGreen: #00BC7D (CTA button)
 *   - OnboardingTextPrimary: #0F172A (Headlines)
 *   - OnboardingTextSecondary: #64748B (Descriptions)
 * - **Dark Mode Support**: Separate light and dark variants
 * 
 * ### 3. Typography System (theme/Type.kt)
 * Material Design 3 typography scale with proper specifications:
 * - **Display**: Large (57sp), Medium (45sp), Small (36sp)
 * - **Headline**: Large (32sp), Medium (28sp), Small (24sp)
 * - **Title**: Large (22sp), Medium (16sp), Small (14sp)
 * - **Body**: Large (16sp), Medium (14sp), Small (12sp)
 * - **Label**: Large (14sp), Medium (12sp), Small (11sp)
 * All with proper font weights, line heights, and letter spacing
 * 
 * ### 4. Shape System (theme/DesignTokens.kt)
 * Consistent corner radius styling:
 * - ExtraSmall: 4dp (buttons, small chips)
 * - Small: 8dp (cards, text fields)
 * - Medium: 12dp (surfaces, dialogs)
 * - Large: 16dp (large surfaces)
 * - ExtraLarge: 28dp (expanded components)
 * - Full: 50dp (pill-shaped buttons)
 * 
 * ### 5. Theme Integration (theme/Theme.kt)
 * Centralized theme composable that provides:
 * - Complete Material3 color scheme
 * - Typography system
 * - Shape definitions
 * - Light/Dark mode support
 * - Wraps all components in KahavanuTheme
 * 
 * ## Component Library (component/)
 * 
 * ### OnboardingHeader
 * Logo display component for screen headers
 * - **Size**: 128x64 dp (via OnboardingTokens.headerWidth/Height)
 * - **Image Loading**: Coil AsyncImage with crossfade
 * - **Usage**: OnboardingHeader(logoUrl = "...")
 * - **Props**: logoUrl, modifier
 * 
 * ### OnboardingImage
 * Central feature image component
 * - **Size**: 280x372 dp (via OnboardingTokens.imageWidth/Height)
 * - **Image Loading**: Coil AsyncImage with crossfade
 * - **Content Scale**: Fit (maintains aspect ratio)
 * - **Usage**: OnboardingImage(imageUrl = "...", contentDescription = "...")
 * - **Props**: imageUrl, modifier, contentDescription
 * 
 * ### OnboardingContent
 * Headline and subheading text container
 * - **Headline**: headlineSmall style (24sp, weight 500)
 * - **Subheading**: bodyMedium style (14sp, weight 500)
 * - **Colors**: Uses OnboardingTextPrimary/Secondary semantic colors
 * - **Spacing**: Spacing.medium gap between headline and subheading
 * - **Usage**: OnboardingContent(headline = "...", subheading = "...")
 * - **Props**: headline, subheading, modifier
 * 
 * ### OnboardingButton
 * Primary call-to-action button
 * - **Style**: Material3 Button with shape.extraLarge
 * - **Color**: OnboardingButtonGreen (#00BC7D)
 * - **Size**: 330x54 dp (via OnboardingTokens.button*)
 * - **Icon**: Material Icons ArrowForward
 * - **Typography**: titleMedium style with Medium weight
 * - **Usage**: OnboardingButton(text = "Get started", onClick = { ... })
 * - **Props**: text, onClick, modifier, isEnabled
 * - **States**: Enabled/Disabled with proper color variants
 * 
 * ### OnboardingGradientOverlay
 * Decorative blurred gradient element
 * - **Size**: 256x256 dp (via OnboardingTokens.gradientSize)
 * - **Blur**: BlurValues.extraLarge (60dp)
 * - **Colors**: Green gradient (OnboardingGradientStart → OnboardingGradientEnd)
 * - **Shape**: CircleShape for visual interest
 * - **Usage**: OnboardingGradientOverlay(modifier = Modifier.offset(...))
 * - **Props**: modifier, topStartColor, topEndColor
 * 
 * ## Main Screen (screen/OnboardingScreen.kt)
 * 
 * The OnboardingScreen combines all components:
 * ```
 * 1. Decorative gradient overlay (positioned via offset)
 * 2. Vertical scrollable column
 * 3. Top spacing (Spacing.massive)
 * 4. Header logo
 * 5. Image spacing (OnboardingTokens.headerBottomSpacing)
 * 6. Main feature image
 * 7. Content spacing (OnboardingTokens.verticalSpacing)
 * 8. Headline + Subheading
 * 9. Button spacing (OnboardingTokens.contentBottomSpacing)
 * 10. CTA Button
 * 11. Bottom spacing (OnboardingTokens.buttonBottomSpacing)
 * ```
 * All elements use design tokens for consistency and easy maintenance.
 * 
 * ## Spacing Scale
 * 
 * All components respect a consistent spacing scale:
 * - **extraSmall**: 4.dp (minimal, between inline elements)
 * - **small**: 8.dp (tight spacing)
 * - **medium**: 12.dp (default component spacing)
 * - **large**: 16.dp (standard padding, horizontal)
 * - **extraLarge**: 24.dp (section padding)
 * - **huge**: 32.dp (large gaps between sections)
 * - **massive**: 48.dp (major section breaks)
 * - **jumbo**: 64.dp (screen-top/bottom padding)
 * 
 * ## Design Best Practices
 * 
 * ✓ **Always use design tokens** - Never hardcode colors, sizes, or spacing values
 * ✓ **Use Material Design 3 typography scale** - Apply correct text styles
 * ✓ **Semantic colors** - Use meaningful color names (Primary, Error, etc)
 * ✓ **Consistent spacing** - Use Spacing object for all padding/margins
 * ✓ **Accessibility first** - Include content descriptions for all images
 * ✓ **Material Icons** - Use official Material Icons for consistency
 * ✓ **Theme support** - Test in both light and dark modes
 * ✓ **Responsive design** - Use flexible layouts with proper spacing
 * 
 * ## Extending the System
 * 
 * To add new components to the design system:
 * 
 * 1. **Define tokens** in theme/DesignTokens.kt (if new sizes needed)
 * 2. **Use existing colors** from theme/Color.kt
 * 3. **Apply typography** from theme/Type.kt
 * 4. **Create composable** that references ONLY design tokens
 * 5. **Document** in this file with clear examples
 * 6. **Test** in both light and dark modes
 * 
 * ## File Structure
 * 
 * ```
 * ui/
 * ├── theme/
 * │   ├── Color.kt          # M3 color palette
 * │   ├── Type.kt           # M3 typography scale
 * │   ├── DesignTokens.kt   # Spacing, shapes, elevation
 * │   └── Theme.kt          # Theme composable
 * ├── component/
 * │   ├── OnboardingHeader.kt
 * │   ├── OnboardingImage.kt
 * │   ├── OnboardingContent.kt
 * │   ├── OnboardingButton.kt
 * │   └── OnboardingGradientOverlay.kt
 * └── screen/
 *     └── OnboardingScreen.kt
 * ```
 * 
 * ## Dependencies
 * 
 * - androidx.compose.material3 - Material Design 3 components
 * - androidx.compose.foundation - Layout primitives
 * - androidx.compose.material.icons - Material Icons
 * - coil3.compose - Image loading library
 * 
 * ## Version Information
 * 
 * - Compose BOM: 2024.09.00
 * - Material 3: Latest from BOM
 * - Kotlin: 2.0.21
 * - Coil: 3.0.0
 */
