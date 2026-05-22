package com.kahavanu.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 ColorScheme values — referenced from Theme.kt
// ─────────────────────────────────────────────────────────────────────────────
val Primary = Color(0xFF059669)              // emerald-600
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD1FAE5)     // emerald-100
val OnPrimaryContainer = Color(0xFF064E3B)   // emerald-900

val Secondary = Color(0xFF475569)            // slate-600
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF1F5F9)   // slate-100
val OnSecondaryContainer = Color(0xFF0F172A) // slate-900

val Tertiary = Color(0xFF4F46E5)             // indigo-600
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFE0E7FF)    // indigo-100
val OnTertiaryContainer = Color(0xFF312E81)  // indigo-900

val Error = Color(0xFFDC2626)                // red-600
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFEE2E2)       // red-100
val OnErrorContainer = Color(0xFF7F1D1D)     // red-900

val LightBackground = Color(0xFFF8FAFC)      // slate-50
val OnBackground = Color(0xFF0F172A)         // slate-900
val LightSurface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF0F172A)            // slate-900
val SurfaceVariant = Color(0xFFF1F5F9)       // slate-100
val OnSurfaceVariant = Color(0xFF475569)     // slate-600
val Outline = Color(0xFFCBD5E1)              // slate-300
val OutlineVariant = Color(0xFFE2E8F0)       // slate-200
val Scrim = Color(0xFF000000)

// ─────────────────────────────────────────────────────────────────────────────
// Extended semantic tokens — usable in any scope (ViewModels, util fns).
// Composable scope should prefer MaterialTheme.extendedColors.* (same values).
// ─────────────────────────────────────────────────────────────────────────────

// Neutral scale extras (slate shades not covered by M3 ColorScheme slots)
val TextTertiary = Color(0xFF94A3B8)         // slate-400
val IconMuted = Color(0xFF64748B)            // slate-500
val TextStrong = Color(0xFF334155)           // slate-700
val TextDeep = Color(0xFF1E293B)             // slate-800

// Brand tonal scale (emerald shades not in primary/onPrimaryContainer)
val BrandWashed = Color(0xFFECFDF5)          // emerald-50
val BrandBorder = Color(0xFFA7F3D0)          // emerald-200
val BrandSoft = Color(0xFF6EE7B7)            // emerald-300
val BrandGlow = Color(0xFF34D399)            // emerald-400
val BrandAccent = Color(0xFF10B981)          // emerald-500
val BrandText = Color(0xFF047857)            // emerald-700
val BrandDark = Color(0xFF065F46)            // emerald-800

// Warning (amber)
val WarningWashed = Color(0xFFFFFBEB)        // amber-50
val WarningContainer = Color(0xFFFEF3C7)     // amber-100
val WarningBorder = Color(0xFFFDE68A)        // amber-200
val WarningAccent = Color(0xFFF59E0B)        // amber-500
val Warning = Color(0xFFD97706)              // amber-600
val WarningText = Color(0xFF92400E)          // amber-800

// Danger tones beyond M3 error slots (red)
val DangerWashed = Color(0xFFFEF2F2)         // red-50
val DangerBorder = Color(0xFFFECACA)         // red-200
val DangerSoft = Color(0xFFF87171)           // red-400
val DangerAccent = Color(0xFFEF4444)         // red-500

// Info (indigo extras + blue)
val InfoWashed = Color(0xFFEEF2FF)           // indigo-50
val InfoSoft = Color(0xFFA5B4FC)             // indigo-300 (legacy: was Indigo400 #818CF8 in some callsites)
val InfoAccentIndigo400 = Color(0xFF818CF8)  // indigo-400 — kept for category subscriptions parity
val InfoAccent = Color(0xFF6366F1)           // indigo-500
val InfoStrong = Color(0xFF4338CA)           // indigo-700
val Info = Color(0xFF3B82F6)                 // blue-500
val InfoDark = Color(0xFF1E40AF)             // blue-800

// Utility / specialty
val Utility = Color(0xFFA78BFA)              // violet-400
val UtilityAccent = Color(0xFF8B5CF6)        // violet-500
val Romance = Color(0xFFF43F5E)              // rose-500

// Income / Expense accents
val AccentExpense = Color(0xFFF97316)
val AccentExpenseSoft = AccentExpense.copy(alpha = 0.13f)
val AccentExpenseBorder = AccentExpense.copy(alpha = 0.2f)
val AccentIncome = Color(0xFF00BC7D)
val AccentIncomeSoft = AccentIncome.copy(alpha = 0.12f)
val AccentIncomeBorder = AccentIncome.copy(alpha = 0.3f)

// Category palette
val CategoryFood = Color(0xFFF97316)
val CategoryTransport = Info
val CategoryUtilities = UtilityAccent
val CategoryShopping = Romance
val CategoryLifestyle = WarningAccent
val CategoryHealth = DangerAccent
val CategorySubscriptions = InfoAccentIndigo400
val CategoryFun = BrandAccent
val CategoryOther = TextTertiary

// Decorative / ambient
val DecorativeGradientStart = BrandAccent.copy(alpha = 0.12f)
val DecorativeGradientEnd = BrandAccent.copy(alpha = 0.04f)
val DecorativeGradientShadow = OnBackground.copy(alpha = 0.12f)

val ScreenBackground = Color(0xFFFFFFFF)
val AmbientGlowPrimary = BrandGlow.copy(alpha = 0.18f)
val AmbientGlowSecondary = BrandGlow.copy(alpha = 0.12f)
val AmbientGlowTertiary = OnBackground.copy(alpha = 0.06f)

val SurfaceCard = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val SurfaceCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.5f)
val SurfaceIcon = Color(0xFFFFFFFF).copy(alpha = 0.8f)
val SurfaceIconBorder = OutlineVariant.copy(alpha = 0.8f)

// Text aliases that compose against background/onPrimaryContainer
val TextPrimary = OnBackground
val TextSecondary = OnSurfaceVariant
val TextPrimaryEmerald = OnPrimaryContainer
val TextSecondaryEmerald = BrandText
val TextTertiaryEmerald = BrandAccent
