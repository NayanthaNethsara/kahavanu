package com.kahavanu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    // Neutral extras
    val textTertiary: Color,
    val iconMuted: Color,
    val textStrong: Color,
    val textDeep: Color,

    // Brand tonal scale
    val brandWashed: Color,
    val brandBorder: Color,
    val brandSoft: Color,
    val brandGlow: Color,
    val brandAccent: Color,
    val brandText: Color,
    val brandDark: Color,

    // Warning
    val warningWashed: Color,
    val warningContainer: Color,
    val warningBorder: Color,
    val warningAccent: Color,
    val warning: Color,
    val warningText: Color,

    // Danger
    val dangerWashed: Color,
    val dangerBorder: Color,
    val dangerSoft: Color,
    val dangerAccent: Color,

    // Info
    val infoWashed: Color,
    val infoSoft: Color,
    val infoSubscriptions: Color,
    val infoAccent: Color,
    val infoStrong: Color,
    val info: Color,
    val infoDark: Color,

    // Utility / specialty
    val utility: Color,
    val utilityAccent: Color,
    val romance: Color,

    // Income / Expense accents
    val accentExpense: Color,
    val accentExpenseSoft: Color,
    val accentExpenseBorder: Color,
    val accentIncome: Color,
    val accentIncomeSoft: Color,
    val accentIncomeBorder: Color,

    // Category palette
    val categoryFood: Color,
    val categoryTransport: Color,
    val categoryUtilities: Color,
    val categoryShopping: Color,
    val categoryLifestyle: Color,
    val categoryHealth: Color,
    val categorySubscriptions: Color,
    val categoryFun: Color,
    val categoryOther: Color,

    // Decorative
    val ambientGlowPrimary: Color,
    val ambientGlowSecondary: Color,
    val ambientGlowTertiary: Color,
    val surfaceCard: Color,
    val surfaceCardBorder: Color,
    val surfaceIcon: Color,
    val surfaceIconBorder: Color,
    val decorativeGradientStart: Color,
    val decorativeGradientEnd: Color,
    val decorativeGradientShadow: Color,
    val screenBackground: Color,

    // Aliased emerald-tinted text
    val textPrimaryEmerald: Color,
    val textSecondaryEmerald: Color,
    val textTertiaryEmerald: Color,
)

val LightExtendedColors = ExtendedColors(
    textTertiary = TextTertiary,
    iconMuted = IconMuted,
    textStrong = TextStrong,
    textDeep = TextDeep,

    brandWashed = BrandWashed,
    brandBorder = BrandBorder,
    brandSoft = BrandSoft,
    brandGlow = BrandGlow,
    brandAccent = BrandAccent,
    brandText = BrandText,
    brandDark = BrandDark,

    warningWashed = WarningWashed,
    warningContainer = WarningContainer,
    warningBorder = WarningBorder,
    warningAccent = WarningAccent,
    warning = Warning,
    warningText = WarningText,

    dangerWashed = DangerWashed,
    dangerBorder = DangerBorder,
    dangerSoft = DangerSoft,
    dangerAccent = DangerAccent,

    infoWashed = InfoWashed,
    infoSoft = InfoSoft,
    infoSubscriptions = InfoAccentIndigo400,
    infoAccent = InfoAccent,
    infoStrong = InfoStrong,
    info = Info,
    infoDark = InfoDark,

    utility = Utility,
    utilityAccent = UtilityAccent,
    romance = Romance,

    accentExpense = AccentExpense,
    accentExpenseSoft = AccentExpenseSoft,
    accentExpenseBorder = AccentExpenseBorder,
    accentIncome = AccentIncome,
    accentIncomeSoft = AccentIncomeSoft,
    accentIncomeBorder = AccentIncomeBorder,

    categoryFood = CategoryFood,
    categoryTransport = CategoryTransport,
    categoryUtilities = CategoryUtilities,
    categoryShopping = CategoryShopping,
    categoryLifestyle = CategoryLifestyle,
    categoryHealth = CategoryHealth,
    categorySubscriptions = CategorySubscriptions,
    categoryFun = CategoryFun,
    categoryOther = CategoryOther,

    ambientGlowPrimary = AmbientGlowPrimary,
    ambientGlowSecondary = AmbientGlowSecondary,
    ambientGlowTertiary = AmbientGlowTertiary,
    surfaceCard = SurfaceCard,
    surfaceCardBorder = SurfaceCardBorder,
    surfaceIcon = SurfaceIcon,
    surfaceIconBorder = SurfaceIconBorder,
    decorativeGradientStart = DecorativeGradientStart,
    decorativeGradientEnd = DecorativeGradientEnd,
    decorativeGradientShadow = DecorativeGradientShadow,
    screenBackground = ScreenBackground,

    textPrimaryEmerald = TextPrimaryEmerald,
    textSecondaryEmerald = TextSecondaryEmerald,
    textTertiaryEmerald = TextTertiaryEmerald,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
