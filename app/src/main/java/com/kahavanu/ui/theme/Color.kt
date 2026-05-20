package com.kahavanu.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = RawColors.Emerald.Emerald600
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = RawColors.Emerald.Emerald100
val OnPrimaryContainer = RawColors.Emerald.Emerald900

val Secondary = RawColors.Slate.Slate600
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = RawColors.Slate.Slate100
val OnSecondaryContainer = RawColors.Slate.Slate900

val Tertiary = RawColors.Indigo.Indigo600
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = RawColors.Indigo.Indigo100
val OnTertiaryContainer = RawColors.Indigo.Indigo900

val Error = RawColors.Red.Red600
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = RawColors.Red.Red100
val OnErrorContainer = RawColors.Red.Red900

val LightBackground = RawColors.Slate.Slate50
val OnBackground = RawColors.Slate.Slate900
val LightSurface = Color(0xFFFFFFFF)
val OnSurface = RawColors.Slate.Slate900
val SurfaceVariant = RawColors.Slate.Slate100
val OnSurfaceVariant = RawColors.Slate.Slate600
val Outline = RawColors.Slate.Slate300
val OutlineVariant = RawColors.Slate.Slate200
val Scrim = Color(0xFF000000)

val DecorativeGradientStart = RawColors.Emerald.Emerald500.copy(alpha = 0.12f)
val DecorativeGradientEnd = RawColors.Emerald.Emerald500.copy(alpha = 0.04f)
val DecorativeGradientShadow = RawColors.Slate.Slate900.copy(alpha = 0.12f)

// General Text Colors - Slate
val TextPrimary = RawColors.Slate.Slate900
val TextSecondary = RawColors.Slate.Slate600
val TextTertiary = RawColors.Slate.Slate400

// General Text Colors - Emerald
val TextPrimaryEmerald = RawColors.Emerald.Emerald900
val TextSecondaryEmerald = RawColors.Emerald.Emerald700
val TextTertiaryEmerald = RawColors.Emerald.Emerald500

// Ambient glow tokens for the Kahavanu screen scaffold. Centralized here so
// screens never reach for RawColors directly.
val ScreenBackground = Color(0xFFFFFFFF)
val AmbientGlowPrimary = RawColors.Emerald.Emerald400.copy(alpha = 0.18f)
val AmbientGlowSecondary = RawColors.Emerald.Emerald400.copy(alpha = 0.12f)
val AmbientGlowTertiary = RawColors.Slate.Slate900.copy(alpha = 0.06f)

// Surface tokens used by scaffolded cards / back buttons.
val SurfaceCard = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val SurfaceCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.5f)
val SurfaceIcon = Color(0xFFFFFFFF).copy(alpha = 0.8f)
val SurfaceIconBorder = RawColors.Slate.Slate200.copy(alpha = 0.8f)

// Accent — orange "Expense" accent and emerald "Income" accent.
val AccentExpense = Color(0xFFF97316)
val AccentExpenseSoft = AccentExpense.copy(alpha = 0.13f)
val AccentExpenseBorder = AccentExpense.copy(alpha = 0.2f)
val AccentIncome = Color(0xFF00BC7D)
val AccentIncomeSoft = AccentIncome.copy(alpha = 0.12f)
val AccentIncomeBorder = AccentIncome.copy(alpha = 0.3f)

// Category palette. Centralized so screens don't pick RawColors by hand.
val CategoryFood = Color(0xFFF97316)
val CategoryTransport = RawColors.Blue.Blue500
val CategoryUtilities = RawColors.Violet.Violet500
val CategoryShopping = RawColors.Rose.Rose500
val CategoryLifestyle = RawColors.Amber.Amber500
val CategoryHealth = RawColors.Red.Red500
val CategorySubscriptions = RawColors.Indigo.Indigo400
val CategoryFun = RawColors.Emerald.Emerald500
val CategoryOther = RawColors.Slate.Slate400
