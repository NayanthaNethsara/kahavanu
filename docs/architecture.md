# Architecture

## Overview

Kahavanu is a single-activity Android app built with Kotlin and Jetpack Compose. The app follows MVVM and uses a layered structure with repositories mediating between UI and data sources.

## Layers

- **UI layer** — Compose screens, ViewModels, shared components, and pure helpers in `ui/`.
- **Domain layer** — Models and repository interfaces in `domain/`.
- **Data layer** — Repository implementations, Room DAOs, Firestore sync code in `data/`.
- **DI layer** — Hilt modules in `di/`.

## Data flow

- UI triggers actions → ViewModel → Repository → data sources.
- UI observes state via `StateFlow` using `collectAsStateWithLifecycle()`.
- Repositories expose `Flow` and `suspend` APIs; UI never touches Firebase or Room directly.
- Domain models cross the boundary into the ViewModel/UI; Room `@Entity` / Firestore `DocumentSnapshot` types never do.

## Offline and sync

- Room provides local persistence and immediate UI data.
- Firebase Auth + Firestore provides cloud sync.
- Sync is orchestrated through `SyncManager` and WorkManager.
- Conflict resolution prefers the Firebase version on timestamp basis.

## Navigation

- Navigation Compose with a single activity ([`MainActivity`](../app/src/main/java/com/kahavanu/MainActivity.kt)).
- Destinations are sealed objects in `AppDestination` and wired in [`AppNavGraph`](../app/src/main/java/com/kahavanu/ui/navigation/AppNavGraph.kt) and `MainTabsScreen`.

## Theme system

The Compose theme is driven by Material 3 and an extended-colors composition local:

- **`KahavanuTheme`** ([Theme.kt](../app/src/main/java/com/kahavanu/ui/theme/Theme.kt)) wraps `MaterialTheme` and provides `LocalExtendedColors`.
- **`MaterialTheme.colorScheme.*`** — Material 3 standard slots (primary, surface, outlineVariant, …). Values are defined in [Color.kt](../app/src/main/java/com/kahavanu/ui/theme/Color.kt) as hex literals (no raw-palette indirection).
- **`MaterialTheme.extendedColors.*`** ([ExtendedColors.kt](../app/src/main/java/com/kahavanu/ui/theme/ExtendedColors.kt)) — semantic tokens that don't fit M3 slots: brand tonal scale (`brandWashed` → `brandDark`), `accentIncome`/`accentExpense`, the category palette, decorative glow/gradient tokens.
- **Top-level `val`s in `Color.kt`** — the same tokens exposed as constants for non-composable scopes (ViewModels, util fns).
- **`KahavanuTypography`** ([Type.kt](../app/src/main/java/com/kahavanu/ui/theme/Type.kt)) — standard M3 mobile scale with Inter font. Title-medium/small and all label styles use Medium weight per M3 spec; the rest use Normal.

Rule of thumb: composables prefer `MaterialTheme.colorScheme.*` / `MaterialTheme.extendedColors.*`; ViewModels and helpers use the matching top-level `val` from `Color.kt`. There is **no** `RawColors` module.

## Shared UI components (`ui/common/`)

Composables shared across features. Highlights:

- **Layout shells** — `KahavanuScreen`, `KahavanuSubScreen`, `AuthScaffold`, `TopAppHeader`, `BottomNavBar`.
- **Cards & surfaces** — `GlassCard`, `AppSurfaces`, `AppDecorativeGradientOverlay`.
- **Inputs & controls** — `AuthTextField`, `NestedSearchField`, `SearchWithFiltersBar`, `AppToggles`, `AppSelection`, `AppControls`, `FilterBottomSheet`.
- **Buttons** — `AppPrimaryButton`, `AuthButtons`, `MorphingIconButton`, `QuickActionRow` (+ `QuickAction` data class).
- **Lists & headers** — `SectionHeader`, `SectionLabel`, `MatchAndCatch`/`MatchingSection`.
- **Status & feedback** — `StatusBadge`, `EmptyState`, `AppSnackbar`, `BouncingDotsIndicator`.
- **Sheets** — `DetailSheet` (`SheetLabel`, `DetailRow`).

## Pure UI helpers (`ui/util/`)

Pure (non-`@Composable`) helpers usable from any scope, including ViewModels and other helpers. No Compose dependency in their public API.

- [`MoneyFormat.kt`](../app/src/main/java/com/kahavanu/ui/util/MoneyFormat.kt) — `formatAmount(amount, currency, decimals = 2)`.
- [`DateFormat.kt`](../app/src/main/java/com/kahavanu/ui/util/DateFormat.kt) — `formatDate`, `currentMonthLabel`, `dueLabel`.
- [`CategoryPalette.kt`](../app/src/main/java/com/kahavanu/ui/util/CategoryPalette.kt) — `categoryColor(name)` / `categoryIcon(name)` (canonical mapping for expense categories).
- [`GoalCategoryPalette.kt`](../app/src/main/java/com/kahavanu/ui/util/GoalCategoryPalette.kt) — `goalCategoryColor(GoalCategory)` / `goalCategoryIcon(GoalCategory)`.

Note: a few feature-specific helpers (`isPending`, `isRecurrent`, `sourceIconFor`) still live in [`income/components/IncomeUtils.kt`](../app/src/main/java/com/kahavanu/ui/income/components/IncomeUtils.kt) because they encode income-domain semantics, not general formatting.

## Data mapping & transformation

To keep repositories lean and maintainable:

- **Firestore mappers** — Dedicated mapper files (`IncomeFirestoreMappers.kt`, `ExpenseFirestoreMappers.kt`, `GoalMappers.kt`) handle the transformation between raw Firestore `DocumentSnapshot` objects and local Room entities, and between Room entities and domain models.
- **Defensive mapping** — Mappers include fallback logic for field naming variations and type safety for numeric values.
- **Domain at the boundary** — Repository interfaces always return domain models or domain-level types; Room entities never leak through the `domain/repository/` API.
